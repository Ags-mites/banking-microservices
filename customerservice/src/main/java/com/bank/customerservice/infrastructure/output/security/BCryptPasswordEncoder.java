package com.bank.customerservice.infrastructure.output.security;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Component;
import com.bank.customerservice.domain.model.Cliente.PasswordHasher;

@Component
public class BCryptPasswordEncoder implements PasswordHasher {
    
    @Override
    public String encode(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt(12));
    }
    
    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        return BCrypt.checkpw(rawPassword, encodedPassword);
    }
}