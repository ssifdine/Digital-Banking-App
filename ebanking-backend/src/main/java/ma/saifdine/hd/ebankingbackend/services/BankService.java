package ma.saifdine.hd.ebankingbackend.services;

import ma.saifdine.hd.ebankingbackend.entities.BankAccount;
import ma.saifdine.hd.ebankingbackend.entities.CurrentAccount;
import ma.saifdine.hd.ebankingbackend.entities.SavingAccount;
import ma.saifdine.hd.ebankingbackend.repositories.BankAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BankService {

    @Autowired
    BankAccountRepository bankAccountRepository;

    public void consulter(){
        BankAccount account =
                bankAccountRepository.findById("05703c6a-e5a2-4a3f-9daf-29a4fd6a0f1a").orElse(null);

        if(account != null){


            System.out.println("*********************************************************");
            System.out.println(account.getId());
            System.out.println(account.getBalance());
            System.out.println(account.getStatus());
//            System.out.println(account.getCreatedAt());
            System.out.println(account.getCustomer().getName());
            System.out.println(account.getClass().getSimpleName());
            if(account instanceof CurrentAccount){
                System.out.println("Over Draft =>" + ((CurrentAccount) account).getOverDraft());
            } else if (account instanceof SavingAccount) {
                System.out.println("Rate =>" + ((SavingAccount) account).getInterestRate());

            }
            account.getAccountOperations().forEach(accountOperation -> {
                System.out.println(accountOperation.getType()+ "\t"+accountOperation.getOperationDate()+"\t"+accountOperation.getAmount());
            });
        }
    }


}
