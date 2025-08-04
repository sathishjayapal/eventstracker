package me.sathish.event_service.debug;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordHashTest {

    @Test
    void generatePasswordHash() {
        PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

        String password = "password";
        String hash = passwordEncoder.encode(password);

        System.out.println("Password: " + password);
        System.out.println("Generated hash: " + hash);

        // Test if the existing hash matches "password"
        String existingHash = "{bcrypt}$2a$10$FMzmOkkfbApEWxS.4XzCKOR7EbbiwzkPEyGgYh6uQiPxurkpzRMa6";
        boolean matches = passwordEncoder.matches(password, existingHash);
        System.out.println("Does existing hash match 'password'? " + matches);

        // Try some common passwords
        String[] commonPasswords = {"password", "pass", "admin", "test", "123456"};
        for (String pwd : commonPasswords) {
            boolean match = passwordEncoder.matches(pwd, existingHash);
            System.out.println("Does existing hash match '" + pwd + "'? " + match);
        }
    }
}
