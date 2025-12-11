package ma.saifdine.hd.ebankingbackend.repositories;

import ma.saifdine.hd.ebankingbackend.entities.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BankAccountRepository extends JpaRepository<BankAccount,String> {
    Optional<BankAccount> findTopByOrderByCreatedDateDesc();
    //    List<BankAccount> findByCustomerId(String customerId);
}
