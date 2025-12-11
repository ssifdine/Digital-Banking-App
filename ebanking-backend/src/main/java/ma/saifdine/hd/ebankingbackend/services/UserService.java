package ma.saifdine.hd.ebankingbackend.services;

import ma.saifdine.hd.ebankingbackend.dtos.ChangePasswordRequestDTO;
import ma.saifdine.hd.ebankingbackend.dtos.ResetPasswordAdminDTO;
import ma.saifdine.hd.ebankingbackend.dtos.UserRequestDTO;
import ma.saifdine.hd.ebankingbackend.dtos.UserResponseDTO;

import java.util.List;

public interface UserService {

    UserResponseDTO createUser(UserRequestDTO userRequestDTO);

    UserResponseDTO getUserByUsername(String username);

    UserResponseDTO getUserById(long id);

    UserResponseDTO updateUser(Long id,UserRequestDTO userRequestDTO);

    void deleteUser(Long id);

    List<UserResponseDTO> getAllUsers();

    void changePassword(String username, ChangePasswordRequestDTO changePasswordRequestDTO);

    void adminResetPassword(Long id, ResetPasswordAdminDTO passwordAdminDTO);

}
