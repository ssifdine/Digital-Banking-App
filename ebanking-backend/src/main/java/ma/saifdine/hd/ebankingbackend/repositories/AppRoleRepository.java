package ma.saifdine.hd.ebankingbackend.repositories;

import ma.saifdine.hd.ebankingbackend.entities.AppRole;
import ma.saifdine.hd.ebankingbackend.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppRoleRepository extends JpaRepository<AppRole, Long> {
    Optional<AppRole> findByRoleName(RoleName role);
}
