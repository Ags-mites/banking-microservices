package com.bank.customerservice.domain.model;

import java.time.Instant;

public class Persona {

    private Long id;
    protected String nombre;
    protected Genero genero;
    protected Integer edad;
    protected String identificacion;
    protected String direccion;
    protected String telefono;
    protected Instant createdAt;
    protected Instant updatedAt;

    protected Persona() {}

    protected Persona(String nombre, Genero genero, Integer edad, String identificacion,
                      String direccion, String telefono) {
        validarNombre(nombre);
        validarIdentificacion(identificacion);
        this.nombre = nombre;
        this.genero = genero;
        this.edad = edad;
        this.identificacion = identificacion;
        this.direccion = direccion;
        this.telefono = telefono;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public static Persona crear(String nombre, Genero genero, Integer edad, String identificacion,
                                String direccion, String telefono) {
        return new Persona(nombre, genero, edad, identificacion, direccion, telefono);
    }
    
    public void actualizar(
        String nombre,
        Genero genero,
        Integer edad,
        String identificacion,
        String direccion,
        String telefono
    ) {
        if (nombre != null) {
            validarNombre(nombre);
            this.nombre = nombre;
        }
        if (genero != null) {
            this.genero = genero;
        }
        if (edad != null) {
            this.edad = edad;
        }
        if (identificacion != null) {
            validarIdentificacion(identificacion);
            this.identificacion = identificacion;
        }
        if (direccion != null) {
            this.direccion = direccion;
        }
        if (telefono != null) {
            this.telefono = telefono;
        }
        this.updatedAt = Instant.now();
    }
    
    // ─── Validaciones ──────────────────────────────────────────────────
    
    private static void validarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        if (nombre.length() > 100) {
            throw new IllegalArgumentException("El nombre no puede exceder 100 caracteres");
        }
    }
    
    private static void validarIdentificacion(String identificacion) {
        if (identificacion == null || identificacion.isBlank()) {
            throw new IllegalArgumentException("La identificación es obligatoria");
        }
        if (identificacion.length() > 50) {
            throw new IllegalArgumentException("La identificación no puede exceder 50 caracteres");
        }
    }
    
    // ─── Getters ───────────────────────────────────────────────────────
    
    public Long id() { return id; }
    public String nombre() { return nombre; }
    public Genero genero() { return genero; }
    public Integer edad() { return edad; }
    public String identificacion() { return identificacion; }
    public String direccion() { return direccion; }
    public String telefono() { return telefono; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
    
    // ─── Setters (para infraestructura/persistencia) ────────────────────
    
    public void setId(Long id) { this.id = id; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
