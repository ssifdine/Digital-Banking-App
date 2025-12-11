package ma.saifdine.hd.ebankingbackend.services;

import lombok.AllArgsConstructor;
import ma.saifdine.hd.ebankingbackend.dtos.ChangePasswordRequestDTO;
import ma.saifdine.hd.ebankingbackend.dtos.ResetPasswordAdminDTO;
import ma.saifdine.hd.ebankingbackend.dtos.UserRequestDTO;
import ma.saifdine.hd.ebankingbackend.dtos.UserResponseDTO;
import ma.saifdine.hd.ebankingbackend.entities.AppRole;
import ma.saifdine.hd.ebankingbackend.entities.AppUser;
import ma.saifdine.hd.ebankingbackend.enums.RoleName;
import ma.saifdine.hd.ebankingbackend.mappers.UserMapperImpl;
import ma.saifdine.hd.ebankingbackend.repositories.AppRoleRepository;
import ma.saifdine.hd.ebankingbackend.repositories.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final PasswordEncoder passwordEncoder;
    private AppUserRepository appUserRepository;
    private AppRoleRepository appRoleRepository;
    private UserMapperImpl userMapper;


    @Override
    public UserResponseDTO createUser(UserRequestDTO userRequestDTO) {
        // Vérifier si l’utilisateur existe déjà
        if (appUserRepository.findByUsername(userRequestDTO.getUsername()).isPresent()) {
            throw new RuntimeException("Username already taken");
        }

        AppUser user = userMapper.toEntity(userRequestDTO);

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Vérifier/assigner un rôle par défaut si roles null
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            AppRole defaultRole = appRoleRepository.findByRoleName(RoleName.USER)
                    .orElseThrow(() -> new RuntimeException("Default role not found"));
            user.getRoles().add(defaultRole);
        }

         return userMapper.toResponseDTO(appUserRepository.save(user));
    }

    @Override
    public UserResponseDTO getUserByUsername(String username) {
        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        return userMapper.toResponseDTO(user);
    }

    @Override
    public UserResponseDTO getUserById(long id) {
        AppUser user = appUserRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        return userMapper.toResponseDTO(user);
    }

    @Override
    public UserResponseDTO updateUser(Long id, UserRequestDTO userRequestDTO) {

        AppUser existingUser = appUserRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        // Mise à jour du username (si différent)
        if (userRequestDTO.getUsername() != null &&
                !userRequestDTO.getUsername().equals(existingUser.getUsername())) {

            // Vérifier si username existe
            if (appUserRepository.findByUsername(userRequestDTO.getUsername()).isPresent()) {
                throw new RuntimeException("Username already taken");
            }

            existingUser.setUsername(userRequestDTO.getUsername());
        }

        // Mise à jour email
        if (userRequestDTO.getEmail() != null) {
            existingUser.setEmail(userRequestDTO.getEmail());
        }

        // Mise à jour des rôles
        if (userRequestDTO.getRoles() != null && !userRequestDTO.getRoles().isEmpty()) {

            existingUser.getRoles().clear();

            userRequestDTO.getRoles().forEach(roleName -> {
                AppRole role = appRoleRepository.findByRoleName(roleName)
                        .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));

                existingUser.getRoles().add(role);
            });
        }

        AppUser updatedUser = appUserRepository.save(existingUser);

        return userMapper.toResponseDTO(updatedUser);
    }

    @Override
    public void deleteUser(Long id) {
        AppUser user = appUserRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        appUserRepository.delete(user);
    }

    @Override
    public List<UserResponseDTO> getAllUsers() {
        List<AppUser> users = appUserRepository.findAll();

        return users.stream()
                .map(userMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void changePassword(String username, ChangePasswordRequestDTO changePasswordRequestDTO) {
        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Vérifier l'ancien mot de passe
        if (!passwordEncoder.matches(changePasswordRequestDTO.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Old password is incorrect");
        }

        // Encoder le nouveau mot de passe
        user.setPassword(passwordEncoder.encode(changePasswordRequestDTO.getNewPassword()));

        appUserRepository.save(user);
    }

    @Override
    public void adminResetPassword(Long id, ResetPasswordAdminDTO passwordAdminDTO) {
        AppUser user = appUserRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Encoder le nouveau mot de passe
        user.setPassword(passwordEncoder.encode(passwordAdminDTO.getNewPassword()));

        appUserRepository.save(user);
    }
}
