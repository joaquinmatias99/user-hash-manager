package com.userhash.manager.service;

import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;

@Component
public class PasswordHasherImpl implements PasswordHasher {

    /**
     * Paso 1: Hashear la contrasena con BCrypt y medir el tiempo de computo.
     */
    @Override
    public String hashPassword(String password) {
        if (password == null) {
            throw new IllegalArgumentException("La contrasena no puede ser nula");
        }
        
        long startTime = System.currentTimeMillis();
        String hash = BCrypt.hashpw(password, BCrypt.gensalt(10));
        long durationMs = System.currentTimeMillis() - startTime;
        
        // Log didactico de descomposicion de BCrypt en consola
        try {
            String version = hash.substring(0, 4);
            String cost = hash.substring(4, 6);
            String salt = hash.substring(7, 29);
            String rawHash = hash.substring(29);
            
            System.out.println("----------------------------------------------------------------");
            System.out.println("[BCrypt Debug] Descomposicion del Hash Generado:");
            System.out.println("-> Hash Completo: " + hash);
            System.out.println("-> Version del Algoritmo: " + version);
            System.out.println("-> Factor de Costo: " + cost + " (2^" + cost + " = 1024 iteraciones)");
            System.out.println("-> Sal (Salt) Embebida (22 caracteres): " + salt);
            System.out.println("-> Firma del Hash (31 caracteres): " + rawHash);
            System.out.println("-> Tiempo de Computo del Hash: " + durationMs + " ms");
            System.out.println("----------------------------------------------------------------");
        } catch (Exception e) {
            // Se previene cualquier fallo de parsing de cadena para no alterar la ejecucion
        }
        
        return hash;
    }

    /**
     * Paso 2: Verificar la contrasena con BCrypt y medir el tiempo de computo.
     */
    @Override
    public boolean verifyPassword(String password, String expectedHash) {
        if (password == null || expectedHash == null) {
            return false;
        }
        
        try {
            long startTime = System.currentTimeMillis();
            boolean isValid = BCrypt.checkpw(password, expectedHash);
            long durationMs = System.currentTimeMillis() - startTime;
            
            System.out.println("----------------------------------------------------------------");
            System.out.println("[BCrypt Debug] Verificacion del Hash (Login):");
            System.out.println("-> Resultado: " + (isValid ? "Credenciales Validas" : "Credenciales Invalidas"));
            System.out.println("-> Tiempo de Computo del Hash: " + durationMs + " ms");
            System.out.println("----------------------------------------------------------------");
            
            return isValid;
        } catch (Exception e) {
            return false;
        }
    }
}
