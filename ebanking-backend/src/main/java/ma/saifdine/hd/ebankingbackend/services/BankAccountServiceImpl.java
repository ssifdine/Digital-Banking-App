package ma.saifdine.hd.ebankingbackend.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.saifdine.hd.ebankingbackend.dtos.*;
import ma.saifdine.hd.ebankingbackend.entities.*;
import ma.saifdine.hd.ebankingbackend.enums.AccountStatus;
import ma.saifdine.hd.ebankingbackend.enums.OperationType;
import ma.saifdine.hd.ebankingbackend.exceptions.BalanceNotSufficientException;
import ma.saifdine.hd.ebankingbackend.exceptions.BankAccountNotFoundException;
import ma.saifdine.hd.ebankingbackend.exceptions.CustomerNotFoundException;
import ma.saifdine.hd.ebankingbackend.mappers.BankAccountMapperImpl;
import ma.saifdine.hd.ebankingbackend.repositories.AccountOperationRepository;
import ma.saifdine.hd.ebankingbackend.repositories.BankAccountRepository;
import ma.saifdine.hd.ebankingbackend.repositories.BeneficiaryRepository;
import ma.saifdine.hd.ebankingbackend.repositories.CustomerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class BankAccountServiceImpl implements BankAccountService {

    private CustomerRepository customerRepository;
    private BankAccountRepository bankAccountRepository;
    private AccountOperationRepository accountOperationRepository;
    private BeneficiaryRepository beneficiaryRepository;
    private BankAccountMapperImpl dtoMapper;


    @Override
    public CustomerDTO saveCustomer(CustomerDTO customerDTO) {
        log.info("Saving new Customer");
        Customer customer = dtoMapper.fromCustomerDTO(customerDTO);
        Customer savedCustomer = customerRepository.save(customer);
        return dtoMapper.fromCustomer(savedCustomer);
    }

    @Override
    public CurrentBankAccountDTO saveCurrentBankAccount(double initialBalance, double overDraft, Long customerId) throws CustomerNotFoundException {
        Customer customer = customerRepository.findById(customerId).orElse(null);
        if(customer == null) {
            throw new CustomerNotFoundException("Customer not found");
        }
        CurrentAccount currentAccount = new CurrentAccount();
        currentAccount.setId(UUID.randomUUID().toString());
//        currentAccount.setCreatedAt(new Date());
        currentAccount.setStatus(AccountStatus.ACTIVE);
        currentAccount.setBalance(initialBalance);
        currentAccount.setOverDraft(overDraft);
        currentAccount.setCustomer(customer);
        CurrentAccount savedBankAccount = bankAccountRepository.save(currentAccount);
        return dtoMapper.fromCurrentBankAccount(savedBankAccount);
    }

    @Override
    public SavingBankAccountDTO saveSavingBankAccount(double initialBalance, double interestRate, Long customerId) throws CustomerNotFoundException {
        Customer customer = customerRepository.findById(customerId).orElse(null);
        if(customer == null) {
            throw new CustomerNotFoundException("Customer not found");
        }
        SavingAccount savingAccount = new SavingAccount();
        savingAccount.setId(UUID.randomUUID().toString());
//        savingAccount.setCreatedAt(new Date());
        savingAccount.setStatus(AccountStatus.ACTIVE);
        savingAccount.setBalance(initialBalance);
        savingAccount.setInterestRate(interestRate);
        savingAccount.setCustomer(customer);
        SavingAccount savedBankAccount = bankAccountRepository.save(savingAccount);
        return dtoMapper.fromSavingBankAccount(savedBankAccount);
    }

    @Override
    public Page<CustomerDTO> listCustomers(Pageable pageable) {
        return customerRepository.findAll(pageable)
                .map(customer -> dtoMapper.fromCustomer(customer));
    }

//    @Override
//    public List<CustomerDTO> listCustomers() {
//        List<Customer> customers = customerRepository.findAll();
//        List<CustomerDTO> customerDTOs = customers.stream()
//                .map(customer -> dtoMapper.fromCustomer(customer))
//                .collect(Collectors.toList());
//        return customerDTOs;
//    }



    @Override
    public BankAccountDTO getBankAccount(String accountId) throws BankAccountNotFoundException {
        BankAccount bankAccount = bankAccountRepository.findById(accountId).orElseThrow(() -> new BankAccountNotFoundException("BankAccount not found"));
        if(bankAccount instanceof SavingAccount) {
            SavingAccount savingAccount = (SavingAccount) bankAccount;
            return dtoMapper.fromSavingBankAccount(savingAccount);
        } else {
            CurrentAccount currentAccount = (CurrentAccount) bankAccount;
            return dtoMapper.fromCurrentBankAccount(currentAccount);
        }
    }

    @Override
    public void debit(String accountId, double amount, String description) throws BankAccountNotFoundException, BalanceNotSufficientException {
        BankAccount bankAccount = bankAccountRepository.findById(accountId).orElseThrow(() -> new BankAccountNotFoundException("BankAccount not found"));
        if (bankAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Account is not active");
        }
        if(bankAccount.getBalance() < amount)
            throw new BalanceNotSufficientException("Balance not sufficient");
        AccountOperation accountOperation = new AccountOperation();
        accountOperation.setType(OperationType.DEBIT);
        accountOperation.setAmount(amount);
        accountOperation.setDescription(description);
        accountOperation.setOperationDate(new Date());
        accountOperation.setBankAccount(bankAccount);
        accountOperationRepository.save(accountOperation);
        bankAccount.setBalance(bankAccount.getBalance() - amount);
        bankAccountRepository.save(bankAccount);
    }

    @Override
    public void credit(String accountId, double amount, String description) throws BankAccountNotFoundException {
        BankAccount bankAccount = bankAccountRepository.findById(accountId).orElseThrow(() -> new BankAccountNotFoundException("BankAccount not found"));
        if (bankAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Account is not active");
        }
        AccountOperation accountOperation = new AccountOperation();
        accountOperation.setType(OperationType.CREDIT);
        accountOperation.setAmount(amount);
        accountOperation.setDescription(description);
        accountOperation.setOperationDate(new Date());
        accountOperation.setBankAccount(bankAccount);
        accountOperationRepository.save(accountOperation);
        bankAccount.setBalance(bankAccount.getBalance() + amount);
        bankAccountRepository.save(bankAccount);

    }

    @Override
    public void transfer(String accountIdSource, String accountIdDestination, double amount) throws BankAccountNotFoundException, BalanceNotSufficientException {
        debit(accountIdSource,amount,"Transfer to " + accountIdDestination);
        credit(accountIdDestination,amount,"Transfer from " + accountIdSource);
    }

    @Override
    public List<BankAccountDTO> bankAccountList(){
        List<BankAccount> bankAccounts = bankAccountRepository.findAll();
        List<BankAccountDTO> bankAccountDTOS = bankAccounts.stream().map(bankAccount -> {
            if(bankAccount instanceof SavingAccount) {
                SavingAccount savingAccount = (SavingAccount) bankAccount;
                return dtoMapper.fromSavingBankAccount(savingAccount);
            }
            else {
                CurrentAccount currentAccount = (CurrentAccount) bankAccount;
                return dtoMapper.fromCurrentBankAccount(currentAccount);
            }
        }).collect(Collectors.toList());
        return bankAccountDTOS;
    }

    @Override
    public CustomerDTO getCustomer(Long customerId) throws CustomerNotFoundException {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer Not found"));
        return dtoMapper.fromCustomer(customer);
    }

    @Override
    public CustomerDTO updateCustomer(CustomerDTO customerDTO) {
        log.info("Saving new Customer");
        Customer customer = dtoMapper.fromCustomerDTO(customerDTO);
        Customer savedCustomer = customerRepository.save(customer);
        return dtoMapper.fromCustomer(savedCustomer);
    }

    @Override
    public void deleteCustomer(Long customerId){
        customerRepository.deleteById(customerId);
    }

    @Override
    public List<AccountOperationDTO> accountHistoy(String accountId){
        List<AccountOperation> accountOperations = accountOperationRepository.findByBankAccountId(accountId);
        return accountOperations.stream().map(op -> dtoMapper.fromAccountOperation(op)).collect(Collectors.toList());
    }

    @Override
    public AccountHistoryDTO getAccountHistoy(String accountId, int page, int size) throws BankAccountNotFoundException {
        BankAccount bankAccount = bankAccountRepository.findById(accountId).orElse(null);
        if(bankAccount == null) throw new BankAccountNotFoundException("Account not found");
        Page<AccountOperation> accountOperations = accountOperationRepository.findByBankAccountIdOrderByOperationDateDesc(accountId, PageRequest.of(page, size));
        List<Beneficaire> beneficiaries = beneficiaryRepository.findBeneficairesByBankAccountId(accountId);

        AccountHistoryDTO accountHistoryDTO = new AccountHistoryDTO();
        List<AccountOperationDTO> accountOperationDTOS = accountOperations.getContent().stream().map(op->dtoMapper.fromAccountOperation(op)).collect(Collectors.toList());
        accountHistoryDTO.setAccountOperationDTOS(accountOperationDTOS);
        List<BeneficaireResponseDTO> beneficiaryDTOs = beneficiaries.stream().map(b -> {
            BeneficaireResponseDTO dto = new BeneficaireResponseDTO();
            dto.setId(b.getId());
            dto.setNom(b.getNom());
            dto.setCompteDestinataire(b.getCompteDestinataire());
            return dto;
        }).toList();

        accountHistoryDTO.setBeneficiaries(beneficiaryDTOs);

        accountHistoryDTO.setAccountId(bankAccount.getId());
        accountHistoryDTO.setBalance(bankAccount.getBalance());
        accountHistoryDTO.setStatus(bankAccount.getStatus());
        if (bankAccount instanceof SavingAccount savingAccount) {
            accountHistoryDTO.setType("Saving");
            accountHistoryDTO.setInterestRate(savingAccount.getInterestRate());
        }
        if (bankAccount instanceof CurrentAccount currentAccount) {
            accountHistoryDTO.setType("Current");
            accountHistoryDTO.setOverdraftLimit(currentAccount.getOverDraft());
        }
        accountHistoryDTO.setCurrentPage(page);
        accountHistoryDTO.setPageSize(size);
        accountHistoryDTO.setTotalPages(accountOperations.getTotalPages());
        return accountHistoryDTO;
    }

    @Override
    public List<CustomerDTO> searchCustomers(String keyword) {
        List<Customer> customers = customerRepository.searchCustomer(keyword);
        List<CustomerDTO> customerDTOS = customers.stream().map(cust -> dtoMapper.fromCustomer(cust)).collect(Collectors.toList());
        return customerDTOS;
    }

    @Override
    public Page<CustomerDTO> searchCustomerPagenation(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Customer> customerPage = customerRepository.searchCustomerPagenation("%" + keyword + "%", pageable);
        // On mappe chaque Customer -> CustomerDTO
        return customerPage.map(cust -> dtoMapper.fromCustomer(cust));
    }


    @Override
    public List<BankAccountDTO> getAccountsByCustomerId(Long customerId) throws CustomerNotFoundException {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
        return customer.getBankAccounts()
                .stream()
                .map(account -> {
                    if (account instanceof SavingAccount) {
                        return dtoMapper.fromSavingBankAccount((SavingAccount) account);
                    } else {
                        return dtoMapper.fromCurrentBankAccount((CurrentAccount) account);
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    public BankAccountDTO getLatestAccount() {
        BankAccount account = bankAccountRepository.findTopByOrderByCreatedDateDesc()
                .orElseThrow(() -> new RuntimeException("Account not found s"));
        if(account instanceof SavingAccount) {
            return dtoMapper.fromSavingBankAccount((SavingAccount) account);
        } else {
            return dtoMapper.fromCurrentBankAccount((CurrentAccount) account);
        }
    }

    @Override
    public String updateInterestRate(String accountId, double newRate) throws BankAccountNotFoundException {
        BankAccount account = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new BankAccountNotFoundException("Account not found"));

        if (!(account instanceof SavingAccount)) {
            return "Erreur : Ce compte n'est pas un compte épargne (SavingAccount), impossible de modifier le taux d'intérêt.";
        }

        try {
            ((SavingAccount) account).setInterestRate(newRate);
            bankAccountRepository.save(account);
            return "Succès : Le taux d’intérêt du compte " + accountId + " a été mis à jour à " + newRate + ".";
        } catch (Exception e) {
            return "Erreur : Échec de la mise à jour du taux d’intérêt pour le compte " + accountId + ".";
        }
    }

    @Override
    public String updateOverdraftLimit(String accountId, double newLimit) throws BankAccountNotFoundException {
        BankAccount account = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new BankAccountNotFoundException("Account not found"));

        if (!(account instanceof CurrentAccount)) {
            return "Erreur : Ce compte n'est pas un compte courant (CurrentAccount), impossible de modifier le découvert.";
        }

        try {
            ((CurrentAccount) account).setOverDraft(newLimit);
            bankAccountRepository.save(account);
            return "Succès : Le découvert du compte " + accountId + " a été mis à jour à " + newLimit + ".";
        } catch (Exception e) {
            return "Erreur : Échec de la mise à jour du découvert pour le compte " + accountId + ".";
        }
    }

    @Override
    public AccountHistoryDTO searchOperations(String accountId, LocalDate startDate, LocalDate endDate,
                                              Double minAmount, Double maxAmount, int page, int size)
            throws BankAccountNotFoundException {
        BankAccount account = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new BankAccountNotFoundException("Account not found"));

        // Convertir LocalDate en LocalDateTime (début de journée 00:00:00)
        LocalDateTime start = (startDate != null)
                ? startDate.atStartOfDay()
                : LocalDateTime.of(1970, 1, 1, 0, 0);

        // Inclure toute la journée de fin (jusqu'à 23:59:59.999999999)
        LocalDateTime end = (endDate != null)
                ? endDate.plusDays(1).atStartOfDay()
                : LocalDateTime.now();

        double min = (minAmount != null) ? minAmount : 0.0;
        double max = (maxAmount != null) ? maxAmount : Double.MAX_VALUE;

        Page<AccountOperation> operationsPage = accountOperationRepository.searchOperations(
                accountId, start, end, min, max,
                PageRequest.of(page, size, Sort.by("operationDate").descending())
        );

        AccountHistoryDTO dto = new AccountHistoryDTO();
        dto.setAccountId(account.getId());
        dto.setBalance(account.getBalance());
        dto.setCurrentPage(page);
        dto.setPageSize(size);
        dto.setTotalPages(operationsPage.getTotalPages());
        dto.setStatus(account.getStatus());
        dto.setAccountOperationDTOS(operationsPage.getContent().stream()
                .map(dtoMapper::fromAccountOperation)
                .toList());

        if (account instanceof SavingAccount savingAccount) {
            dto.setType("Saving");
            dto.setInterestRate(savingAccount.getInterestRate());
        } else if (account instanceof CurrentAccount currentAccount) {
            dto.setType("Current");
            dto.setOverdraftLimit(currentAccount.getOverDraft());
        }

        return dto;
    }

    @Override
    public void updateAccountStatus(String id, AccountStatus status) throws BankAccountNotFoundException {
        BankAccount bankAccount = bankAccountRepository.findById(id)
                .orElseThrow(() -> new BankAccountNotFoundException("Bank account not found"));
        bankAccount.setStatus(status);
        bankAccountRepository.save(bankAccount);
    }


}
