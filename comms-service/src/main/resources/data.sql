INSERT INTO client_communications (
    id, agent_id, client_id, client_email, crud_type, subject, status, timestamp
) VALUES (
    gen_random_uuid(),
    'agent_123', 
    'client_456', 
    'client@example.com', 
    'CREATE'::VARCHAR,  
    'Welcome Email client 1'::VARCHAR,  
    'SENT'::VARCHAR,  
    NOW()
);

INSERT INTO client_communications (
    id, agent_id, client_id, client_email, crud_type, subject, status, timestamp
) VALUES (
    gen_random_uuid(),
    'agent_122', 
    'client_456', 
    'client@example.com', 
    'CREATE'::VARCHAR,  
    'Welcome Email client 2'::VARCHAR,  
    'SENT'::VARCHAR,  
    NOW()
);

INSERT INTO account_communications (
    id, agent_id, client_id, client_email, account_id, account_type, crud_type, subject, status, timestamp
) VALUES (
    gen_random_uuid(),
    'agent_123', 
    'client_456', 
    'client@example.com',
    'CLIENT456_STE110067',
    'PERSONAL', 
    'CREATE'::VARCHAR,  
    'Welcome Email account 3'::VARCHAR,  
    'SENT'::VARCHAR,  
    NOW()
);

INSERT INTO account_communications (
    id, agent_id, client_id, client_email, account_id, account_type, crud_type, subject, status, timestamp
) VALUES (
    gen_random_uuid(),
    'agent_122', 
    'client_456', 
    'client@example.com',
    'CLIENT456_STE110067',
    'PERSONAL', 
    'CREATE'::VARCHAR,  
    'Welcome Email account 4'::VARCHAR,  
    'SENT'::VARCHAR,  
    NOW()
);

INSERT INTO user_communications (
    id, username, user_role, user_email, temp_password, subject, status, timestamp
) VALUES (
    gen_random_uuid(),
    'agent_1234', 
    'AGENT', 
    'agent@example.com', 
    '12i4yn1uxg1ui338'::VARCHAR,  
    'Welcome Email user'::VARCHAR,  
    'SENT'::VARCHAR,  
    NOW()
);

INSERT INTO otp_communications (
    id, email, otp, subject, status, timestamp
) VALUES (
    gen_random_uuid(),
    'agent@example.com', 
    923474,  
    'Welcome Email otp'::VARCHAR,  
    'SENT'::VARCHAR,  
    NOW()
);
