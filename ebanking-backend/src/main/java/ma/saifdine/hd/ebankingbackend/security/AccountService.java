package ma.saifdine.hd.ebankingbackend.security;

import ma.saifdine.hd.ebankingbackend.entities.AppRole;
import ma.saifdine.hd.ebankingbackend.entities.AppUser;
import ma.saifdine.hd.ebankingbackend.enums.RoleName;
import ma.saifdine.hd.ebankingbackend.repositories.AppRoleRepository;
import ma.saifdine.hd.ebankingbackend.repositories.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AccountService {

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private AppRoleRepository appRoleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder ;

    public AppUser addNewUser(String username, String password, String email) {
        AppUser appUser = new AppUser();
        appUser.setUsername(username);
        appUser.setPassword(passwordEncoder.encode(password));
        appUser.setEmail(email);
        return appUserRepository.save(appUser);
    }

    public AppRole addNewRole(RoleName roleName) {
        AppRole appRole = new AppRole(roleName);
        return appRoleRepository.save(appRole);
    }

    public void addRoleToUser(String username, RoleName roleName) {
        AppUser appUser = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        AppRole appRole = appRoleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        appUser.getRoles().add(appRole);
    }

    public AppUser loadUserByUsername(String username) {
        return appUserRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public List<AppUser> listUsers() {
        return appUserRepository.findAll();
    }
}
