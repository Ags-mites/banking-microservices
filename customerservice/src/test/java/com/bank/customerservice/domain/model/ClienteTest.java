package com.bank.customerservice.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ClienteTest {

    private final Cliente.PasswordHasher dummyHasher = new Cliente.PasswordHasher() {
        @Override
        public String encode(String rawPassword) {
            return "encoded_" + rawPassword;
        }

        @Override
        public boolean matches(String rawPassword, String encodedPassword) {
            return encode(rawPassword).equals(encodedPassword);
        }
    };

    @Test
    public void testCrearClienteValido() {
        Long personaId = 1L;
        String contrasenaValida = "mypassword123";
        Boolean estado = true;

        Cliente cliente = Cliente.crear(personaId, contrasenaValida, estado, dummyHasher);

        assertNotNull(cliente);
        assertEquals(personaId, cliente.personaId());
        assertEquals("encoded_mypassword123", cliente.contrasena());
        assertTrue(cliente.estaActivo());
        assertNotNull(cliente.createdAt());
        assertNotNull(cliente.updatedAt());
    }

    @Test
    public void testCrearClienteContrasenaCortaLanzaExcepcion() {
        Long personaId = 1L;
        String contrasenaInvalida = "123"; 
        Boolean estado = true;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            Cliente.crear(personaId, contrasenaInvalida, estado, dummyHasher)
        );

        assertEquals("La contraseña debe tener mínimo 8 caracteres", exception.getMessage());
    }

    @Test
    public void testActualizarCliente() {
        Cliente cliente = Cliente.crear(1L, "password", true, dummyHasher);
        
        cliente.actualizar("newpassword", false, dummyHasher);
        
        assertEquals("encoded_newpassword", cliente.contrasena());
        assertFalse(cliente.estaActivo());
    }
}
