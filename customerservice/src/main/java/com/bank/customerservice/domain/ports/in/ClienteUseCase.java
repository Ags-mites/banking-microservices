package com.bank.customerservice.domain.ports.in;

import java.util.List;
import com.bank.customerservice.application.dto.ClienteCreateRequest;
import com.bank.customerservice.application.dto.ClienteUpdateRequest;
import com.bank.customerservice.application.dto.ClientePatchRequest;
import com.bank.customerservice.application.dto.ClienteResponse;

public interface ClienteUseCase {
    
    ClienteResponse crearCliente(ClienteCreateRequest request);
    
    ClienteResponse obtenerClientePorId(Long id);
    
    List<ClienteResponse> obtenerTodos();
    
    ClienteResponse actualizarCliente(Long id, ClienteUpdateRequest request);
    
    ClienteResponse actualizarClienteParcialmente(Long id, ClientePatchRequest request);
    
    void eliminarCliente(Long id);
}
