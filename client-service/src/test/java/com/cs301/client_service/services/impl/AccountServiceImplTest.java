package com.cs301.client_service.services.impl;

import com.cs301.client_service.constants.AccountStatus;
import com.cs301.client_service.constants.AccountType;
import com.cs301.client_service.constants.VerificationStatus;
import com.cs301.client_service.exceptions.AccountNotFoundException;
import com.cs301.client_service.exceptions.ClientNotFoundException;
import com.cs301.client_service.models.Account;
import com.cs301.client_service.models.Client;
import com.cs301.client_service.repositories.AccountRepository;
import com.cs301.client_service.repositories.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private AccountServiceImpl accountService;

    private Client testClient;
    private Account testAccount;
    private String clientId = "client-uuid";
    private String accountId = "account-uuid";

    @BeforeEach
    void setUp() {
        // Setup test client
        testClient = new Client();
        testClient.setClientId(clientId);
        testClient.setFirstName("John");
        testClient.setLastName("Doe");
        testClient.setVerificationStatus(VerificationStatus.VERIFIED);

        // Setup test account
        testAccount = new Account();
        testAccount.setAccountId(accountId);
        testAccount.setClient(testClient);
        testAccount.setAccountType(AccountType.SAVINGS);
        testAccount.setAccountStatus(AccountStatus.ACTIVE);
        testAccount.setOpeningDate(LocalDate.now());
        testAccount.setInitialDeposit(new BigDecimal("1000.00"));
        testAccount.setCurrency("SGD");
        testAccount.setBranchId("BR001");
    }

    @Test
    void testCreateAccount_Success() {
        // Given
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(testClient));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        // When
        Account account = new Account();
        account.setClient(testClient);
        Account result = accountService.createAccount(account);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAccountId()).isEqualTo(accountId);
        assertThat(result.getClient().getClientId()).isEqualTo(clientId);
        verify(clientRepository, times(1)).findById(clientId);
        verify(accountRepository, times(1)).save(account);
    }

    @Test
    void testCreateAccount_ClientNotFound() {
        // Given
        when(clientRepository.findById(anyString())).thenReturn(Optional.empty());

        // When & Then
        Account account = new Account();
        account.setClient(testClient);
        ClientNotFoundException exception = assertThrows(ClientNotFoundException.class, () -> {
            accountService.createAccount(account);
        });
        
        // Verify the exception message contains the ID
        assertThat(exception.getMessage()).contains(clientId);
        verify(clientRepository, times(1)).findById(clientId);
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void testGetAccount_Success() {
        // Given
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(testAccount));

        // When
        Account result = accountService.getAccount(accountId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAccountId()).isEqualTo(accountId);
        verify(accountRepository, times(1)).findById(accountId);
    }

    @Test
    void testGetAccount_NotFound() {
        // Given
        String nonExistentId = "non-existent-id";
        when(accountRepository.findById(anyString())).thenReturn(Optional.empty());

        // When & Then
        AccountNotFoundException exception = assertThrows(AccountNotFoundException.class, () -> {
            accountService.getAccount(nonExistentId);
        });
        
        // Verify the exception message contains the ID
        assertThat(exception.getMessage()).contains(nonExistentId);
        verify(accountRepository, times(1)).findById(nonExistentId);
    }

    @Test
    void testGetAccountsByClientId_Success() {
        // Given
        when(clientRepository.existsById(clientId)).thenReturn(true);
        when(accountRepository.findByClientClientId(clientId)).thenReturn(Arrays.asList(testAccount));

        // When
        List<Account> results = accountService.getAccountsByClientId(clientId);

        // Then
        assertThat(results)
            .isNotEmpty()
            .hasSize(1)
            .extracting(Account::getAccountId)
            .containsExactly(accountId);
        verify(clientRepository, times(1)).existsById(clientId);
        verify(accountRepository, times(1)).findByClientClientId(clientId);
    }

    @Test
    void testGetAccountsByClientId_ClientNotFound() {
        // Given
        String nonExistentId = "non-existent-id";
        when(clientRepository.existsById(anyString())).thenReturn(false);

        // When & Then
        ClientNotFoundException exception = assertThrows(ClientNotFoundException.class, () -> {
            accountService.getAccountsByClientId(nonExistentId);
        });
        
        // Verify the exception message contains the ID
        assertThat(exception.getMessage()).contains(nonExistentId);
        verify(clientRepository, times(1)).existsById(nonExistentId);
        verify(accountRepository, never()).findByClientClientId(anyString());
    }

    @Test
    void testGetAccountsByClientId_NoAccounts() {
        // Given
        when(clientRepository.existsById(clientId)).thenReturn(true);
        when(accountRepository.findByClientClientId(clientId)).thenReturn(Collections.emptyList());

        // When
        List<Account> results = accountService.getAccountsByClientId(clientId);

        // Then
        assertThat(results).isEmpty();
        verify(clientRepository, times(1)).existsById(clientId);
        verify(accountRepository, times(1)).findByClientClientId(clientId);
    }

    @Test
    void testDeleteAccount_Success() {
        // Given
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(testAccount));
        doNothing().when(accountRepository).deleteById(accountId);

        // When
        accountService.deleteAccount(accountId);

        // Then
        verify(accountRepository, times(1)).findById(accountId);
        verify(accountRepository, times(1)).deleteById(accountId);
    }

    @Test
    void testDeleteAccount_AccountNotFound() {
        // Given
        String nonExistentId = "non-existent-id";
        when(accountRepository.findById(anyString())).thenReturn(Optional.empty());

        // When & Then
        AccountNotFoundException exception = assertThrows(AccountNotFoundException.class, () -> {
            accountService.deleteAccount(nonExistentId);
        });
        
        // Verify the exception message contains the ID
        assertThat(exception.getMessage()).contains(nonExistentId);
        verify(accountRepository, times(1)).findById(nonExistentId);
        verify(accountRepository, never()).deleteById(anyString());
    }

    @Test
    void testDeleteAccountsByClientId_Success() {
        // Given
        when(clientRepository.existsById(clientId)).thenReturn(true);
        when(accountRepository.findByClientClientId(clientId)).thenReturn(Arrays.asList(testAccount));
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(testAccount));
        doNothing().when(accountRepository).deleteById(accountId);

        // When
        accountService.deleteAccountsByClientId(clientId);

        // Then
        verify(clientRepository, times(1)).existsById(clientId);
        verify(accountRepository, times(1)).findByClientClientId(clientId);
        verify(accountRepository, times(1)).findById(accountId);
        verify(accountRepository, times(1)).deleteById(accountId);
    }

    @Test
    void testDeleteAccountsByClientId_ClientNotFound() {
        // Given
        String nonExistentId = "non-existent-id";
        when(clientRepository.existsById(anyString())).thenReturn(false);

        // When & Then
        ClientNotFoundException exception = assertThrows(ClientNotFoundException.class, () -> {
            accountService.deleteAccountsByClientId(nonExistentId);
        });
        
        // Verify the exception message contains the ID
        assertThat(exception.getMessage()).contains(nonExistentId);
        verify(clientRepository, times(1)).existsById(nonExistentId);
        verify(accountRepository, never()).deleteByClientClientId(anyString());
    }
}
