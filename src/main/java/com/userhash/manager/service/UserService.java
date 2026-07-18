package com.userhash.manager.service;

import com.userhash.manager.model.UserDTO;

import java.util.List;

public interface UserService {
    UserDTO register(UserDTO userDTO);
    String login(String username, String password);
    UserDTO getByUsername(String username);
    void deleteUser(String username);
    UserDTO update(String username, UserDTO userDTO);
    List<UserDTO> getAllUsers();
}
