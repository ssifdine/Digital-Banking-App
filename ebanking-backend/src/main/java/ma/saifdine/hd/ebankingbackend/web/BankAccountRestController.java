package ma.saifdine.hd.ebankingbackend.web;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import ma.saifdine.hd.ebankingbackend.dtos.*;
import ma.saifdine.hd.ebankingbackend.enums.AccountStatus;
import ma.saifdine.hd.ebankingbackend.exceptions.BalanceNotSufficientException;
import ma.saifdine.hd.ebankingbackend.exceptions.BankAccountNotFoundException;
import ma.saifdine.hd.ebankingbackend.exceptions.CustomerNotFoundException;
import ma.saifdine.hd.ebankingbackend.services.BankAccountService;
import ma.saifdine.hd.ebankingbackend.services.BeneficaireService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@AllArgsConstructor
@CrossOrigin("*")
public class BankAccountRestController {

    private BankAccountService bankAccountService;
    private BeneficaireService beneficaireService;

    @GetMapping("/accounts/{accountId}")
    public BankAccountDTO getBankAccount(@PathVariable String accountId) throws BankAccountNotFoundException {
        System.out.println("getBankAccount-accountId : " + accountId);
        return bankAccountService.getBankAccount(accountId);
    }

    @GetMapping("/accounts")
    public List<BankAccountDTO> getAllBankAccounts() {
        return bankAccountService.bankAccountList();
    }

    @GetMapping("/accounts/{accountId}/operations")
    public List<AccountOperationDTO> getHistorique(@PathVariable String accountId) {
        return bankAccountService.accountHistoy(accountId);
    }

    @GetMapping("/accounts/{accountId}/pageOperations")
    public AccountHistoryDTO getAccountHistorique(
            @PathVariable String accountId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size
                                                          ) throws BankAccountNotFoundException {
        return bankAccountService.getAccountHistoy(accountId,page,size);
    }

    @PostMapping("/accounts/debit")
    public DebitDTO debit(@RequestBody DebitDTO debitDTO) throws BankAccountNotFoundException, BalanceNotSufficientException {
        this.bankAccountService.debit(debitDTO.getAccountId(), debitDTO.getAmount(), debitDTO.getDescription());
        return debitDTO;
    }

    @PostMapping("/accounts/credit")
    public CreditDTO credit(@RequestBody CreditDTO creditDTO) throws BankAccountNotFoundException {
        this.bankAccountService.credit(creditDTO.getAccountId(), creditDTO.getAmount(), creditDTO.getDescription());
        return creditDTO;
    }

    @PostMapping("/accounts/transfer")
    public void transfer(@RequestBody TransferRequestDTO transferRequestDTO) throws BankAccountNotFoundException, BalanceNotSufficientException {
        this.bankAccountService.transfer(transferRequestDTO.getAccountSource(),
                transferRequestDTO.getAccountDestination(),
                transferRequestDTO.getAmount());
    }

    @PostMapping("/accounts/createAccount")
    public BankAccountDTO create(@RequestBody BankAccountRequestDTO bankAccountRequestDTO) throws BankAccountNotFoundException , CustomerNotFoundException {
        if("CURRENT".equalsIgnoreCase(bankAccountRequestDTO.getType())){
            return bankAccountService.saveCurrentBankAccount(bankAccountRequestDTO.getInitialBalance(),bankAccountRequestDTO.getOverdraft(),bankAccountRequestDTO.getCustomerId());
        } else if ("SAVING".equalsIgnoreCase(bankAccountRequestDTO.getType())) {
            return bankAccountService.saveSavingBankAccount(bankAccountRequestDTO.getInitialBalance(),bankAccountRequestDTO.getInterestRate(),bankAccountRequestDTO.getCustomerId());
        } else {
            throw new IllegalArgumentException("Invalid account type: " + bankAccountRequestDTO.getType());
        }
    }

    @GetMapping("/accounts/latest")
    public BankAccountDTO getLatestAccount(){
        return bankAccountService.getLatestAccount();
    }

    @PutMapping("/accounts/{id}/interest-rate")
    @PreAuthorize("hasAuthority('SCOPE_USER')")
    public ResponseEntity<String> updateInterestRate(@PathVariable String id, @RequestBody double newRate) {
        try {
            String message = bankAccountService.updateInterestRate(id, newRate);
            return ResponseEntity.ok(message);
        } catch (BankAccountNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erreur inattendue : " + e.getMessage());
        }
    }

    @PutMapping("/accounts/{id}/overdraft-limit")
    @PreAuthorize("hasAuthority('SCOPE_USER')")
    public ResponseEntity<String> updateOverdraftLimit(@PathVariable String id, @RequestBody double newLimit) {
        try {
            String message = bankAccountService.updateOverdraftLimit(id, newLimit);
            return ResponseEntity.ok(message);
        } catch (BankAccountNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erreur inattendue : " + e.getMessage());
        }
    }

    @GetMapping("/accounts/{accountId}/operations/search")
    @PreAuthorize("hasAuthority('SCOPE_USER')")
    public ResponseEntity<AccountHistoryDTO> searchOperations(
            @PathVariable String accountId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) @Min(0) Double minAmount,
            @RequestParam(required = false) @Min(0) Double maxAmount,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "5") @Min(1) @Max(100) int size
    ) throws BankAccountNotFoundException {
        // Validation personnalisée
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }
        if (minAmount != null && maxAmount != null && minAmount > maxAmount) {
            throw new IllegalArgumentException("Min amount must be less than max amount");
        }

        AccountHistoryDTO result = bankAccountService.searchOperations(
                accountId, startDate, endDate, minAmount, maxAmount, page, size
        );
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/accounts/{id}/status")
    @PreAuthorize("hasAuthority('SCOPE_USER')")
    public ResponseEntity<?> updateStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> payload,
            Principal principal) throws BankAccountNotFoundException {

        String status = payload.get("status");
        AccountStatus newStatus;
        try {
            newStatus = AccountStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid status value: " + status);
        }

        bankAccountService.updateAccountStatus(id, newStatus);
//        actionLogServiceImpl.log(principal.getName(), "UPDATE_ACCOUNT_STATUS", "Updated status of account " + id + " to " + newStatus);
        return ResponseEntity.ok("Status updated to " + newStatus);
    }

    @PostMapping("/accounts/{id}/addBeneficaire")
    @PreAuthorize("hasAuthority('SCOPE_USER')")
    public ResponseEntity<Map<String, String>> addBeneficaire(
            @PathVariable String id,
            @RequestBody BeneficaireRequestDTO beneficaireRequestDTO) {

        beneficaireRequestDTO.setBankAccountId(id);
        beneficaireService.saveBeneficaire(beneficaireRequestDTO);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Bénéficiaire ajouté avec succès");

        return ResponseEntity.ok(response);
    }


    @GetMapping("/accounts/{accountId}/beneficaires")
    @PreAuthorize("hasAuthority('SCOPE_USER')")
    public ResponseEntity<BeneficiareHistoryDTO> getbeneficairesByAccountId(@PathVariable String accountId) {
        BeneficiareHistoryDTO beneficiareHistoryDTO = beneficaireService.getbeneficairesByAccountId1(accountId);
        return ResponseEntity.ok(beneficiareHistoryDTO);
    }

    @PutMapping("/accounts/{id}/updateBeneficaire")
    @PreAuthorize("hasAuthority('SCOPE_USER')")
    public ResponseEntity<BeneficaireResponseDTO> updateBeneficaire(
            @PathVariable String id,
            @RequestBody BeneficaireRequestDTO beneficaireRequestDTO
    ){
        beneficaireRequestDTO.setBankAccountId(id);
        BeneficaireResponseDTO responseDTO = beneficaireService.updateBenficaire(beneficaireRequestDTO);
        return ResponseEntity.ok(responseDTO);
    }

    @DeleteMapping("/accounts/{accountId}/beneficiaires/{benefId}")
    @PreAuthorize("hasAuthority('SCOPE_USER')")
    public ResponseEntity<String> deleteBeneficiaire(
            @PathVariable String accountId,
            @PathVariable Long benefId) {

        beneficaireService.deleteBeneficaire(benefId, accountId);
        return ResponseEntity.ok("Bénéficiaire supprimé avec succès");
    }


}
