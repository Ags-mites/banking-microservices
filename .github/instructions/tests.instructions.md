---
applyTo: "src/test/java/**/*.java,frontend/src/__tests__/**/*.{js,jsx}"
---

> **Scope**: Las reglas de backend aplican a proyectos con tests en Java (Spring Boot 3.x + JUnit 5); las de frontend aplican a proyectos con tests en JS/JSX. En proyectos con otro stack, adaptar las herramientas y convenciones manteniendo los principios (independencia, aislamiento, AAA, cobertura ≥ 80%).

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

## Frontend (Vitest + React Testing Library)

### Framework Obligatorio
- **Test Framework**: Vitest
- **Rendering**: React Testing Library
- **Mocking**: `vi.mock()`
- **Assertions**: `@testing-library/jest-dom`

### Estructura AAA

```jsx
describe('AccountForm', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });
  
  it('renders submit button when form is valid', () => {
    // ARRANGE
    const mockOnSubmit = vi.fn();
    const { getByRole, getByLabelText } = render(<AccountForm onSubmit={mockOnSubmit} />);
    
    // ACT
    const accountInput = getByLabelText(/account number/i);
    const submitButton = getByRole('button', { name: /submit/i });
    
    fireEvent.change(accountInput, { target: { value: 'ACC-001' } });
    fireEvent.click(submitButton);
    
    // ASSERT
    expect(mockOnSubmit).toHaveBeenCalledWith(
      expect.objectContaining({ accountNumber: 'ACC-001' })
    );
    expect(mockOnSubmit).toHaveBeenCalledTimes(1);
  });
  
  it('disables submit button when form has errors', () => {
    // ARRANGE
    const { getByRole } = render(<AccountForm />);
    
    // ACT
    const submitButton = getByRole('button', { name: /submit/i });
    
    // ASSERT
    expect(submitButton).toBeDisabled();
  });
});
```

### Convenciones de Nomenclatura

- **Describe**: nombre del componente/hook
- **It/Test**: `{verbo} {qué hace} {condición}`
  - ✅ `renders submit button when form is valid`
  - ✅ `displays error message on network failure`
  - ❌ `test renders`, ❌ `AccountFormTest`, ❌ `test1`

## Estructura de Archivos Obligatoria

```
src/test/java/com/example/<service>/
  domain/service/
    AccountServiceTests.java
  infrastructure/input/
    AccountControllerTests.java
  infrastructure/output/
    AccountRepositoryAdapterTests.java

frontend/src/__tests__/
  components/[ComponentName].test.jsx
  hooks/use[HookName].test.js
  services/[ServiceName].test.js
```

## Cobertura ≥ 80% — BLOQUEANTE (PR Auto-Rechazado)

**Reglas Críticas**:
- ✅ PR aprovado si: Cobertura ≥ 80% en TODAS las rutas
- ❌ PR rechazado si: Cobertura < 80% en CUALQUIER archivo nuevo o modificado
- ❌ PR rechazado si: Cobertura de Domain < 85%
- ❌ PR rechazado si: Cobertura de Service Handlers < 85%
- ❌ PR rechazado si: Cobertura de Controllers < 80%

**Por Capa** (mínimos innegociables):

| Capa | Cobertura mínima | Justificación |
|------|------------------|---------------|
| Domain (entidades, lógica) | 85% | Lógica pura, alta criticidad |
| Application (dto, mappers) | 85% | High-risk workflows |
| Infrastructure (repos, configs) | 75% | Lower risk, repetitive |
| API (Controllers, endpoints) | 80% | Routing, validation |
| Frontend (Components, hooks) | 80% | UI logic |

**Medición en Pipeline**:

```bash
# Backend — JUnit 5 + JaCoCo
./mvnw verify

# Verificar cobertura mínima
./mvnw verify -Djacoco.min.coverage=0.80
# Si coverage < 80%, exit 1

# Frontend — Vitest
npm run test:coverage

# Si coverage < 80%, exit 1
```

**GitHub Actions Integration**:

```yaml
- name: Check Coverage ≥ 80%
  run: |
    ./mvnw verify
    COVERAGE=$(cat target/site/jacoco/index.xml | grep -oP 'LINE.*?"counter" value="\K[0-9.]+' | head -1)
    if (( $(echo "$COVERAGE < 80" | bc -l) )); then
      echo "❌ Coverage $COVERAGE% < 80% — PR REJECTED"
      exit 1
    fi
    echo "✅ Coverage $COVERAGE% >= 80% — PR APPROVED"
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
