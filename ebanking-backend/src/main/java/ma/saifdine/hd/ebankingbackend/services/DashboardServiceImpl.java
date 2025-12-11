package ma.saifdine.hd.ebankingbackend.services;

import lombok.RequiredArgsConstructor;
import ma.saifdine.hd.ebankingbackend.entities.BankAccount;
import ma.saifdine.hd.ebankingbackend.entities.CurrentAccount;
import ma.saifdine.hd.ebankingbackend.entities.Customer;
import ma.saifdine.hd.ebankingbackend.entities.SavingAccount;
import ma.saifdine.hd.ebankingbackend.enums.OperationType;
import ma.saifdine.hd.ebankingbackend.repositories.AccountOperationRepository;
import ma.saifdine.hd.ebankingbackend.repositories.AppUserRepository;
import ma.saifdine.hd.ebankingbackend.repositories.BankAccountRepository;
import ma.saifdine.hd.ebankingbackend.repositories.CustomerRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final CustomerRepository customerRepository;
    private final BankAccountRepository bankAccountRepository;
    private final AccountOperationRepository accountOperationRepository;
    private final AppUserRepository appUserRepository;

    @Override
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("customerCount", customerRepository.count());
        stats.put("accountCount", bankAccountRepository.count());
        stats.put("totalBalance", bankAccountRepository.findAll().stream()
                .mapToDouble(BankAccount::getBalance)
                .sum());
        stats.put("currentAccounts", countCurrentAccounts());
        stats.put("savingAccounts", countSavingAccounts());
        stats.put("operationCount", accountOperationRepository.count());
        return stats;
    }

    @Override
    public Map<String, Long> getOperationsByType() {
        Map<String, Long> result = new HashMap<>();
        result.put("DEBIT", accountOperationRepository.countByType(OperationType.DEBIT));
        result.put("CREDIT", accountOperationRepository.countByType(OperationType.CREDIT));
        return result;
    }

    @Override
    public Map<String, Long> getMostActiveCustomers() {
        List<Object[]> raw = accountOperationRepository.countOperationsGroupedByBankAccountId();
        Map<String, Long> result = new LinkedHashMap<>();

        for (Object[] row : raw) {
            String bankAccountId = (String) row[0];
            Long count = (Long) row[1];

            BankAccount bankAccount = bankAccountRepository.findById(bankAccountId).orElse(null);
            if (bankAccount == null) continue;

            Long customerId = bankAccount.getCustomer().getId();
            String customerName = customerRepository.findById(customerId)
                    .map(Customer::getName)
                    .orElse("Unknown");

            // Un client peut avoir plusieurs comptes, on additionne
            result.merge(customerName, count, Long::sum);
        }

        return result;
    }


    public long countCurrentAccounts() {
        return bankAccountRepository.findAll().stream()
                .filter(account -> account instanceof CurrentAccount)
                .count();
    }

    public long countSavingAccounts() {
        return bankAccountRepository.findAll().stream()
                .filter(account -> account instanceof SavingAccount)
                .count();
    }


}
