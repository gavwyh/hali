package com.cs301.client_service.mappers;

import com.cs301.client_service.constants.AccountStatus;
import com.cs301.client_service.constants.AccountType;
import com.cs301.client_service.dtos.AccountDTO;
import com.cs301.client_service.models.Account;
import com.cs301.client_service.models.Client;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@org.junit.jupiter.api.Disabled("Disabled for unit testing")
class AccountMapperTest {

    @InjectMocks
    private AccountMapper accountMapper;

    private Account accountModel;
    private AccountDTO accountDTO;
    private String accountId = "account-uuid";
    private String clientId = "client-uuid";
    private String clientFirstName = "John";
    private String clientLastName = "Doe";

    @BeforeEach
    void setUp() {
        // Setup client
        Client client = new Client();
        client.setClientId(clientId);
        client.setFirstName(clientFirstName);
        client.setLastName(clientLastName);

        // Setup account model
        accountModel = new Account();
        accountModel.setAccountId(accountId);
        accountModel.setClient(client);
        accountModel.setAccountType(AccountType.SAVINGS);
        accountModel.setAccountStatus(AccountStatus.ACTIVE);
        accountModel.setOpeningDate(LocalDate.now());
        accountModel.setInitialDeposit(new BigDecimal("1000.00"));
        accountModel.setCurrency("SGD");
        accountModel.setBranchId("BR001");

        // Setup account DTO
        accountDTO = AccountDTO.builder()
                .accountId(accountId)
                .clientId(clientId)
                .accountType(AccountType.SAVINGS)
                .accountStatus(AccountStatus.ACTIVE)
                .openingDate(LocalDate.now().toString())
                .initialDeposit(new BigDecimal("1000.00"))
                .currency("SGD")
                .branchId("BR001")
                .build();
    }

    @Nested
    @DisplayName("To DTO Tests")
    class ToDtoTests {
        @Test
        @DisplayName("Should successfully convert model to DTO")
        void testToDto() {
            // Act
            AccountDTO result = accountMapper.toDto(accountModel);

            // Assert
            assertNotNull(result);
            assertEquals(accountId, result.getAccountId());
            assertEquals(clientId, result.getClientId());
            assertEquals(clientFirstName + " " + clientLastName, result.getClientName());
            assertEquals(AccountType.SAVINGS, result.getAccountType());
            assertEquals(AccountStatus.ACTIVE, result.getAccountStatus());
            assertEquals(accountModel.getOpeningDate().toString(), result.getOpeningDate());
            assertEquals(new BigDecimal("1000.00"), result.getInitialDeposit());
            assertEquals("SGD", result.getCurrency());
            assertEquals("BR001", result.getBranchId());
        }

        @Test
        @DisplayName("Should return null when model is null")
        void testToDtoWithNullModel() {
            // Act
            AccountDTO result = accountMapper.toDto(null);

            // Assert
            assertNull(result);
        }

        @Test
        @DisplayName("Should handle null client in model")
        void testToDtoWithNullClient() {
            // Arrange
            accountModel.setClient(null);

            // Act
            AccountDTO result = accountMapper.toDto(accountModel);

            // Assert
            assertNotNull(result);
            assertNull(result.getClientId());
            assertEquals("", result.getClientName());
        }
    }

    @Nested
    @DisplayName("To Model Tests")
    class ToModelTests {
        @Test
        @DisplayName("Should successfully convert DTO to model")
        void testToModel() {
            // Act
            Account result = accountMapper.toModel(accountDTO);

            // Assert
            assertNotNull(result);
            assertEquals(accountId, result.getAccountId());
            assertNotNull(result.getClient());
            assertEquals(clientId, result.getClient().getClientId());
            assertEquals(AccountType.SAVINGS, result.getAccountType());
            assertEquals(AccountStatus.ACTIVE, result.getAccountStatus());
            assertEquals(LocalDate.parse(accountDTO.getOpeningDate()), result.getOpeningDate());
            assertEquals(new BigDecimal("1000.00"), result.getInitialDeposit());
            assertEquals("SGD", result.getCurrency());
            assertEquals("BR001", result.getBranchId());
        }

        @Test
        @DisplayName("Should return null when DTO is null")
        void testToModelWithNullDto() {
            // Act
            Account result = accountMapper.toModel(null);

            // Assert
            assertNull(result);
        }
    }

    @Nested
    @DisplayName("List Conversion Tests")
    class ListConversionTests {
        @Test
        @DisplayName("Should successfully convert model list to DTO list")
        void testToDtoList() {
            // Arrange
            List<Account> accounts = Arrays.asList(accountModel);

            // Act
            List<AccountDTO> result = accountMapper.toDtoList(accounts);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            AccountDTO firstResult = result.get(0);
            assertEquals(accountId, firstResult.getAccountId());
            assertEquals(clientId, firstResult.getClientId());
            assertEquals(clientFirstName + " " + clientLastName, firstResult.getClientName());
        }

        @Test
        @DisplayName("Should successfully convert DTO list to model list")
        void testToModelList() {
            // Arrange
            List<AccountDTO> dtos = Arrays.asList(accountDTO);

            // Act
            List<Account> result = accountMapper.toModelList(dtos);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            Account firstResult = result.get(0);
            assertEquals(accountId, firstResult.getAccountId());
            assertNotNull(firstResult.getClient());
            assertEquals(clientId, firstResult.getClient().getClientId());
        }
    }
} 