package ma.saifdine.hd.ebankingbackend.services;

import ma.saifdine.hd.ebankingbackend.dtos.AccountHistoryDTO;
import ma.saifdine.hd.ebankingbackend.dtos.BeneficaireRequestDTO;
import ma.saifdine.hd.ebankingbackend.dtos.BeneficaireResponseDTO;
import ma.saifdine.hd.ebankingbackend.dtos.BeneficiareHistoryDTO;

import java.util.List;
//import ma.saifdine.hd.ebankingbackend.entities.Beneficaire;

public interface BeneficaireService {

    void saveBeneficaire(BeneficaireRequestDTO beneficaireRequestDTO);

    BeneficaireResponseDTO updateBenficaire(BeneficaireRequestDTO beneficaireRequestDTO);

    void deleteBeneficaire(Long id, String accountId);


    List<BeneficaireRequestDTO> getbeneficairesByAccountId(String account);

    BeneficiareHistoryDTO getbeneficairesByAccountId1(String account);

}
