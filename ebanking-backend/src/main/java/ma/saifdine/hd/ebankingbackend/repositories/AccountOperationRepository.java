package ma.saifdine.hd.ebankingbackend.repositories;

import ma.saifdine.hd.ebankingbackend.entities.AccountOperation;
import ma.saifdine.hd.ebankingbackend.enums.OperationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface AccountOperationRepository extends JpaRepository<AccountOperation,Long> {
    List<AccountOperation> findByBankAccountId(String id);

    Page<AccountOperation> findByBankAccountIdOrderByOperationDateDesc(String id, Pageable pageable);

    long countByType(OperationType type);

    // --- Méthode pour rechercher par compte, dates et montant ---
    @Query("""
       SELECT ao FROM AccountOperation ao
       WHERE ao.bankAccount.id = :accountId
         AND ao.operationDate >= :start
         AND ao.operationDate < :end
         AND ao.amount BETWEEN :minAmount AND :maxAmount
       ORDER BY ao.operationDate DESC
       """)
    Page<AccountOperation> searchOperations(
            @Param("accountId") String accountId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("minAmount") Double minAmount,
            @Param("maxAmount") Double maxAmount,
            Pageable pageable
    );

    @Query("""
    SELECT ao.bankAccount.id AS accountId, COUNT(ao) AS opCount 
    FROM AccountOperation ao
    GROUP BY ao.bankAccount.id
    ORDER BY COUNT(ao) DESC
""")
    List<Object[]> countOperationsGroupedByBankAccountId();

}
