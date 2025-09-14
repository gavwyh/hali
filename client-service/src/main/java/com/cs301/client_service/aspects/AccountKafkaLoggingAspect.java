package com.cs301.client_service.aspects;

import com.cs301.client_service.aspects.base.KafkaLoggingAspect;
import com.cs301.client_service.models.Account;
import com.cs301.client_service.producers.KafkaProducer;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * Aspect for logging account service operations to Kafka using A2C format.
 */
@Aspect
@Component
public class AccountKafkaLoggingAspect extends KafkaLoggingAspect {

    public AccountKafkaLoggingAspect(KafkaProducer kafkaProducer) {
        super(kafkaProducer);
    }

    @Override
    protected String getEntityId(Object entity) {
        if (entity instanceof Account) {
            return ((Account) entity).getAccountId();
        }
        return null;
    }

    @Override
    protected String getClientId(Object entity) {
        if (entity instanceof Account && ((Account) entity).getClient() != null) {
            return ((Account) entity).getClient().getClientId();
        }
        return null;
    }

    @Override
    protected String getClientEmail(Object entity) {
        if (entity instanceof Account && ((Account) entity).getClient() != null) {
            return ((Account) entity).getClient().getEmailAddress();
        }
        return null;
    }

    @Override
    protected String getEntityType() {
        return "Account";
    }

    @Override
    protected String getAttributeNames(Object entity) {
        if (entity instanceof Account) {
            return "accountId,clientId,accountType,accountStatus,openingDate,initialDeposit,currency,branchId";
        }
        return "";
    }
    
    @Override
    protected String getEntityValues(Object entity) {
        if (entity instanceof Account account) {
            return String.format("%s,%s,%s,%s,%s,%s,%s,%s",
                account.getAccountId(),
                account.getClient().getClientId(),
                account.getAccountType(),
                account.getAccountStatus(),
                account.getOpeningDate(),
                account.getInitialDeposit(),
                account.getCurrency(),
                account.getBranchId()
            );
        }
        return "";
    }

    /**
     * Pointcut for account creation
     */
    @Pointcut("execution(* com.cs301.client_service.services.impl.AccountServiceImpl.createAccount(..))")
    public void accountCreation() {}

    /**
     * Log after account creation to Kafka
     */
    @AfterReturning(pointcut = "accountCreation()", returning = "result")
    public void logAfterAccountCreation(JoinPoint joinPoint, Account result) {
        logAfterCreationToKafka(joinPoint, result);
    }
}
