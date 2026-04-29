package com.bank.customerservice.domain.model;

import java.time.Instant;

public class Cliente {
    
    public interface PasswordHasher {
        String encode(String rawPassword);
        boolean matches(String rawPassword, String encodedPassword);
    }
    
    private Long id;
    private Long personaId;
    private String contrasena;
    private Boolean estado;
    private Instant createdAt;
    private Instant updatedAt;
    
    private Cliente() {}
    
    public static Cliente crear(Long personaId, String contrasena, Boolean estado, PasswordHasher hasher) {
        validarContrasena(contrasena);
        
        Cliente cliente = new Cliente();
        cliente.personaId = personaId;
        cliente.contrasena = hasher.encode(contrasena);
        cliente.estado = estado != null ? estado : true;
        cliente.createdAt = Instant.now();
        cliente.updatedAt = Instant.now();
        
        return cliente;
    }
    
    public static Cliente crearDesdeRepository(Long id, Long personaId, String contrasena, Boolean estado, Instant createdAt, Instant updatedAt) {
        Cliente cliente = new Cliente();
        cliente.id = id;
        cliente.personaId = personaId;
        cliente.contrasena = contrasena;
        cliente.estado = estado;
        cliente.createdAt = createdAt;
        cliente.updatedAt = updatedAt;
        return cliente;
    }
    
    public void actualizar(String contrasena, Boolean estado, PasswordHasher hasher) {
        if (contrasena != null) {
            validarContrasena(contrasena);
            this.contrasena = hasher.encode(contrasena);
        }
        if (estado != null) {
            this.estado = estado;
        }
        this.updatedAt = Instant.now();
    }
    
    public void cambiarEstado(Boolean nuevoEstado) {
        if (nuevoEstado != null) {
            this.estado = nuevoEstado;
            this.updatedAt = Instant.now();
        }
    }
    
    public boolean estaActivo() {
        return estado != null && estado;
    }
    
    // ─── Validaciones ──────────────────────────────────────────────────
    
    private static void validarContrasena(String contrasena) {
        if (contrasena == null || contrasena.isBlank()) {
            throw new IllegalArgumentException("La contraseña es obligatoria");
        }
        if (contrasena.length() < 8) {
            throw new IllegalArgumentException("La contraseña debe tener mínimo 8 caracteres");
        }
    }
    
    // ─── Getters ───────────────────────────────────────────────────────
    
    public Long id() { return id; }
    public Long personaId() { return personaId; }
    public String contrasena() { return contrasena; }
    public Boolean estado() { return estado; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
    
    // ─── Setters (para infraestructura/persistencia) ────────────────────
    
    public void setId(Long id) { this.id = id; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
