package ma.saifdine.hd.ebankingbackend.services;

import ma.saifdine.hd.ebankingbackend.dtos.*;
import ma.saifdine.hd.ebankingbackend.entities.BankAccount;
import ma.saifdine.hd.ebankingbackend.entities.CurrentAccount;
import ma.saifdine.hd.ebankingbackend.entities.Customer;
import ma.saifdine.hd.ebankingbackend.entities.SavingAccount;
import ma.saifdine.hd.ebankingbackend.enums.AccountStatus;
import ma.saifdine.hd.ebankingbackend.exceptions.BalanceNotSufficientException;
import ma.saifdine.hd.ebankingbackend.exceptions.BankAccountNotFoundException;
import ma.saifdine.hd.ebankingbackend.exceptions.CustomerNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface BankAccountService {

    CustomerDTO saveCustomer(CustomerDTO customerDTO);

    CurrentBankAccountDTO saveCurrentBankAccount(double initialBalance, double overDraft, Long customerId) throws CustomerNotFoundException;

    SavingBankAccountDTO saveSavingBankAccount(double initialBalance, double interestRate, Long customerId) throws CustomerNotFoundException;

//    List<CustomerDTO> listCustomers();

    Page<CustomerDTO> listCustomers(Pageable pageable);

    BankAccountDTO getBankAccount(String accountId) throws BankAccountNotFoundException;

    void debit(String accountId, double amount,String description) throws BankAccountNotFoundException, BalanceNotSufficientException;

    void credit(String accountId, double amount,String description) throws BankAccountNotFoundException;
    void transfer(String accountIdSource,String accountIdDestination, double amount) throws BankAccountNotFoundException, BalanceNotSufficientException;


    List<BankAccountDTO> bankAccountList();

    CustomerDTO getCustomer(Long customerId) throws CustomerNotFoundException;

    CustomerDTO updateCustomer(CustomerDTO customerDTO);

    void deleteCustomer(Long customerId);

    List<AccountOperationDTO> accountHistoy(String accountId);

    AccountHistoryDTO getAccountHistoy(String accountId, int page, int size) throws BankAccountNotFoundException;

    List<CustomerDTO> searchCustomers(String keyword);

    public Page<CustomerDTO> searchCustomerPagenation(String keyword, int page, int size);

    List<BankAccountDTO> getAccountsByCustomerId(Long customerId) throws CustomerNotFoundException;

    BankAccountDTO getLatestAccount();

    String updateInterestRate(String id, double newRate) throws BankAccountNotFoundException;

    String updateOverdraftLimit(String accountId, double newLimit) throws BankAccountNotFoundException;

    AccountHistoryDTO searchOperations(String accountId, LocalDate startDate, LocalDate endDate, Double minAmount, Double maxAmount, int page, int size) throws BankAccountNotFoundException;

    void updateAccountStatus(String id, AccountStatus status) throws BankAccountNotFoundException;


}


