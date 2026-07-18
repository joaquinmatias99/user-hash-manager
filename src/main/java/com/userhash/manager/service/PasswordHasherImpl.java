package com.userhash.manager.service;

import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;

@Component
public class PasswordHasherImpl implements PasswordHasher {

    /**
     * Paso 1: Hashear la contrasena con BCrypt.
     * BCrypt genera internamente una sal aleatoria de 22 caracteres
     * y realiza 2^10 (1024) iteraciones de hasheo por defecto (costo = 10).
     * Devuelve una unica cadena de texto que contiene tanto el costo, la sal como el hash final.
     * Adicionalmente, imprime en consola la descomposicion didactica del hash.
     */
    @Override
    public String hashPassword(String password) {
        if (password == null) {
            throw new IllegalArgumentException("La contrasena no puede ser nula");
        }
        
        String hash = BCrypt.hashpw(password, BCrypt.gensalt(10));
        
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
            System.out.println("----------------------------------------------------------------");
        } catch (Exception e) {
            // Se previene cualquier fallo de parsing de cadena para no alterar la ejecucion
        }
        
        return hash;
    }

    /**
     * Paso 2: Verificar la contrasena.
     * BCrypt extrae automaticamente los metadatos y la sal
     * de la cadena "expectedHash" guardada en la base de datos para comparar con la contraseña ingresada.
     */
    @Override
    public boolean verifyPassword(String password, String expectedHash) {
        if (password == null || expectedHash == null) {
            return false;
        }
        
        try {
            return BCrypt.checkpw(password, expectedHash);
        } catch (Exception e) {
            return false;
        }
    }
}
