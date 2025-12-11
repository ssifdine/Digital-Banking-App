package ma.saifdine.hd.ebankingbackend.dtos;

import lombok.Data;

@Data
public class BeneficaireRequestDTO {

    private Long id;
    private String bankAccountId; // ID du compte bancaire lié
    private String nom;
    private String compteDestinataire;

}
