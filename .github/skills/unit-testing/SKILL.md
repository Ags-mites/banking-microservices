---
name: unit-testing
description: Genera tests unitarios e integración para backend y/o frontend. Lee la spec y el código implementado. Requiere spec APPROVED e implementación completa.
argument-hint: "<nombre-feature> [backend|frontend|ambos]"
---

# Unit Testing

## Definition of Done — verificar al completar

- [ ] Cobertura ≥ 80% en lógica de negocio (quality gate bloqueante)
- [ ] Tests aislados — sin conexión a DB real ni servicios externos (siempre mocks)
- [ ] Escenario feliz + errores de negocio + validaciones de entrada cubiertos
- [ ] Los cambios no rompen contratos existentes del módulo

## Prerequisito — Lee en paralelo

```
[[specs/<feature>.spec.md]]        (criterios de aceptación)
código implementado en backend/ y/o frontend/
[[instructions/backend.instructions.md]]   (JUnit 5 + Mockito)
[[instructions/frontend.instructions.md]]  (Vitest + Testing Library)
```

## Output por scope

### Backend → `src/test/java/com/example/<service>/`

| Archivo | Cubre |
|---------|-------|
| `domain/service/<Feature>ServiceTests.java` | Lógica de negocio: happy path + errores |
| `infrastructure/input/<Feature>ControllerTests.java` | Endpoints: 200/201, 400, 404, 409 |
| `infrastructure/output/<Feature>RepositoryAdapterTests.java` | Adaptador: parámetros y retornos correctos |

### Frontend → `frontend/src/__tests__/`

| Archivo | Cubre |
|---------|-------|
| `components/<Feature>.test.jsx` | Render + interacciones (click, submit) |
| `hooks/use<Feature>.test.js` | Estado inicial + respuesta API + error handling |
| `pages/<Feature>Page.test.jsx` | Render completo con providers |

## Patrones core

```java
// Backend — JUnit 5 + Mockito (AAA Pattern)
package com.example.banking.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTests {
    
    @Mock
    private AccountRepositoryPort accountRepository;
    
    @InjectMocks
    private AccountService accountService;
    
    @Test
    void createAccount_WithValidData_ReturnsSuccess() {
        // Arrange — preparar datos y contexto
        CreateAccountDto dto = new CreateAccountDto("ACC-001", 1L, AccountType.SAVINGS);
        
        Account savedAccount = Account.create("ACC-001", 1L, AccountType.SAVINGS);
        when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);
        when(accountRepository.findByClientId(1L)).thenReturn(List.of());
        
        // Act — ejecutar la acción bajo prueba
        AccountResponseDto result = accountService.createAccount(dto);
        
        // Assert — verificar resultado esperado
        assertNotNull(result);
        assertEquals("ACC-001", result.accountNumber());
        verify(accountRepository, times(1)).save(any(Account.class));
    }
    
    @Test
    void createAccount_WithExistingClient_ReturnsExistingAccount() {
        // Arrange
        CreateAccountDto dto = new CreateAccountDto("ACC-001", 1L, AccountType.SAVINGS);
        
        Account existingAccount = Account.create("ACC-001", 1L, AccountType.SAVINGS);
        when(accountRepository.findByClientId(1L)).thenReturn(List.of(existingAccount));
        
        // Act
        AccountResponseDto result = accountService.createAccount(dto);
        
        // Assert
        assertNotNull(result);
        verify(accountRepository, never()).save(any(Account.class));
    }
    
    @Test
    void withdraw_WithInsufficientFunds_ThrowsException() {
        // Arrange
        Account account = Account.create("ACC-001", 1L, AccountType.SAVINGS);
        account.deposit(100.0);
        
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        
        // Act & Assert
        assertThrows(InsufficientFundsException.class, () -> {
            accountService.withdraw(1L, 500.0);
        });
    }
}

// Test de Controller
@ExtendWith(MockitoExtension.class)
class AccountControllerTests {
    
    @Mock
    private AccountUseCase accountUseCase;
    
    @InjectMocks
    private AccountController accountController;
    
    @Test
    void getAccount_WhenExists_Returns200() {
        // Arrange
        AccountResponseDto dto = new AccountResponseDto(
            1L, "ACC-001", 1L, 1000.0, AccountType.SAVINGS, 
            LocalDateTime.now(), LocalDateTime.now()
        );
        when(accountUseCase.getAccountById(1L)).thenReturn(dto);
        
        // Act
        ResponseEntity<?> response = accountController.getAccount(1L);
        
        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }
    
    @Test
    void getAccount_WhenNotExists_ThrowsException() {
        // Arrange
        when(accountUseCase.getAccountById(999L))
            .thenThrow(new AccountNotFoundException("Account not found"));
        
        // Act & Assert
        assertThrows(AccountNotFoundException.class, () -> {
            accountController.getAccount(999L);
        });
    }
}
```

```js
// Frontend — mock service + renderHook (Vitest + Testing Library)
vi.mock('../../services/featureService');
getFeatures.mockResolvedValue([{ uid: '1' }]);
const { result } = renderHook(() => useFeature());
await waitFor(() => expect(result.current.data).toHaveLength(1));
```

## Dependencias Maven para Testing

```xml
<!-- pom.xml -->
<dependencies>
    <!-- JUnit 5 -->
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <version>5.10.2</version>
        <scope>test</scope>
    </dependency>
    
    <!-- Mockito -->
    <dependency>
        <groupId>org.mockito</groupId>
        <artifactId>mockito-core</artifactId>
        <version>5.8.0</version>
        <scope>test</scope>
    </dependency>
    
    <dependency>
        <groupId>org.mockito</groupId>
        <artifactId>mockito-junit-jupiter</artifactId>
        <version>5.8.0</version>
        <scope>test</scope>
    </dependency>
    
    <!-- Spring Boot Test -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

## Comandos Maven para Tests

### Ejecutar todos los tests
```bash
./mvnw test
```

### Ejecutar tests con reporte de cobertura (JaCoCo)
```bash
./mvnw verify
```

### Ejecutar test específico
```bash
./mvnw test -Dtest=AccountServiceTests
```

### Verificar cobertura mínima (80%)
```bash
./mvnw verify -Djacoco.min.coverage=0.80
```

## Estructura de Tests (Backend)

```
src/test/java/com/example/<service>/
├── domain/
│   ├── model/                    # Tests de entidades (lógica de dominio)
│   │   └── AccountTests.java
│   └── service/                  # Tests de casos de uso
│       └── AccountServiceTests.java
├── application/                   # Tests de mappers (MapStruct)
│   └── AccountMapperTests.java
└── infrastructure/
    ├── input/                    # Tests de controladores REST
    │   └── AccountControllerTests.java
    └── output/                   # Tests de adaptadores
        └── AccountRepositoryAdapterTests.java
```

## Restricciones

- Solo `src/test/java/` para tests backend. No modificar código fuente.
- Nunca conectar a DB real ni servicios externos — siempre mocks (Mockito para Java).
- Cobertura mínima ≥ 80% en lógica de negocio (verificar con JaCoCo).
- Usar `@ExtendWith(MockitoExtension.class)` para inyección de mocks.
- Seguir patrón AAA: Arrange, Act, Assert.
- No usar `@SpringBootTest` en tests unitarios — usar mocks puros.
