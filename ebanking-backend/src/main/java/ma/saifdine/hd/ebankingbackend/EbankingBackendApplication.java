package ma.saifdine.hd.ebankingbackend;

import ma.saifdine.hd.ebankingbackend.dtos.BankAccountDTO;
import ma.saifdine.hd.ebankingbackend.dtos.CurrentBankAccountDTO;
import ma.saifdine.hd.ebankingbackend.dtos.CustomerDTO;
import ma.saifdine.hd.ebankingbackend.dtos.SavingBankAccountDTO;
import ma.saifdine.hd.ebankingbackend.entities.*;
import ma.saifdine.hd.ebankingbackend.enums.AccountStatus;
import ma.saifdine.hd.ebankingbackend.enums.OperationType;
import ma.saifdine.hd.ebankingbackend.enums.RoleName;
import ma.saifdine.hd.ebankingbackend.exceptions.CustomerNotFoundException;
import ma.saifdine.hd.ebankingbackend.repositories.AccountOperationRepository;
import ma.saifdine.hd.ebankingbackend.repositories.BankAccountRepository;
import ma.saifdine.hd.ebankingbackend.repositories.CustomerRepository;
import ma.saifdine.hd.ebankingbackend.security.AccountService;
import ma.saifdine.hd.ebankingbackend.services.BankAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class EbankingBackendApplication {

    @Autowired
    private AccountService accountService;

    public static void main(String[] args) {
        SpringApplication.run(EbankingBackendApplication.class, args);
    }

    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> {
            var auth = SecurityContextHolder.getContext().getAuthentication();

            if (auth == null || !auth.isAuthenticated()) {
                return Optional.of("SYSTEM");
            }

            return Optional.of(auth.getName());
        };
    }



    @Bean
    CommandLineRunner runner(BankAccountService bankAccountService){

// Créer les rôles
        accountService.addNewRole(RoleName.USER);
        accountService.addNewRole(RoleName.ADMIN);

        // Créer les utilisateurs
        accountService.addNewUser("user1", "12345", "user1@example.com");
        accountService.addNewUser("admin1", "12345", "admin1@example.com");
        accountService.addNewUser("user2", "12345", "user2@example.com");
        accountService.addNewUser("admin2", "12345", "admin2@example.com");

        // Assigner les rôles
        accountService.addRoleToUser("user1", RoleName.USER);
        accountService.addRoleToUser("admin1", RoleName.USER);
        accountService.addRoleToUser("admin1", RoleName.ADMIN);
        accountService.addRoleToUser("admin2", RoleName.USER);
        accountService.addRoleToUser("admin2", RoleName.ADMIN);
        accountService.addRoleToUser("user2", RoleName.USER);

        System.out.println("✅ Données initiales créées avec succès!");

        return args -> {
            Stream.of("Hassan","Imane","Mohamed").forEach(name ->{
                CustomerDTO customer = new CustomerDTO();
                customer.setName(name);
                customer.setEmail(name + "@gmail.com");
                bankAccountService.saveCustomer(customer);
            });
            bankAccountService.listCustomers(PageRequest.of(0, 100)).forEach(customer -> {
                try {
                    bankAccountService.saveCurrentBankAccount(Math.random() + 90000, 9000, customer.getId());
                    bankAccountService.saveSavingBankAccount(Math.random() + 90000, 5.5, customer.getId());
                } catch (CustomerNotFoundException e) {
                    e.printStackTrace();
                }
            });

            List<BankAccountDTO> bankAccounts = bankAccountService.bankAccountList();
            for(BankAccountDTO bankAccount : bankAccounts){
                for (int i = 0; i < 10; i++) {
                    String accountId;
                    if(bankAccount instanceof SavingBankAccountDTO){
                        accountId = ((SavingBankAccountDTO) bankAccount).getId();
                    } else {
                        accountId = ((CurrentBankAccountDTO) bankAccount).getId();
                    }
                    bankAccountService.credit(accountId, 10000+Math.random()*120000,"Credit");
                    bankAccountService.debit(accountId,1000 + Math.random()*9000,"Debit");
                }
            }
        };
    }

//    @Bean
    CommandLineRunner start(CustomerRepository customerRepository,
                            BankAccountRepository bankAccountRepository,
                            AccountOperationRepository operationRepository) {
        return args -> {
            Stream.of("Hassan","Yassine","Aihca").forEach(name -> {
                Customer customer = new Customer();
                customer.setName(name);
                customer.setEmail(name + "@gmail.com");
                customerRepository.save(customer);
            });
            customerRepository.findAll().forEach(customer -> {
                CurrentAccount currentAccount = new CurrentAccount();
                currentAccount.setId(UUID.randomUUID().toString());
                currentAccount.setBalance(Math.random() * 9000);
//                currentAccount.setCreatedAt(new Date());
                currentAccount.setStatus(AccountStatus.ACTIVE);
                currentAccount.setCustomer(customer);
                currentAccount.setOverDraft(9000);
                bankAccountRepository.save(currentAccount);

                SavingAccount savingAccount = new SavingAccount();
                savingAccount.setId(UUID.randomUUID().toString());
                savingAccount.setBalance(Math.random() * 9000);
//                savingAccount.setCreatedAt(new Date());
                savingAccount.setStatus(AccountStatus.ACTIVE);
                savingAccount.setCustomer(customer);
                savingAccount.setInterestRate(5.5);
                bankAccountRepository.save(savingAccount);
            });

            bankAccountRepository.findAll().forEach(bankAccount -> {
                for (int i = 0; i < 10 ; i++){
                    AccountOperation accountOperation = new AccountOperation();
                    accountOperation.setOperationDate(new Date());
                    accountOperation.setAmount(Math.random() * 12000);
                    accountOperation.setType(Math.random()>0.5? OperationType.DEBIT: OperationType.CREDIT);
                    accountOperation.setBankAccount(bankAccount);
                    operationRepository.save(accountOperation);

                }
            });
        };
    }
}


