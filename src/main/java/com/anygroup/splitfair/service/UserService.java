package com.anygroup.splitfair.service;

import com.anygroup.splitfair.dto.UserDTO;
import java.util.List;
import java.util.UUID;

public interface UserService {
    List<UserDTO> getAllUsers();
    UserDTO getUserById(UUID id);
    UserDTO createUser(UserDTO dto);
    UserDTO updateUser(UserDTO dto);
    void deleteUser(UUID id);
    List<UserDTO> searchUsers(String keyword);
}