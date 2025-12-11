package ma.saifdine.hd.ebankingbackend.mappers;

import ma.saifdine.hd.ebankingbackend.dtos.BeneficaireRequestDTO;
import ma.saifdine.hd.ebankingbackend.dtos.BeneficaireResponseDTO;
import ma.saifdine.hd.ebankingbackend.entities.Beneficaire;
import org.springframework.stereotype.Service;

@Service
public class BeneficiaryMapperImpl {

    // DTO -> Entity
    public Beneficaire toEntity(BeneficaireRequestDTO dto) {
        if (dto == null) return null;

        Beneficaire b = new Beneficaire();
        b.setNom(dto.getNom());
        b.setCompteDestinataire(dto.getCompteDestinataire());
        return b;
    }

    // Entity -> DTO
    public BeneficaireRequestDTO toDTO(Beneficaire entity) {
        if (entity == null) return null;

        BeneficaireRequestDTO dto = new BeneficaireRequestDTO();
        dto.setNom(entity.getNom());
        dto.setCompteDestinataire(entity.getCompteDestinataire());
        dto.setBankAccountId(entity.getBankAccount().getId());
        return dto;
    }

    // Entity -> ResponseDTO
    public BeneficaireResponseDTO toResponseDTO(Beneficaire entity) {
        if (entity == null) return null;

        BeneficaireResponseDTO dto = new BeneficaireResponseDTO();
        dto.setId(entity.getId());
        dto.setNom(entity.getNom());
        dto.setCompteDestinataire(entity.getCompteDestinataire());
        return dto;
    }

}
