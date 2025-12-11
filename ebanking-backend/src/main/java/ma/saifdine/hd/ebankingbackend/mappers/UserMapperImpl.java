package ma.saifdine.hd.ebankingbackend.mappers;

import ma.saifdine.hd.ebankingbackend.dtos.UserRequestDTO;
import ma.saifdine.hd.ebankingbackend.dtos.UserResponseDTO;
import ma.saifdine.hd.ebankingbackend.entities.AppRole;
import ma.saifdine.hd.ebankingbackend.entities.AppUser;
import ma.saifdine.hd.ebankingbackend.enums.RoleName;
import ma.saifdine.hd.ebankingbackend.repositories.AppRoleRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserMapperImpl {

    private final AppRoleRepository appRoleRepository;


    public UserMapperImpl(AppRoleRepository appRoleRepository) {
        this.appRoleRepository = appRoleRepository;
    }

    // DTO -> Entity
    public AppUser toEntity(UserRequestDTO dto) {
        if (dto == null) return null;

        AppUser user = new AppUser();
        user.setId(dto.getId());
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword());
        user.setEmail(dto.getEmail());

        if (dto.getRoles() != null && !dto.getRoles().isEmpty()) {
            List<AppRole> roles = dto.getRoles().stream()
                    .map(roleName -> appRoleRepository
                            .findByRoleName(roleName)
                            .orElse(null)) // On laisse le service gérer les vérifications
                    .collect(Collectors.toList());
            user.setRoles(roles);
        }
        return user;
    }

    // Entity -> RequestDTO
    public UserRequestDTO toDTO(AppUser entity) {
        if (entity == null) return null;

        UserRequestDTO dto = new UserRequestDTO();
        dto.setId(entity.getId());
        dto.setUsername(entity.getUsername());
        dto.setPassword(entity.getPassword());
        dto.setEmail(entity.getEmail());

        if (entity.getRoles() != null && !entity.getRoles().isEmpty()) {
            Set<RoleName> roles = entity.getRoles().stream()
                    .map(AppRole::getRoleName)
                    .collect(Collectors.toSet());
            dto.setRoles(roles);
        }

        return dto;
    }




    // Entity -> ResponseDTO
    public UserResponseDTO toResponseDTO(AppUser user) {
        if (user == null) return null;

        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());

        // Convertir la liste des AppRole en Set<RoleName>
        Set<RoleName> roles = user.getRoles()
                .stream()
                .map(AppRole::getRoleName)
                .collect(Collectors.toSet());

        dto.setRoles(roles);

        // Champs audit
        dto.setCreatedBy(user.getCreatedBy());
        dto.setCreatedDate(user.getCreatedDate());
        dto.setLastModifiedBy(user.getLastModifiedBy());
        dto.setLastModifiedDate(user.getLastModifiedDate());

        return dto;
    }
}
