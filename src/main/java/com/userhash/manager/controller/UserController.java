package com.userhash.manager.controller;

import com.userhash.manager.model.UserDTO;
import com.userhash.manager.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@RequestBody UserDTO userDTO) {
        UserDTO registeredUser = userService.register(userDTO);
        return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        if (username == null || password == null) {
            throw new IllegalArgumentException("Se requiere proporcionar 'username' y 'password'.");
        }

        String loginMessage = userService.login(username, password);

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", loginMessage);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{username}")
    public ResponseEntity<UserDTO> getProfile(@PathVariable String username) {
        UserDTO userDTO = userService.getByUsername(username);
        return ResponseEntity.ok(userDTO);
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable String username) {
        userService.deleteUser(username);
        
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Usuario eliminado correctamente de la base de datos.");
        
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{username}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable String username, @RequestBody UserDTO userDTO) {
        UserDTO updatedUser = userService.update(username, userDTO);
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
}
