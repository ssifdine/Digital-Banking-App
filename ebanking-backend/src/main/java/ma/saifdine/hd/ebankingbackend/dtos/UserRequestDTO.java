package ma.saifdine.hd.ebankingbackend.dtos;

import lombok.Data;
import ma.saifdine.hd.ebankingbackend.enums.RoleName;

import java.util.Set;

@Data
public class UserRequestDTO {

    private Long id;

    private String username;

    private String password;

    private String email;

    private Set<RoleName> roles;
}
