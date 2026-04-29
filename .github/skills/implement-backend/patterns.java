/*
 * patterns.java — Patrones de referencia AGNÓSTICOS para Backend Developer
 * Spring Boot 4 / Java 21 con Arquitectura Hexagonal PURA
 * 
 * 📌 PRINCIPIO: Domain = Java PURO. Sin ningún import de framework.
 *     Las anotaciones van exclusivamente en Infrastructure y Application (para wiring).
 * 
 * Estructura Hexagonal:
 *   domain/       → Java PURO (POJO) — SIN Spring, SIN JPA, SIN nada de framework
 *   application/  → DTOs + UseCases — AQUÍ SÍ puede haber @Service (wiring)
 *   infrastructure/ → JPA Entities, Controllers, Adapters — Anotaciones de framework
 */

// ╔══════════════════════════════════════════════════════════════════════════════════════════╗
// ║                    ARQUITECTURA HEXAGONAL PURA                               ║
// ╠══════════════════════════════════════════════════════════════════════════════════════════╣
// ║                                                                          ║
// ║  DOMAIN/         → Java PURO (POJO) — 0% framework                             ║
// ║                  Solo imports: java.*, excepciones del dominio                 ║
// ║                                                                          ║
// ║  APPLICATION/   → DTOs (Records) + UseCases —aquí SÍ puede usar @Service   ║
// ║                  Solo contratos y orquestación                               ║
// ║                                                                          ║
// ║  INFRASTRUCTURE/ → JPA Entities, Controllers, Repos — 100% framework      ║
// ║                                                                          ║
// ╚══════════════════════════════════════════════════════════════════════════════════╝

package com.example.domain.model;


// ══════════════════════════════════════════════════════════════════════════════════════════════
// DOMAIN LAYER — 100% JAVA PURO (SIN NINGUNA ANOTACIÓN)
// ══════════════════════════════════════════════════════════════════════════════════════════════

// ─── VALUE OBJECT: Money (Inmutable) ──────────────────────────────────────────────────────────────
/**
 * Value Object para dinero — inmutable.
 * 
 * ⚠️ SIN anotaciones. Solo Java puro.
 */
public final class Money {
    public static final Money ZERO = new Money(java.math.BigDecimal.ZERO);
    
    private final java.math.BigDecimal amount;
    private final String currency;
    
    private Money(java.math.BigDecimal amount, String currency) {
        this.amount = amount;
        this.currency = currency;
    }
    
    public static Money of(java.math.BigDecimal amount) {
        return new Money(amount, "USD");
    }
    
    public static Money fromString(String amount) {
        return new Money(new java.math.BigDecimal(amount), "USD");
    }
    
    public Money add(Money other) {
        return new Money(this.amount.add(other.amount), this.currency);
    }
    
    public Money subtract(Money other) {
        return new Money(this.amount.subtract(other.amount), this.currency);
    }
    
    public boolean isZeroOrNegative() {
        return amount.compareTo(java.math.BigDecimal.ZERO) <= 0;
    }
    
    public boolean lessThan(Money other) {
        return this.amount.compareTo(other.amount) < 0;
    }
    
    public boolean greaterThanOrEqual(Money other) {
        return this.amount.compareTo(other.amount) >= 0;
    }
    
    public java.math.BigDecimal amount() { return amount; }
    public String currency() { return currency; }
    
    @Override public String toString() { return currency + " " + amount; }
}

// ─── VALUE OBJECT: Email ───────────────────────────────────────────────────────────────────────
/**
 * Value Object para email — inmutable.
 */
public final class Email {
    private final String value;
    
    private Email(String value) { this.value = value; }
    
    public static Email of(String value) {
        if (value == null || value.isBlank() || !value.contains("@")) {
            throw new IllegalArgumentException("Invalid email format");
        }
        return new Email(value);
    }
    
    public String value() { return value; }
    @Override public String toString() { return value; }
    @Override public boolean equals(Object o) { return o instanceof Email e && value.equals(e.value); }
    @Override public int hashCode() { return value.hashCode(); }
}

// ─── ENTITY: Account (Dominio Puro) ────────────────────────────────────────────────────────────
/**
 * Entity del dominio — lógica de negocio pura.
 * 
 * ⚠️ SIN @Entity, @Table, @Column.
 * ⚠️ Solo imports: java.*, excepciones del dominio
 */
public class Account {
    
    private Long id;
    private String accountNumber;
    private Long clientId;
    private Money balance;
    private AccountType type;
    private java.time.Instant createdAt;
    private java.time.Instant updatedAt;
    private int version;
    
    // Constructor de fábrica (única forma de crear instancias)
    private Account() {}
    
    public static Account create(String accountNumber, Long clientId, AccountType type) {
        if (accountNumber == null || accountNumber.isBlank()) {
            throw new IllegalArgumentException("accountNumber is required");
        }
        Account account = new Account();
        account.accountNumber = accountNumber;
        account.clientId = clientId;
        account.balance = Money.ZERO;
        account.type = type;
        account.createdAt = java.time.Instant.now();
        account.updatedAt = java.time.Instant.now();
        account.version = 0;
        return account;
    }
    
    // ─── Lógica de dominio (pertenece aquí, es negocio puro) ───────────────────────────
    public void deposit(Money amount) {
        if (amount == null || amount.isZeroOrNegative()) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        this.balance = this.balance.add(amount);
        this.updatedAt = java.time.Instant.now();
    }
    
    public void withdraw(Money amount) {
        if (amount == null || amount.isZeroOrNegative()) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (this.balance.lessThan(amount)) {
            throw new DomainException("Insufficient funds. Balance: " + this.balance);
        }
        this.balance = this.balance.subtract(amount);
        this.updatedAt = java.time.Instant.now();
    }
    
    public boolean hasSufficientFunds(Money amount) {
        return this.balance.greaterThanOrEqual(amount);
    }
    
    // Getters
    public Long id() { return id; }
    public String accountNumber() { return accountNumber; }
    public Long clientId() { return clientId; }
    public Money balance() { return balance; }
    public AccountType type() { return type; }
    public java.time.Instant createdAt() { return createdAt; }
    public java.time.Instant updatedAt() { return updatedAt; }
    public int version() { return version; }
    
    // Setters (para infraestructura)
    public void setId(Long id) { this.id = id; }
    public void setBalance(Money balance) { this.balance = balance; }
    public void setUpdatedAt(java.time.Instant updatedAt) { this.updatedAt = updatedAt; }
    public void setVersion(int version) { this.version = version; }
}

// ─── ENUM: AccountType ──────────────────────────────────────────────────────────────────
public enum AccountType {
    SAVINGS,
    CHECKING,
    CREDIT
}

// ─── EXCEPTIONS (Dominio Puro) ──────────────────────────────────────────────────────
public class DomainException extends RuntimeException {
    public DomainException(String message) { super(message); }
}

public class InsufficientFundsException extends DomainException {
    public InsufficientFundsException(String message) { super(message); }
}

public class AccountNotFoundException extends DomainException {
    public AccountNotFoundException(String message) { super(message); }
}


// ══════════════════════════════════════════════════════════════════════════════════════════════
// DOMAIN PORTS — INTERFACES (Java Puro, sin anotaciones)
// ══════════════════════════════════════════════════════════════════════════════════════════════

package com.example.domain.ports.in;

import com.example.domain.model.Account;
import com.example.application.dto.CreateAccountRequest;
import com.example.application.dto.AccountResponse;

/**
 * Input Port — Caso de uso del dominio.
 * 
 * ⚠️ Interfaz Java pura. Sin anotaciones de Spring.
 */
public interface AccountUseCase {
    AccountResponse createAccount(CreateAccountRequest request);
    AccountResponse getAccountById(Long id);
    AccountResponse deposit(Long id, java.math.BigDecimal amount);
    AccountResponse withdraw(Long id, java.math.BigDecimal amount);
}

package com.example.domain.ports.out;

import com.example.domain.model.Account;
import java.util.Optional;
import java.util.List;

/**
 * Output Port — Persistencia.
 * 
 * ⚠️ Interfaz Java pura. Sin JPA.
 */
public interface AccountRepository {
    Account save(Account account);
    Optional<Account> findById(Long id);
    List<Account> findByClientId(Long clientId);
    void delete(Account account);
}


// ══════════════════════════════════════════════════════════════════════════════════════════════
// APPLICATION LAYER — DTOs + USECASES (aquí SÍ puede usar @Service)
// ══════════════════════════════════════════════════════════════════════════════════════════════════════

package com.example.application.dto;

/**
 * DTOs como Records — inmutables.
 * 
 * ⚠️ SIN lógica de negocio. Solo validación de formato.
 */
public record CreateAccountRequest(
    String accountNumber,
    Long clientId,
    String accountType  // "SAVINGS", "CHECKING", "CREDIT"
) {}

public record AccountResponse(
    Long id,
    String accountNumber,
    Long clientId,
    String balance,
    String accountType,
    String createdAt,
    String updatedAt
) {}


package com.example.application.usecase;

import com.example.domain.model.Account;
import com.example.domain.model.AccountType;
import com.example.domain.model.Money;
import com.example.domain.ports.in.AccountUseCase;
import com.example.domain.ports.out.AccountRepository;
import com.example.application.dto.CreateAccountRequest;
import com.example.application.dto.AccountResponse;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;

/**
 * Caso de uso — Orquesta el dominio.
 * 
 * ⚠️ AQUÍ SÍ usa @Service (es Application layer).
 *    Usa los ports para dialogar con infraestructura.
 */
@Service
public class AccountService implements AccountUseCase {
    
    private final AccountRepository repository;
    
    // Constructor injection
    public AccountService(AccountRepository repository) {
        this.repository = repository;
    }
    
    @Override
    public AccountResponse createAccount(CreateAccountRequest request) {
        AccountType type = AccountType.valueOf(request.accountType());
        Account account = Account.create(request.accountNumber(), request.clientId(), type);
        Account saved = repository.save(account);
        return toResponse(saved);
    }
    
    @Override
    public AccountResponse getAccountById(Long id) {
        Account account = repository.findById(id)
            .orElseThrow(() -> new AccountNotFoundException("Account not found: " + id));
        return toResponse(account);
    }
    
    @Override
    public AccountResponse deposit(Long id, BigDecimal amount) {
        Account account = repository.findById(id)
            .orElseThrow(() -> new AccountNotFoundException("Account not found: " + id));
        account.deposit(Money.of(amount));
        Account saved = repository.save(account);
        return toResponse(saved);
    }
    
    @Override
    public AccountResponse withdraw(Long id, BigDecimal amount) {
        Account account = repository.findById(id)
            .orElseThrow(() -> new AccountNotFoundException("Account not found: " + id));
        account.withdraw(Money.of(amount));
        Account saved = repository.save(account);
        return toResponse(saved);
    }
    
    private AccountResponse toResponse(Account account) {
        return new AccountResponse(
            account.id(),
            account.accountNumber(),
            account.clientId(),
            account.balance().toString(),
            account.type().name(),
            account.createdAt().toString(),
            account.updatedAt().toString()
        );
    }
}


// ══════════════════════════════════════════════════════════════════════════════════════════════
// INFRASTRUCTURE LAYER — JPA + SPRING (100% framework)
// ══════════════════════════════════════════════════════════════════════════════════════

package com.example.infrastructure.output;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * JPA Entity — Mapea la entidad de dominio a la tabla de BD.
 * 
 * ⚠️ LAS ANOTACIONES JPA VAN AQUÍ, NO EN DOMAIN/.
 */
@Entity
@Table(name = "accounts", schema = "banking_service")
public class AccountEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "account_number", nullable = false, unique = true)
    private String accountNumber;
    
    @Column(name = "client_id", nullable = false)
    private Long clientId;
    
    @Column(name = "balance", nullable = false)
    private BigDecimal balance;
    
    @Column(name = "account_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private String accountType;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    @Version
    @Column(name = "version")
    private Integer version;
    
    // Mapeo Domain → Entity
    public static AccountEntity fromDomain(Account account) {
        AccountEntity e = new AccountEntity();
        e.setId(account.id());
        e.setAccountNumber(account.accountNumber());
        e.setClientId(account.clientId());
        e.setBalance(new BigDecimal(account.balance().amount().toString()));
        e.setAccountType(account.type().name());
        e.setCreatedAt(account.createdAt());
        e.setUpdatedAt(account.updatedAt());
        return e;
    }
    
    // Mapeo Entity → Domain
    public Account toDomain() {
        Account account = Account.create(this.accountNumber, this.clientId, AccountType.valueOf(this.accountType));
        account.setId(this.id);
        account.setBalance(Money.of(this.balance));
        account.setUpdatedAt(this.updatedAt);
        account.setVersion(this.version);
        return account;
    }
    
    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
}

package com.example.infrastructure.output;

import com.example.domain.model.Account;
import com.example.domain.ports.out.AccountRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

/**
 * Adapter de persistencia — Implementa el puerto de salida.
 */
@Repository
public class AccountRepositoryAdapter implements AccountRepository {
    
    private final AccountJpaRepository jpaRepository;
    
    public AccountRepositoryAdapter(AccountJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }
    
    @Override
    public Account save(Account account) {
        AccountEntity entity = AccountEntity.fromDomain(account);
        return jpaRepository.save(entity).toDomain();
    }
    
    @Override
    public Optional<Account> findById(Long id) {
        return jpaRepository.findById(id).map(AccountEntity::toDomain);
    }
    
    @Override
    public List<Account> findByClientId(Long clientId) {
        return jpaRepository.findByClientId(clientId).stream()
            .map(AccountEntity::toDomain)
            .toList();
    }
    
    @Override
    public void delete(Account account) {
        jpaRepository.delete(AccountEntity.fromDomain(account));
    }
}

package com.example.infrastructure.output;

import com.example.infrastructure.output.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Spring Data JPA Repository.
 */
@Repository
public interface AccountJpaRepository extends JpaRepository<AccountEntity, Long> {
    List<AccountEntity> findByClientId(Long clientId);
}


// ══════════════════════════════════════════════════════════════════════════════════════
// INFRASTRUCTURE LAYER — REST CONTROLLER
// ══════════════════════════════════════════════════════════════════════════════════════

package com.example.infrastructure.input;

import com.example.domain.ports.in.AccountUseCase;
import com.example.application.dto.CreateAccountRequest;
import com.example.application.dto.AccountResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.Map;

/**
 * REST Controller — Adapter de entrada.
 * 
 * ⚠️ Solo routing y validación. SIN lógica de negocio.
 */
@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    
    private final AccountUseCase accountUseCase;
    
    public AccountController(AccountUseCase accountUseCase) {
        this.accountUseCase = accountUseCase;
    }
    
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CreateAccountRequest request) {
        AccountResponse response = accountUseCase.createAccount(request);
        return ResponseEntity.status(201).body(Map.of(
            "data", response,
            "timestamp", Instant.now().toString()
        ));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(accountUseCase.getAccountById(id));
    }
    
    @PostMapping("/{id}/deposit")
    public ResponseEntity<?> deposit(@PathVariable Long id, @RequestParam String amount) {
        AccountResponse response = accountUseCase.deposit(id, new java.math.BigDecimal(amount));
        return ResponseEntity.ok(Map.of(
            "data", response,
            "timestamp", Instant.now().toString()
        ));
    }
    
    @PostMapping("/{id}/withdraw")
    public ResponseEntity<?> withdraw(@PathVariable Long id, @RequestParam String amount) {
        AccountResponse response = accountUseCase.withdraw(id, new java.math.BigDecimal(amount));
        return ResponseEntity.ok(Map.of(
            "data", response,
            "timestamp", Instant.now().toString()
        ));
    }
}


// ══════════════════════════════════════════════════════════════════════════════════════════════
// CONFIG: Wiring (conecta Application con Infrastructure)
// ══════════════════════════════════════════════════════════════════════════════════════════════

package com.example.infrastructure.config;

import com.example.domain.ports.in.AccountUseCase;
import com.example.domain.ports.out.AccountRepository;
import com.example.application.usecase.AccountService;
import com.example.infrastructure.output.AccountJpaRepository;
import com.example.infrastructure.output.AccountRepositoryAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de wiring.
 * 
 * ⚠️Aquí se registran los beans y se conectan las capas.
 */
@Configuration
public class ApplicationConfig {
    
    @Bean
    public AccountRepository accountRepository(AccountJpaRepository jpaRepository) {
        return new AccountRepositoryAdapter(jpaRepository);
    }
    
    @Bean
    public AccountUseCase accountUseCase(AccountRepository repository) {
        return new AccountService(repository);
    }
}