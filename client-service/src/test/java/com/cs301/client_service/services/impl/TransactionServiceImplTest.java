package com.cs301.client_service.services.impl;

import com.cs301.client_service.constants.TransactionStatus;
import com.cs301.client_service.dtos.TransactionDTO;
import com.cs301.client_service.exceptions.ApiException;
import com.cs301.client_service.mappers.TransactionMapper;
import com.cs301.client_service.models.Account;
import com.cs301.client_service.models.Client;
import com.cs301.client_service.models.Transaction;
import com.cs301.client_service.repositories.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private Transaction testTransaction;
    private TransactionDTO testTransactionDTO;
    private Client testClient;
    private Account testAccount;
    private final UUID transactionId = UUID.randomUUID();
    private final String clientId = "client-uuid";
    private final String accountId = "account-uuid";
    private final String agentId = "agent001";

    @BeforeEach
    void setUp() {
        // Setup test client
        testClient = Client.builder()
                .clientId(clientId)
                .firstName("John")
                .lastName("Doe")
                .agentId(agentId)
                .build();

        // Setup test account
        testAccount = new Account();
        testAccount.setAccountId(accountId);
        testAccount.setClient(testClient);

        // Setup test transaction
        testTransaction = Transaction.builder()
                .transactionId(transactionId)
                .client(testClient)
                .account(testAccount)
                .amount(new BigDecimal("100.00"))
                .status(TransactionStatus.COMPLETED)
                .timestamp(LocalDateTime.now())
                .description("Test transaction")
                .build();

        // Setup test transaction DTO
        testTransactionDTO = TransactionDTO.builder()
                .id(transactionId.toString())
                .clientId(clientId)
                .accountId(accountId)
                .amount(new BigDecimal("100.00"))
                .status(TransactionStatus.COMPLETED)
                .date(LocalDateTime.now())
                .description("Test transaction")
                .clientFirstName("John")
                .clientLastName("Doe")
                .build();
    }

    @Nested
    @DisplayName("Get Transaction By ID Tests")
    class GetTransactionByIdTests {
        @Test
        @DisplayName("Should successfully retrieve a transaction by ID")
        void testGetTransactionById_Success() {
            // Given
            when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(testTransaction));
            when(transactionMapper.toDTO(testTransaction)).thenReturn(testTransactionDTO);

            // When
            TransactionDTO result = transactionService.getTransactionById(transactionId);

            // Then
            assertThat(result)
                .isNotNull()
                .extracting(TransactionDTO::getId)
                .isEqualTo(transactionId.toString());
            verify(transactionRepository, times(1)).findById(transactionId);
            verify(transactionMapper, times(1)).toDTO(testTransaction);
        }

        @Test
        @DisplayName("Should throw ApiException when transaction not found")
        void testGetTransactionById_NotFound() {
            // Given
            UUID nonExistentId = UUID.randomUUID();
            when(transactionRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // When & Then
            ApiException exception = assertThrows(ApiException.class, () -> {
                transactionService.getTransactionById(nonExistentId);
            });
            
            // Verify the exception message contains the ID
            assertThat(exception.getMessage()).contains(nonExistentId.toString());
            verify(transactionRepository, times(1)).findById(nonExistentId);
            verify(transactionMapper, never()).toDTO(any(Transaction.class));
        }
    }

    @Nested
    @DisplayName("Get Transactions By Client ID Tests")
    class GetTransactionsByClientIdTests {
        @Test
        @DisplayName("Should retrieve transactions for a client with search query")
        void testGetTransactionsByClientId_WithSearchQuery() {
            // Given
            int page = 0;
            int limit = 10;
            String searchQuery = "test";
            Pageable pageable = PageRequest.of(page, limit);
            
            Transaction anotherTransaction = Transaction.builder()
                    .transactionId(UUID.randomUUID())
                    .client(testClient)
                    .account(testAccount)
                    .amount(new BigDecimal("200.00"))
                    .status(TransactionStatus.PENDING)
                    .timestamp(LocalDateTime.now())
                    .description("Another test transaction")
                    .build();
            
            List<Transaction> transactions = Arrays.asList(testTransaction, anotherTransaction);
            Page<Transaction> transactionPage = new PageImpl<>(transactions, pageable, 2);
            
            TransactionDTO anotherTransactionDTO = TransactionDTO.builder()
                    .id(anotherTransaction.getTransactionId().toString())
                    .clientId(clientId)
                    .accountId(accountId)
                    .amount(new BigDecimal("200.00"))
                    .status(TransactionStatus.PENDING)
                    .date(LocalDateTime.now())
                    .description("Another test transaction")
                    .clientFirstName("John")
                    .clientLastName("Doe")
                    .build();
            
            List<TransactionDTO> transactionDTOs = Arrays.asList(testTransactionDTO, anotherTransactionDTO);
            
            when(transactionRepository.searchByClientId(clientId, searchQuery, pageable))
                    .thenReturn(transactionPage);
            when(transactionMapper.toDTOList(transactions)).thenReturn(transactionDTOs);

            // When
            List<TransactionDTO> results = transactionService.getTransactionsByClientId(clientId, searchQuery, page, limit);

            // Then
            assertThat(results)
                .isNotEmpty()
                .hasSize(2)
                .extracting(TransactionDTO::getId)
                .containsExactly(transactionId.toString(), anotherTransaction.getTransactionId().toString());
            verify(transactionRepository, times(1)).searchByClientId(clientId, searchQuery, pageable);
            verify(transactionMapper, times(1)).toDTOList(transactions);
        }

        @Test
        @DisplayName("Should retrieve transactions for a client without search query")
        void testGetTransactionsByClientId_WithoutSearchQuery() {
            // Given
            int page = 0;
            int limit = 10;
            String searchQuery = null;
            Pageable pageable = PageRequest.of(page, limit);
            
            List<Transaction> transactions = Arrays.asList(testTransaction);
            Page<Transaction> transactionPage = new PageImpl<>(transactions, pageable, 1);
            
            List<TransactionDTO> transactionDTOs = Arrays.asList(testTransactionDTO);
            
            when(transactionRepository.findByClientClientId(clientId, pageable))
                    .thenReturn(transactionPage);
            when(transactionMapper.toDTOList(transactions)).thenReturn(transactionDTOs);

            // When
            List<TransactionDTO> results = transactionService.getTransactionsByClientId(clientId, searchQuery, page, limit);

            // Then
            assertThat(results)
                .isNotEmpty()
                .hasSize(1)
                .extracting(TransactionDTO::getId)
                .containsExactly(transactionId.toString());
            verify(transactionRepository, times(1)).findByClientClientId(clientId, pageable);
            verify(transactionMapper, times(1)).toDTOList(transactions);
        }
    }

    @Nested
    @DisplayName("Get Transactions By Agent ID Tests")
    class GetTransactionsByAgentIdTests {
        @Test
        @DisplayName("Should retrieve transactions for an agent with search query")
        void testGetTransactionsByAgentId_WithSearchQuery() {
            // Given
            int page = 0;
            int limit = 10;
            String searchQuery = "test";
            Pageable pageable = PageRequest.of(page, limit);
            
            Transaction anotherTransaction = Transaction.builder()
                    .transactionId(UUID.randomUUID())
                    .client(testClient)
                    .account(testAccount)
                    .amount(new BigDecimal("200.00"))
                    .status(TransactionStatus.PENDING)
                    .timestamp(LocalDateTime.now())
                    .description("Another test transaction")
                    .build();
            
            List<Transaction> transactions = Arrays.asList(testTransaction, anotherTransaction);
            Page<Transaction> transactionPage = new PageImpl<>(transactions, pageable, 2);
            
            TransactionDTO anotherTransactionDTO = TransactionDTO.builder()
                    .id(anotherTransaction.getTransactionId().toString())
                    .clientId(clientId)
                    .accountId(accountId)
                    .amount(new BigDecimal("200.00"))
                    .status(TransactionStatus.PENDING)
                    .date(LocalDateTime.now())
                    .description("Another test transaction")
                    .clientFirstName("John")
                    .clientLastName("Doe")
                    .build();
            
            List<TransactionDTO> transactionDTOs = Arrays.asList(testTransactionDTO, anotherTransactionDTO);
            
            when(transactionRepository.searchByAgentId(agentId, searchQuery, pageable))
                    .thenReturn(transactionPage);
            when(transactionMapper.toDTOList(transactions)).thenReturn(transactionDTOs);

            // When
            List<TransactionDTO> results = transactionService.getTransactionsByAgentId(agentId, searchQuery, page, limit);

            // Then
            assertThat(results)
                .isNotEmpty()
                .hasSize(2)
                .extracting(TransactionDTO::getId)
                .containsExactly(transactionId.toString(), anotherTransaction.getTransactionId().toString());
            verify(transactionRepository, times(1)).searchByAgentId(agentId, searchQuery, pageable);
            verify(transactionMapper, times(1)).toDTOList(transactions);
        }

        @Test
        @DisplayName("Should retrieve transactions for an agent without search query")
        void testGetTransactionsByAgentId_WithoutSearchQuery() {
            // Given
            int page = 0;
            int limit = 10;
            String searchQuery = null;
            Pageable pageable = PageRequest.of(page, limit);
            
            List<Transaction> transactions = Arrays.asList(testTransaction);
            Page<Transaction> transactionPage = new PageImpl<>(transactions, pageable, 1);
            
            List<TransactionDTO> transactionDTOs = Arrays.asList(testTransactionDTO);
            
            when(transactionRepository.findByClientAgentId(agentId, pageable))
                    .thenReturn(transactionPage);
            when(transactionMapper.toDTOList(transactions)).thenReturn(transactionDTOs);

            // When
            List<TransactionDTO> results = transactionService.getTransactionsByAgentId(agentId, searchQuery, page, limit);

            // Then
            assertThat(results)
                .isNotEmpty()
                .hasSize(1)
                .extracting(TransactionDTO::getId)
                .containsExactly(transactionId.toString());
            verify(transactionRepository, times(1)).findByClientAgentId(agentId, pageable);
            verify(transactionMapper, times(1)).toDTOList(transactions);
        }
    }

    @Nested
    @DisplayName("Get Transactions By Account ID Tests")
    class GetTransactionsByAccountIdTests {
        @Test
        @DisplayName("Should retrieve transactions for an account")
        void testGetTransactionsByAccountId_Success() {
            // Given
            List<Transaction> transactions = Arrays.asList(testTransaction);
            List<TransactionDTO> transactionDTOs = Arrays.asList(testTransactionDTO);
            
            when(transactionRepository.findByAccountAccountId(accountId)).thenReturn(transactions);
            when(transactionMapper.toDTOList(transactions)).thenReturn(transactionDTOs);

            // When
            List<TransactionDTO> results = transactionService.getTransactionsByAccountId(accountId);

            // Then
            assertThat(results)
                .isNotEmpty()
                .hasSize(1)
                .extracting(TransactionDTO::getId)
                .containsExactly(transactionId.toString());
            verify(transactionRepository, times(1)).findByAccountAccountId(accountId);
            verify(transactionMapper, times(1)).toDTOList(transactions);
        }
    }

    @Nested
    @DisplayName("Get Transactions By Status Tests")
    class GetTransactionsByStatusTests {
        @Test
        @DisplayName("Should retrieve transactions by status")
        void testGetTransactionsByStatus_Success() {
            // Given
            List<Transaction> transactions = Arrays.asList(testTransaction);
            List<TransactionDTO> transactionDTOs = Arrays.asList(testTransactionDTO);
            
            when(transactionRepository.findByStatus(TransactionStatus.COMPLETED)).thenReturn(transactions);
            when(transactionMapper.toDTOList(transactions)).thenReturn(transactionDTOs);

            // When
            List<TransactionDTO> results = transactionService.getTransactionsByStatus(TransactionStatus.COMPLETED);

            // Then
            assertThat(results)
                .isNotEmpty()
                .hasSize(1)
                .extracting(TransactionDTO::getId)
                .containsExactly(transactionId.toString());
            verify(transactionRepository, times(1)).findByStatus(TransactionStatus.COMPLETED);
            verify(transactionMapper, times(1)).toDTOList(transactions);
        }
    }
}
