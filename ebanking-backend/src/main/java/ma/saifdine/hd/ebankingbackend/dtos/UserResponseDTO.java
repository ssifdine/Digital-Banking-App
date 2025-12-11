package ma.saifdine.hd.ebankingbackend.dtos;

import lombok.Data;
import ma.saifdine.hd.ebankingbackend.enums.RoleName;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class UserResponseDTO {

    private Long id;

    private String username;

    private String email;

    private Set<RoleName> roles;

    private String createdBy;

    private LocalDateTime createdDate;

    private String lastModifiedBy;

    private LocalDateTime lastModifiedDate;
}
