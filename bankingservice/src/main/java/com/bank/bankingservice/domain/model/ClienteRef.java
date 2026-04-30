package com.bank.bankingservice.domain.model;

import java.time.Instant;

public class ClienteRef {
    private Long id;
    private Long clienteId;
    private String nombre;
    private String identificacion;
    private Integer version;
    private Instant createdAt;
    private Instant updatedAt;

    private ClienteRef() {
    }

    public static ClienteRef reconstituir(Long id,
                                          Long clienteId,
                                          String nombre,
                                          String identificacion,
                                          Integer version,
                                          Instant createdAt,
                                          Instant updatedAt) {
        ClienteRef clienteRef = new ClienteRef();
        clienteRef.id = id;
        clienteRef.clienteId = clienteId;
        clienteRef.nombre = nombre;
        clienteRef.identificacion = identificacion;
        clienteRef.version = version;
        clienteRef.createdAt = createdAt;
        clienteRef.updatedAt = updatedAt;
        return clienteRef;
    }

    public Long id() {
        return id;
    }

    public Long clienteId() {
        return clienteId;
    }

    public String nombre() {
        return nombre;
    }

    public String identificacion() {
        return identificacion;
    }

    public Integer version() {
        return version;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}