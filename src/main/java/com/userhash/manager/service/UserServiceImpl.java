package com.userhash.manager.service;

import com.userhash.manager.exception.ResourceNotFoundException;
import com.userhash.manager.model.UserEntity;
import com.userhash.manager.model.UserDTO;
import com.userhash.manager.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    public UserServiceImpl(UserRepository userRepository, PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    @Override
    @Transactional
    public UserDTO register(UserDTO userDTO) {
        logger.info("[UserService] Registrando usuario: {}", userDTO.getUsername());

        // Validar si el usuario ya existe
        if (userRepository.findByUsername(userDTO.getUsername()).isPresent()) {
            throw new IllegalArgumentException("El nombre de usuario ya está registrado: " + userDTO.getUsername());
        }

        // 1. Hashear la contraseña usando BCrypt (la sal se autogenera y se incluye internamente en el hash)
        String passwordHash = passwordHasher.hashPassword(userDTO.getPassword());
        logger.info("[UserService] Hash BCrypt generado para {}: {}", userDTO.getUsername(), passwordHash);

        // 2. Crear y guardar la entidad UserEntity (sin columna salt separada)
        UserEntity userEntity = new UserEntity(
                userDTO.getUsername(),
                passwordHash,
                userDTO.getPassword(), // rawPasswordDebugOnly (para fines didácticos)
                userDTO.getEmail(),
                LocalDateTime.now()
        );

        UserEntity savedUser = userRepository.save(userEntity);

        return new UserDTO(
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getCreatedAt()
        );
    }

    @Override
    public String login(String username, String password) {
        logger.info("[UserService] Intento de login para usuario: {}", username);

        // 1. Buscar al usuario en el repositorio por su username
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));

        // 2. Comprobar si la contraseña ingresada coincide con el hash BCrypt guardado
        boolean isValid = passwordHasher.verifyPassword(password, user.getPasswordHash());

        if (!isValid) {
            logger.warn("[UserService] Contraseña incorrecta para el usuario: {}", username);
            throw new IllegalArgumentException("Contraseña incorrecta. Credenciales inválidas.");
        }

        logger.info("[UserService] Autenticación exitosa para el usuario: {}", username);
        return "Sesión iniciada correctamente para el usuario: " + username;
    }

    @Override
    public UserDTO getByUsername(String username) {
        logger.info("[UserService] Buscando perfil de usuario: {}", username);

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));

        return new UserDTO(
                user.getUsername(),
                user.getEmail(),
                user.getCreatedAt()
        );
    }

    @Override
    @Transactional
    public void deleteUser(String username) {
        logger.info("[UserService] Eliminando usuario: {}", username);
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));

        userRepository.delete(user);
    }

    @Override
    @Transactional
    public UserDTO update(String username, UserDTO userDTO) {
        logger.info("[UserService] Actualizando usuario: {}", username);
        
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));

        // Actualizar email si se provee
        if (userDTO.getEmail() != null) {
            user.setEmail(userDTO.getEmail());
        }

        // Actualizar contraseña si se provee (genera un nuevo hash BCrypt completo con nueva sal interna)
        if (userDTO.getPassword() != null && !userDTO.getPassword().trim().isEmpty()) {
            String newHash = passwordHasher.hashPassword(userDTO.getPassword());
            logger.info("[UserService] Nuevo hash BCrypt generado para la actualización: {}", newHash);
            
            user.setPasswordHash(newHash);
            user.setRawPasswordDebugOnly(userDTO.getPassword());
        }

        // Registrar la fecha de actualización
        user.setUpdatedAt(LocalDateTime.now());
        
        UserEntity updatedUser = userRepository.save(user);
        
        return new UserDTO(
                updatedUser.getUsername(),
                updatedUser.getEmail(),
                updatedUser.getCreatedAt()
        );
    }

    @Override
    public List<UserDTO> getAllUsers() {
        logger.info("[UserService] Obteniendo lista de todos los usuarios");
        
        List<UserEntity> users = userRepository.findAll();
        
        return users.stream()
                .map(user -> new UserDTO(user.getUsername(), user.getEmail(), user.getCreatedAt()))
                .collect(Collectors.toList());
    }
}
