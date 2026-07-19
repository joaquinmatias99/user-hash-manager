package com.userhash.manager.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;

@Component
public class PasswordHasherImpl implements PasswordHasher {

    private static final Logger logger = LoggerFactory.getLogger(PasswordHasherImpl.class);

    // Cambia este valor de 1 en 1 para calibrar el tiempo de computo en tu hardware.
    // Rango sugerido: entre 10 y 14.
    private static final int BCRYPT_COST = 12;

    /**
     * Paso 1: Hashear la contrasena con BCrypt y medir el tiempo de computo.
     */
    @Override
    public String hashPassword(String password) {
        if (password == null) {
            throw new IllegalArgumentException("La contrasena no puede ser nula");
        }
        
        long startTime = System.currentTimeMillis();
        String hash = BCrypt.hashpw(password, BCrypt.gensalt(BCRYPT_COST));
        long durationMs = System.currentTimeMillis() - startTime;
        
        // Log didactico de descomposicion de BCrypt en consola
        try {
            String version = hash.substring(0, 4);
            String cost = hash.substring(4, 6);
            String salt = hash.substring(7, 29);
            String rawHash = hash.substring(29);
            
            // Usamos el logger con un unico string multilinea para no repetir el prefijo de Spring en cada renglon
            logger.info("\n----------------------------------------------------------------" +
                        "\n[BCrypt Debug] Descomposicion del Hash Generado:" +
                        "\n-> Hash Completo: {}" +
                        "\n-> Version del Algoritmo: {}" +
                        "\n-> Factor de Costo: {} (2^{} = {} iteraciones)" +
                        "\n-> Sal (Salt) Embebida (22 caracteres): {}" +
                        "\n-> Firma del Hash (31 caracteres): {}" +
                        "\n-> Tiempo de Computo del Hash: {} ms" +
                        "\n----------------------------------------------------------------",
                        hash, version, cost, cost, (int) Math.pow(2, Integer.parseInt(cost)), salt, rawHash, durationMs);
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
            
            logger.info("\n----------------------------------------------------------------" +
                        "\n[BCrypt Debug] Verificacion del Hash (Login):" +
                        "\n-> Resultado: {}" +
                        "\n-> Tiempo de Computo del Hash: {} ms" +
                        "\n----------------------------------------------------------------",
                        isValid ? "Credenciales Validas" : "Credenciales Invalidas", durationMs);
            
            return isValid;
        } catch (Exception e) {
            return false;
        }
    }
}
