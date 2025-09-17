package com.cs301.communication_service.services.impl;

import com.cs301.communication_service.services.*;
import com.cs301.communication_service.constants.*;
import com.cs301.communication_service.models.*;
import com.cs301.communication_service.repositories.*;
import com.cs301.communication_service.exceptions.*;
import com.cs301.communication_service.mappers.CommunicationMapper;
import com.cs301.communication_service.dtos.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CommunicationServiceImpl implements CommunicationService {
    
    private final CommunicationRepository communicationRepository;
    private final UserCommunicationRepository userCommunicationRepository;
    private final OtpCommunicationRepository otpCommunicationRepository;    
    private final AccountCommunicationRepository accountCommunicationRepository;
    private final CommunicationMapper communicationMapper;
    private final EmailService emailService;

    public CommunicationServiceImpl(CommunicationMapper communicationMapper, CommunicationRepository communicationRepository, EmailService emailService, UserCommunicationRepository userCommunicationRepository, OtpCommunicationRepository otpCommunicationRepository, AccountCommunicationRepository accountCommunicationRepository) {
        this.communicationRepository = communicationRepository;
        this.emailService = emailService;
        this.userCommunicationRepository = userCommunicationRepository;
        this.otpCommunicationRepository = otpCommunicationRepository;
        this.accountCommunicationRepository = accountCommunicationRepository;
        this.communicationMapper = communicationMapper;
    }

    @Override
    @Transactional
    public Communication createCommunication(Communication communication, CRUDInfo crudInfo) {
        Communication savedCommunication = communicationRepository.save(communication);

        // Send HTML email notification
        emailService.sendClientEmail(
            savedCommunication.getAgentId(),
            savedCommunication.getClientEmail(),
            savedCommunication.getClientId(),
            savedCommunication.getCrudType().toString(),
            savedCommunication.getSubject(),
            crudInfo.getAttribute(),
            crudInfo.getBeforeValue(),
            crudInfo.getAfterValue(),
            crudInfo.getTimeStamp()
        );

        return savedCommunication;
    }
    
    @Override
    @Transactional(readOnly = true)
    public CommunicationStatus getCommunicationStatus(UUID communicationId) {
        System.out.println(communicationId.toString());
        return communicationRepository.findById(communicationId)
            .map(Communication::getStatus)
            .orElseThrow(() -> new CommunicationNotFoundException(communicationId));
    }

    @Override
    @Transactional
    public UserCommunication createUserCommunication(UserCommunication communication) {
        UserCommunication savedCommunication = userCommunicationRepository.save(communication);

        // Send HTML email notification
        emailService.sendUserEmail(
            savedCommunication.getUsername(),
            savedCommunication.getUserRole(),
            savedCommunication.getUserEmail(),
            savedCommunication.getTempPassword(),
            savedCommunication.getSubject()
        );

        return savedCommunication;
    }

    @Override
    @Transactional
    public OtpCommunication createOtpCommunication(OtpCommunication communication) {
        OtpCommunication savedCommunication = otpCommunicationRepository.save(communication);

        // Send HTML email notification
        emailService.sendOtpEmail(
            savedCommunication.getEmail(),
            savedCommunication.getOtp(),
            savedCommunication.getSubject()
        );

        return savedCommunication;
    }

    @Override
    @Transactional
    public AccountCommunication createAccountCommunication(AccountCommunication communication) {
        AccountCommunication savedCommunication = accountCommunicationRepository.save(communication);

        // Send HTML email notification
        emailService.sendAccountEmail(
            savedCommunication.getAgentId(),
            savedCommunication.getClientEmail(),
            savedCommunication.getClientId(),
            savedCommunication.getCrudType().toString(),
            savedCommunication.getAccountId(),
            savedCommunication.getAccountType(),
            savedCommunication.getSubject()
        );

        return savedCommunication;
    }

    public List<RestCommunicationDTO> getRestCommunicationsDTOs(String userId) {

        List<Communication> communications = communicationRepository.findByAgentId(userId);
        List<AccountCommunication> accountCommunications = accountCommunicationRepository.findByAgentId(userId);

        return communicationMapper.getRestCommunicationDTOs(communications, accountCommunications);
    }

    public List<RestCommunicationDTO> getRestCommunicationsDTOs(String agentId, String searchQuery, int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by("timestamp").descending());
        
        Page<Communication> commPage;
        if (searchQuery != null && !searchQuery.isEmpty()) {
            commPage = communicationRepository.findByAgentIdAndSubjectContainingIgnoreCase(agentId, searchQuery, pageable);
        } else {
            commPage = communicationRepository.findByAgentId(agentId, pageable);
        }
        
        // For AccountCommunication, repeat similar steps:
        Page<AccountCommunication> accountCommPage;
        if (searchQuery != null && !searchQuery.isEmpty()) {
            accountCommPage = accountCommunicationRepository.findByAgentIdAndSubjectContainingIgnoreCase(agentId, searchQuery, pageable);
        } else {
            accountCommPage = accountCommunicationRepository.findByAgentId(agentId, pageable);
        }
        
        // Merge the lists, sort them, then apply pagination if needed
        List<RestCommunication> mergedList = new ArrayList<>();
        mergedList.addAll(commPage.getContent().stream()
                                .map(communicationMapper::communicationToRest)
                                .collect(Collectors.toList()));
        mergedList.addAll(accountCommPage.getContent().stream()
                                        .map(communicationMapper::accountCommunicationToRest)
                                        .collect(Collectors.toList()));
        
        // Sort merged list (if not already sorted by each query)
        mergedList.sort(Comparator.comparing(RestCommunication::getTimeStamp).reversed());
        
        // If the merged list size exceeds a page, you may need to create subList on mergedList.
        int fromIndex = (page - 1) * limit;
        int toIndex = Math.min(fromIndex + limit, mergedList.size());
        List<RestCommunication> paginatedMerged = (fromIndex < mergedList.size())
                ? mergedList.subList(fromIndex, toIndex)
                : Collections.emptyList();
        
        return communicationMapper.restToDTOList(paginatedMerged);
    }
}
