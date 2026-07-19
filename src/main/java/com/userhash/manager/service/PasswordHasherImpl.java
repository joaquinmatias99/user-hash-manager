package com.userhash.manager.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;

@Component
public class PasswordHasherImpl implements PasswordHasher {

    private static final Logger logger = LoggerFactory.getLogger(PasswordHasherImpl.class);

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

    /**
     * Paso 3: Evaluar si el hash actual requiere actualizacion de costo.
     * Lee la cadena de BCrypt (MCF) y extrae el costo del usuario para compararlo con el costo actual.
     */
    @Override
    public boolean isUpgradeRequired(String expectedHash) {
        if (expectedHash == null || expectedHash.length() < 6 || !expectedHash.startsWith("$2")) {
            return false;
        }
        
        try {
            // El costo en formato BCrypt se encuentra en los caracteres de los indices 4 y 5
            String costString = expectedHash.substring(4, 6);
            int hashCost = Integer.parseInt(costString);
            
            // Si el costo guardado es menor que el configurado en el servidor, requiere actualizacion
            return hashCost < BCRYPT_COST;
        } catch (Exception e) {
            return false;
        }
    }
}
