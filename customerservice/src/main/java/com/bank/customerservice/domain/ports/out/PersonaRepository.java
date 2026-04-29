package com.bank.customerservice.domain.ports.out;

import com.bank.customerservice.domain.model.Persona;

public interface PersonaRepository {
    
    Persona guardar(Persona persona);
    
    Persona buscarPorId(Long id);
    
    Persona buscarPorIdentificacion(String identificacion);
    
    boolean existePorIdentificacion(String identificacion);
    
    Persona actualizar(Persona persona);
    
    java.util.List<Persona> obtenerTodas();
    
    void eliminarPorId(Long id);
}
