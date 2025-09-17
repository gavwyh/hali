package com.cs301.client_service.repositories;

import com.cs301.client_service.constants.AccountStatus;
import com.cs301.client_service.constants.AccountType;
import com.cs301.client_service.constants.Gender;
import com.cs301.client_service.models.Account;
import com.cs301.client_service.models.Client;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class AccountRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AccountRepository accountRepository;

    private Client testClient;
    private Account testAccount;

    @BeforeEach
    void setup() {
        // Create a client
        testClient = new Client();
        testClient.setFirstName("John");
        testClient.setLastName("Doe");
        testClient.setDateOfBirth(LocalDate.of(1990, 1, 1));
        testClient.setGender(Gender.MALE);
        testClient.setEmailAddress("john.doe@example.com");
        testClient.setPhoneNumber("1234567890");
        testClient.setAddress("123 Main St");
        testClient.setCity("Singapore");
        testClient.setState("Singapore");
        testClient.setCountry("Singapore");
        testClient.setPostalCode("123456");
        testClient.setNric("S1234567A");
        testClient.setAgentId("test-agent001");

        // Persist the client
        entityManager.persist(testClient);
        entityManager.flush();

        // Create an account
        testAccount = new Account();
        testAccount.setClient(testClient);
        testAccount.setAccountType(AccountType.SAVINGS);
        testAccount.setAccountStatus(AccountStatus.ACTIVE);
        testAccount.setOpeningDate(LocalDate.now());
        testAccount.setInitialDeposit(new BigDecimal("1000.00"));
        testAccount.setCurrency("SGD");
        testAccount.setBranchId("BR001");

        // Persist the account
        entityManager.persist(testAccount);
        entityManager.flush();
    }

    @Test
    void testFindByClientClientId() {
        // When: finding accounts by client ID
        List<Account> foundAccounts = accountRepository.findByClientClientId(testClient.getClientId());

        // Then: the account should be found
        assertThat(foundAccounts)
            .isNotEmpty()
            .hasSize(1)
            .extracting(Account::getAccountId)
            .containsExactly(testAccount.getAccountId());
    }

    @Test
    void testFindByClientClientId_NoAccounts() {
        // Given: a client with no accounts
        Client anotherClient = new Client();
        anotherClient.setFirstName("Jane");
        anotherClient.setLastName("Smith");
        anotherClient.setDateOfBirth(LocalDate.of(1992, 2, 2));
        anotherClient.setGender(Gender.FEMALE);
        anotherClient.setEmailAddress("jane.smith@example.com");
        anotherClient.setPhoneNumber("0987654321");
        anotherClient.setAddress("456 Side St");
        anotherClient.setCity("Singapore");
        anotherClient.setState("Singapore");
        anotherClient.setCountry("Singapore");
        anotherClient.setPostalCode("654321");
        anotherClient.setNric("S7654321A");
        anotherClient.setAgentId("test-agent002");

        entityManager.persist(anotherClient);
        entityManager.flush();

        // When: finding accounts by client ID
        List<Account> foundAccounts = accountRepository.findByClientClientId(anotherClient.getClientId());

        // Then: no accounts should be found
        assertThat(foundAccounts).isEmpty();
    }

    @Test
    void testDeleteByClientClientId() {
        // When: deleting accounts by client ID
        accountRepository.deleteByClientClientId(testClient.getClientId());
        entityManager.flush();

        // Then: only the accounts for the specified client should be deleted
        List<Account> remainingAccounts = accountRepository.findByClientClientId(testClient.getClientId());
        assertThat(remainingAccounts).isEmpty();
    }
}
