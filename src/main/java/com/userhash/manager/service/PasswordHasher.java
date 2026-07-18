package com.userhash.manager.service;

public interface PasswordHasher {
    /**
     * Hashea la contrasena usando el algoritmo BCrypt.
     * Genera una sal de forma interna y la incluye dentro del hash devuelto en formato MCF.
     */
    String hashPassword(String password);

    /**
     * Verifica si la contrasena ingresada coincide con el hash almacenado en formato BCrypt.
     */
    boolean verifyPassword(String password, String expectedHash);
}
