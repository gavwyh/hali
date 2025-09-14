INSERT INTO client_communications (
    id, agent_id, client_id, client_email, crud_type, subject, status, timestamp
) VALUES (
    random_uuid(),
    'agent_123', 
    'client_456', 
    'client@example.com', 
    'CREATE',  
    'Welcome Email',  
    'SENT',  
    CURRENT_TIMESTAMP
);

INSERT INTO account_communications (
    id, agent_id, client_id, client_email, account_id, account_type, crud_type, subject, status, timestamp
) VALUES (
    random_uuid(),
    'agent_123', 
    'client_456', 
    'client@example.com',
    'CLIENT456_STE110067',
    'PERSONAL', 
    'CREATE',  
    'Welcome Email',  
    'SENT',  
    CURRENT_TIMESTAMP
);

INSERT INTO user_communications (
    id, username, user_role, user_email, temp_password, subject, status, timestamp
) VALUES (
    random_uuid(),
    'agent_1234', 
    'AGENT', 
    'agent@example.com', 
    '12i4yn1uxg1ui338',  
    'Welcome Email',  
    'SENT',  
    CURRENT_TIMESTAMP
);

INSERT INTO otp_communications (
    id, email, otp, subject, status, timestamp
) VALUES (
    random_uuid(),
    'agent@example.com', 
    923474,  
    'Welcome Email',  
    'SENT',  
    CURRENT_TIMESTAMP
);