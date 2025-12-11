package ma.saifdine.hd.ebankingbackend.dtos;

import lombok.Data;

@Data
public class BeneficaireResponseDTO {

    private Long id;
    private String nom;
    private String compteDestinataire;
}
