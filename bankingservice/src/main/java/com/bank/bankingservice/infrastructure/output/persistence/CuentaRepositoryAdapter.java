package com.bank.bankingservice.infrastructure.output.persistence;

import com.bank.bankingservice.domain.model.Cuenta;
import com.bank.bankingservice.domain.ports.out.CuentaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Repository
public class CuentaRepositoryAdapter implements CuentaRepository {

    private final SpringDataCuentaRepository springDataRepository;
    private final CuentaMapper mapper;

    public CuentaRepositoryAdapter(SpringDataCuentaRepository springDataRepository, CuentaMapper mapper) {
        this.springDataRepository = springDataRepository;
        this.mapper = mapper;
    }

    @Override
    public Cuenta save(Cuenta cuenta) {
        CuentaEntity entity = mapper.toEntity(cuenta);
        CuentaEntity savedEntity = springDataRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public List<Cuenta> findAll() {
        return StreamSupport.stream(springDataRepository.findAll().spliterator(), false)
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Cuenta> findById(Long id) {
        return springDataRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public boolean existsByNumeroCuenta(String numeroCuenta) {
        return springDataRepository.existsByNumeroCuenta(numeroCuenta);
    }

    @Override
    public void deleteById(Long id) {
        springDataRepository.deleteById(id);
    }
}
