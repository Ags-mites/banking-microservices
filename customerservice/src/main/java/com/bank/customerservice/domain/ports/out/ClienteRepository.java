package com.bank.customerservice.domain.ports.out;

import com.bank.customerservice.domain.model.Cliente;

public interface ClienteRepository {
    
    Cliente guardar(Cliente cliente);
    
    Cliente buscarPorId(Long id);
    
    Cliente buscarPorPersonaId(Long personaId);
    
    java.util.List<Cliente> obtenerTodos();
    
    Cliente actualizar(Cliente cliente);
    
    void eliminarPorId(Long id);
    
    boolean existePorId(Long id);
}
