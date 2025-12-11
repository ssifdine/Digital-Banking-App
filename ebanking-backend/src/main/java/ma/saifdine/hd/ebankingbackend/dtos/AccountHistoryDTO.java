package ma.saifdine.hd.ebankingbackend.dtos;

import lombok.Data;
import ma.saifdine.hd.ebankingbackend.enums.AccountStatus;

import java.util.List;

@Data
public class AccountHistoryDTO {

    private String accountId;
    private double balance;
    private int currentPage;
    private int totalPages;
    private int pageSize;
    private List<AccountOperationDTO> accountOperationDTOS;
    private AccountStatus status;
    private String type;
    private double interestRate;
    private double overdraftLimit;
    private List<BeneficaireResponseDTO> beneficiaries;
}
