package ma.saifdine.hd.ebankingbackend.dtos;

import lombok.Data;
import ma.saifdine.hd.ebankingbackend.enums.AccountStatus;

import java.util.List;

@Data
public class BeneficiareHistoryDTO {

    private String accountId;
    private AccountStatus status;
    private List<BeneficaireResponseDTO> beneficiaries;
}
