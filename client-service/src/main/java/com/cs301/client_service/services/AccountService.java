package com.cs301.client_service.services;

import com.cs301.client_service.constants.AccountStatus;
import com.cs301.client_service.constants.AccountType;
import com.cs301.client_service.models.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface AccountService {
    Account createAccount(Account account);
    Account getAccount(String accountId);
    List<Account> getAccountsByClientId(String clientId);
    Page<Account> getAccountsByClientIdPaginated(String clientId, Pageable pageable);
    Page<Account> getAllAccountsPaginated(Pageable pageable, AccountType type, AccountStatus status);
    Page<Account> getAccountsWithSearchAndFilters(String agentId, String searchQuery, AccountType type, AccountStatus status, Pageable pageable);
    void deleteAccount(String accountId);
    void deleteAccountsByClientId(String clientId);
}
