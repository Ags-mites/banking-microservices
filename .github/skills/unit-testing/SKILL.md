---
name: unit-testing
description: Genera tests unitarios para backend Java según la tabla de pruebas de la spec.Requiere spec APPROVED.
argument-hint: "<nombre-feature>"
---

# Unit Testing — Backend Java

## Definition of Done — verificar al completar

- [ ] Cobertura ≥ 80% en lógica de negocio (quality gate bloqueante)
- [ ] Tests aislados — sin conexión a DB real ni servicios externos (siempre mocks)
- [ ] Solo pruebas definidas en la spec — NO crear pruebas adicionales
- [ ] Los cambios no rompen contratos existentes del módulo

## Matriz de Pruebas — CANTIDAD FIJA

**La spec define exactamente qué pruebas generar. NO inventar más.**

| Capa | Test | Cantidad | Escenario |
|------|------|---------|---------|
| **Service** | `*ServiceTests.java` | **3 por método** | 1 happy + 1 error + 1 edge |
| **Controller** | `*ControllerTests.java` | **3 por endpoint** | 200/201 + 400 + 404 |
| **Adapter** | `*RepositoryAdapterTests.java` | **2 por método** | save + findById |

### Regla: 3-2-1
```
POR CADA MÉTODO EN SERVICE:
  + 1 test happy path
  + 1 test error de negocio
  + 1 test edge case

POR CADA ENDPOINT EN CONTROLLER:
  + 1 test éxito (200/201)
  + 1 test validación (400)
  + 1 test not found (404)

POR CADA MÉTODO EN REPOSITORY ADAPTER:
  + 1 test save
  + 1 test findById
```

## Prerequisito — Lee en paralelo

```
[[specs/<feature>.spec.md]]        (Sección 3: Lista de Tareas → Tests)
[[instructions/backend.instructions.md]]   (JUnit 5 + Mockito)
```

## Output por scope

### Backend → `src/test/java/com/example/<service>/`

| Archivo | Cubre |
|---------|-------|
| `domain/service/<Feature>ServiceTests.java` | Lógica de negocio: happy path + errores |
| `infrastructure/input/<Feature>ControllerTests.java` | Endpoints: 200/201, 400, 404, 409 |
| `infrastructure/output/<Feature>RepositoryAdapterTests.java` | Adaptador: parámetros y retornos correctos |

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
