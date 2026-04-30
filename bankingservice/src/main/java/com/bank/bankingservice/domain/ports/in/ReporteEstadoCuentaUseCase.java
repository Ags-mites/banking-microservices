package com.bank.bankingservice.domain.ports.in;

import com.bank.bankingservice.domain.model.MovimientoReporte;

import java.util.List;

public interface ReporteEstadoCuentaUseCase {
    List<MovimientoReporte> generarReporte(Long clienteId, String fecha);
}