package ma.saifdine.hd.ebankingbackend.repositories;

import ma.saifdine.hd.ebankingbackend.entities.Beneficaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BeneficiaryRepository extends JpaRepository<Beneficaire, Long> {

    List<Beneficaire> findBeneficairesByBankAccountId(String bankAccountId);

    Beneficaire findByCompteDestinataireAndBankAccountId(String compteDestinataire, String bankAccountId);

    Beneficaire findByIdAndBankAccountId(Long id, String accountId);

}
