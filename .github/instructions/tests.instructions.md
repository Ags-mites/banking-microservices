---
applyTo: "src/test/java/**/*.java"
---

> **Scope**: Las reglas de backend aplican a proyectos con tests en Java (Spring Boot 4 + JUnit 5). En proyectos con otro stack, adaptar las herramientas y convenciones manteniendo los principios (independencia, aislamiento, AAA, cobertura ≥ 80%).

# Instrucciones para Archivos de Pruebas Unitarias

## Principios No Negociables

- **Independencia**: cada test es 100% independiente — sin estado compartido entre tests.
- **Aislamiento Total**: mockear SIEMPRE dependencias externas (DB, APIs, servicios externos).
- **Patrón AAA Obligatorio**: Arrange → Act → Assert en CADA test.
- **Cobertura ≥ 80% Bloqueante**: Un PR se rechaza si la cobertura es < 80% en cualquier ruta crítica.
- **Claridad de Nombres**: nombre del test describe la función, escenario y resultado esperado.

## Backend (JUnit 5 + Mockito + AssertJ)

### Framework Obligatorio (No Negociable)
- **Test Framework**: ✅ JUnit 5 (Jupiter) (❌ NO JUnit 4, ❌ NO TestNG)
- **Mocking**: ✅ Mockito (❌ NO EasyMock, ❌ NO JMock)
- **Assertions**: ✅ AssertJ (legibilidad) o JUnit assertions

### Estructura AAA + Constructor Injection

```java
package com.example.banking.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.*;
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
        // ARRANGE — preparar mocks y datos
        CreateAccountDto dto = new CreateAccountDto("ACC-001", 1L, AccountType.SAVINGS);
        
        Account savedAccount = Account.create("ACC-001", 1L, AccountType.SAVINGS);
        when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);
        when(accountRepository.findByClientId(1L)).thenReturn(List.of());
        
        // ACT — ejecutar bajo prueba
        AccountResponseDto result = accountService.createAccount(dto);
        
        // ASSERT — verificar resultado
        assertThat(result).isNotNull();
        assertThat(result.accountNumber()).isEqualTo("ACC-001");
        assertThat(result.balance()).isEqualTo(0.0);
        
        // Verificar que el repo fue llamado
        verify(accountRepository, times(1)).save(any(Account.class));
    }
    
    @Test
    void createAccount_WithExistingClient_ReturnsExistingAccount() {
        // ARRANGE
        CreateAccountDto dto = new CreateAccountDto("ACC-001", 1L, AccountType.SAVINGS);
        
        Account existingAccount = Account.create("ACC-001", 1L, AccountType.SAVINGS);
        when(accountRepository.findByClientId(1L)).thenReturn(List.of(existingAccount));
        
        // ACT
        AccountResponseDto result = accountService.createAccount(dto);
        
        // ASSERT
        assertThat(result).isNotNull();
        verify(accountRepository, never()).save(any(Account.class));
    }
    
    @Test
    void withdraw_WithInsufficientFunds_ThrowsException() {
        // ARRANGE
        Account account = Account.create("ACC-001", 1L, AccountType.SAVINGS);
        account.deposit(100.0);
        
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        
        // ACT & ASSERT
        assertThatThrownBy(() -> accountService.withdraw(1L, 500.0))
            .isInstanceOf(InsufficientFundsException.class)
            .hasMessageContaining("Insufficient funds");
    }
}
```

### Naming Convención Obligatoria

- **Test Name**: `{Método}_{Escenario}_{ResultadoEsperado}`
  - ✅ `createAccount_WithValidData_ReturnsSuccess`
  - ✅ `withdraw_WithInsufficientFunds_ThrowsException`
  - ✅ `deposit_WithNegativeAmount_ThrowsIllegalArgumentException`
  - ❌ `test1 `, ❌ `AccountTest`, ❌ `createTest`

### Mocks: Regla de Oro

```java
// ❌ NUNCA HACER — tocar BD real
@Autowired
private AccountJpaRepository jpaRepository;  // PROHIBIDO en tests unitarios

// ✅ SIEMPRE — mockear
@Mock
private AccountRepositoryPort accountRepository;

// ❌ NUNCA — APIs reales
var response = new RestTemplate().getForObject("https://api.example.com", String.class);

// ✅ SIEMPRE — mockear
@Mock
private ExternalServiceClient externalService;
```

## Prohibiciones Absolutas

❌ Tests que dependen del orden de ejecución  
❌ Llamadas reales a bases de datos o APIs  
❌ `System.out.println` permanentes (usar Logging vía Mockito)  
❌ Lógica condicional dentro de tests (if/else)  
❌ `Thread.sleep()` para sincronización (tests "flaky")  
❌ Tests que pasan ocasionalmente (non-deterministic)  
❌ Compartir estado entre tests (cada test 100% independiente)  
❌ Cobertura < 80% en PR nuevo (auto-rechazado)  
❌ Uso de `@SpringBootTest` en tests unitarios (usar mocks puros)

---

> Para quality gates, pirámide de testing, TDD y Gherkin, ver `[[docs/lineamientos/dev-guidelines.md]]` y `[[docs/lineamientos/qa-guidelines.md]]`.
