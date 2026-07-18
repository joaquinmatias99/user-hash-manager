package com.userhash.manager;

import com.userhash.manager.model.UserDTO;
import com.userhash.manager.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserHashManagerApplicationTests {

    @Autowired
    private UserService userService;

    @Test
    void contextLoads() {
    }

    @Test
    void testUserRegistrationLoginUpdateAndGetAll() {
        String username = "testuser";
        String password = "password123";
        String email = "test@example.com";

        UserDTO dto = new UserDTO();
        dto.setUsername(username);
        dto.setPassword(password);
        dto.setEmail(email);

        // 1. Registrar usuario
        UserDTO registered = userService.register(dto);
        assertNotNull(registered);
        assertEquals(username, registered.getUsername());
        assertEquals(email, registered.getEmail());

        // 2. Verificar que se puede obtener la lista completa y que contiene al usuario
        List<UserDTO> allUsers = userService.getAllUsers();
        assertFalse(allUsers.isEmpty());
        assertTrue(allUsers.stream().anyMatch(u -> u.getUsername().equals(username)));

        // 3. Login exitoso con contraseña inicial
        String loginMessage = userService.login(username, password);
        assertNotNull(loginMessage);
        assertTrue(loginMessage.contains("Sesión iniciada correctamente"));

        // 4. Actualizar usuario (nueva contraseña y nuevo email)
        String newPassword = "newsecurepassword456";
        String newEmail = "newemail@example.com";
        UserDTO updateDTO = new UserDTO();
        updateDTO.setPassword(newPassword);
        updateDTO.setEmail(newEmail);

        UserDTO updated = userService.update(username, updateDTO);
        assertNotNull(updated);
        assertEquals(newEmail, updated.getEmail());

        // 5. Verificar login con la nueva contraseña
        String loginMessageAfterUpdate = userService.login(username, newPassword);
        assertNotNull(loginMessageAfterUpdate);
        assertTrue(loginMessageAfterUpdate.contains("Sesión iniciada correctamente"));

        // 6. Verificar que el login con la vieja contraseña ahora falla
        assertThrows(IllegalArgumentException.class, () -> {
            userService.login(username, password);
        });

        // 7. Eliminar usuario de prueba
        userService.deleteUser(username);
    }
}
