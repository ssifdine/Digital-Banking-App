package ma.saifdine.hd.ebankingbackend.services;

import lombok.AllArgsConstructor;
import ma.saifdine.hd.ebankingbackend.dtos.AccountHistoryDTO;
import ma.saifdine.hd.ebankingbackend.dtos.BeneficaireRequestDTO;
import ma.saifdine.hd.ebankingbackend.dtos.BeneficaireResponseDTO;
import ma.saifdine.hd.ebankingbackend.dtos.BeneficiareHistoryDTO;
import ma.saifdine.hd.ebankingbackend.entities.BankAccount;
import ma.saifdine.hd.ebankingbackend.entities.Beneficaire;
import ma.saifdine.hd.ebankingbackend.mappers.BeneficiaryMapperImpl;
import ma.saifdine.hd.ebankingbackend.repositories.BankAccountRepository;
import ma.saifdine.hd.ebankingbackend.repositories.BeneficiaryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@AllArgsConstructor
public class BeneficaireServiceImpl implements BeneficaireService {

    private BeneficiaryRepository beneficiaryRepository;
    private BankAccountRepository bankAccountRepository;
    private BeneficiaryMapperImpl beneficiaryMapper;

    @Override
    public void saveBeneficaire(BeneficaireRequestDTO beneficaireRequestDTO) {
        BankAccount account = bankAccountRepository
                .findById(beneficaireRequestDTO.getBankAccountId())
                .orElseThrow(() -> new RuntimeException("Compte introuvable"));

        Beneficaire existingBenef = beneficiaryRepository
                .findByCompteDestinataireAndBankAccountId(
                        beneficaireRequestDTO.getCompteDestinataire(),
                        beneficaireRequestDTO.getBankAccountId()
                );

        if (existingBenef != null) {
            throw new RuntimeException("Bénéficiaire déjà existant pour ce compte");
        }


        Beneficaire newBeneficaire = beneficiaryMapper.toEntity(beneficaireRequestDTO);
        newBeneficaire.setBankAccount(account);
        beneficiaryRepository.save(newBeneficaire);
    }

    @Override
    public BeneficaireResponseDTO updateBenficaire(BeneficaireRequestDTO dto) {

        BankAccount account = bankAccountRepository.findById(dto.getBankAccountId())
                .orElseThrow(() -> new RuntimeException("Compte introuvable"));

        Beneficaire beneficaire = beneficiaryRepository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("Bénéficiaire introuvable"));

        if (dto.getNom() != null && !dto.getNom().isEmpty()) {
            beneficaire.setNom(dto.getNom());
        }
        if(dto.getCompteDestinataire() != null && !dto.getCompteDestinataire().isEmpty()) {
            beneficaire.setCompteDestinataire(dto.getCompteDestinataire());
        }

        Beneficaire updated = beneficiaryRepository.save(beneficaire);

        return beneficiaryMapper.toResponseDTO(updated);
    }

    @Override
    public void deleteBeneficaire(Long id, String accountId) {
        Beneficaire b = beneficiaryRepository.findByIdAndBankAccountId(id, accountId);

        if (b == null) {
            throw new RuntimeException("Bénéficiaire n'appartient pas à ce compte ou introuvable");
        }

        beneficiaryRepository.delete(b);
    }


    @Override
    public List<BeneficaireRequestDTO> getbeneficairesByAccountId(String accountId) {
        // 1️⃣ Vérifier que le compte existe
        bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Compte introuvable"));

        // 2️⃣ Récupérer les bénéficiaires liés au compte
        List<Beneficaire> beneficiaries = beneficiaryRepository.findBeneficairesByBankAccountId(accountId);

        // 3️⃣ Mapper les entités vers DTO
        return beneficiaries.stream().map(b -> {
            BeneficaireRequestDTO dto = new BeneficaireRequestDTO();
            dto.setNom(b.getNom());
            dto.setCompteDestinataire(b.getCompteDestinataire());
            dto.setBankAccountId(accountId);
            return dto;
        }).toList();
    }

    @Override
    public BeneficiareHistoryDTO getbeneficairesByAccountId1(String accountId) {
        // 1️⃣ Vérifier que le compte existe
        BankAccount bankAccount = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Compte introuvable"));

        // 2️⃣ Récupérer les bénéficiaires liés au compte
        List<Beneficaire> beneficiaries = beneficiaryRepository.findBeneficairesByBankAccountId(accountId);

        // 3️⃣ Créer le DTO principal
        BeneficiareHistoryDTO beneficiareHistoryDTO = new BeneficiareHistoryDTO();
        beneficiareHistoryDTO.setAccountId(accountId);
        beneficiareHistoryDTO.setStatus(bankAccount.getStatus());

        // 4️⃣ Mapper les bénéficiaires vers les DTO
        List<BeneficaireResponseDTO> beneficiaryDTOs = beneficiaries.stream().map(b -> {
            BeneficaireResponseDTO dto = new BeneficaireResponseDTO();
            dto.setId(b.getId());
            dto.setNom(b.getNom());
            dto.setCompteDestinataire(b.getCompteDestinataire());
//            dto.setBankAccountId(bankAccount.getId());
            return dto;
        }).toList();

        beneficiareHistoryDTO.setBeneficiaries(beneficiaryDTOs);

        // 5️⃣ Retourner le DTO complet
        return beneficiareHistoryDTO;
    }



}
