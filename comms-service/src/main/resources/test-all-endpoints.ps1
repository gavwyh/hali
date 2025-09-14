# test-all-endpoints.ps1
# PowerShell script to test all endpoints (clients, accounts, logs) and show the messages

# Set variables
$BASE_URL = "http://localhost:8081/api/v1"
$CLIENT_ID = ""
$ACCOUNT_ID = ""

Write-Host "Testing all endpoints (clients, accounts, logs)..."

# Generate a unique email address with timestamp
$TIMESTAMP = (Get-Date -UFormat %s)
$UNIQUE_EMAIL = "fqteo.2023@scis.smu.edu.sg"
$UNIQUE_NRIC = "S" + $TIMESTAMP.Substring(0,7) + "D"

# === 1. Creating a test client ===
Write-Host "=== 1. Creating a test client ==="
$clientBody = @{
    firstName    = "Test"
    lastName     = "Client"
    dateOfBirth  = "1990-01-01"
    gender       = "MALE"
    emailAddress = $UNIQUE_EMAIL
    phoneNumber  = "1234567890"
    address      = "123 Test St"
    city         = "Singapore"
    state        = "Singapore"
    country      = "Singapore"
    postalCode   = "123456"
    nric         = $UNIQUE_NRIC
    agentId      = "test-agent"
}
$clientJson = $clientBody | ConvertTo-Json -Depth 10

try {
    $CLIENT_RESPONSE = Invoke-RestMethod -Method Post -Uri "$BASE_URL/clients" -Body $clientJson -ContentType "application/json"
} catch {
    Write-Host "Error creating client:" $_.Exception.Message
    exit 1
}
Write-Host "Client creation response:"
$CLIENT_RESPONSE | ConvertTo-Json -Depth 10 | Write-Host

$CLIENT_ID = $CLIENT_RESPONSE.clientId

if (-not $CLIENT_ID) {
    Write-Host "Failed to create test client"
    exit 1
}
Write-Host "Created test client with ID: $CLIENT_ID"

# === 2. Getting the client by ID ===
Write-Host "=== 2. Getting the client by ID ==="
try {
    $GET_CLIENT_RESPONSE = Invoke-RestMethod -Method Get -Uri "$BASE_URL/clients/$CLIENT_ID"
    Write-Host "Get client response:"
    $GET_CLIENT_RESPONSE | ConvertTo-Json -Depth 10 | Write-Host
} catch {
    Write-Host "Error getting client by ID:" $_.Exception.Message
}

# === 3. Getting all clients ===
Write-Host "=== 3. Getting all clients ==="
try {
    $GET_ALL_CLIENTS_RESPONSE = Invoke-RestMethod -Method Get -Uri "$BASE_URL/clients"
    $allClientsJson = $GET_ALL_CLIENTS_RESPONSE | ConvertTo-Json -Depth 10
    Write-Host "Get all clients response (showing first 200 characters):"
    if ($allClientsJson.Length -gt 200) {
        Write-Host $allClientsJson.Substring(0,200) "..."
    } else {
        Write-Host $allClientsJson
    }
} catch {
    Write-Host "Error getting all clients:" $_.Exception.Message
}

# === 4. Getting clients by agent ID ===
Write-Host "=== 4. Getting clients by agent ID ==="
try {
    $GET_CLIENTS_BY_AGENT_RESPONSE = Invoke-RestMethod -Method Get -Uri "$BASE_URL/clients/agent/test-agent"
    $agentClientsJson = $GET_CLIENTS_BY_AGENT_RESPONSE | ConvertTo-Json -Depth 10
    Write-Host "Get clients by agent response (showing first 200 characters):"
    if ($agentClientsJson.Length -gt 200) {
        Write-Host $agentClientsJson.Substring(0,200) "..."
    } else {
        Write-Host $agentClientsJson
    }
} catch {
    Write-Host "Error getting clients by agent ID:" $_.Exception.Message
}

# === 5. Verifying the client ===
Write-Host "=== 5. Verifying the client ==="
try {
    $VERIFY_RESPONSE = Invoke-RestMethod -Method Post -Uri "$BASE_URL/clients/$CLIENT_ID/verify" -ContentType "application/json"
    Write-Host "Client verification response:"
    $VERIFY_RESPONSE | ConvertTo-Json -Depth 10 | Write-Host
} catch {
    Write-Host "Error verifying client:" $_.Exception.Message
}

# === 6. Updating the client ===
Write-Host "=== 6. Updating the client ==="
$updateClientBody = @{
    firstName          = "Updated"
    lastName           = "Client"
    dateOfBirth        = "1990-01-01"
    gender             = "MALE"
    emailAddress       = $UNIQUE_EMAIL
    phoneNumber        = "9876543210"
    address            = "456 Updated St"
    city               = "Singapore"
    state              = "Singapore"
    country            = "Singapore"
    postalCode         = "654321"
    nric               = $UNIQUE_NRIC
    agentId            = "test-agent"
    verificationStatus = "VERIFIED"
}
$updateClientJson = $updateClientBody | ConvertTo-Json -Depth 10

try {
    $UPDATE_CLIENT_RESPONSE = Invoke-RestMethod -Method Put -Uri "$BASE_URL/clients/$CLIENT_ID" -Body $updateClientJson -ContentType "application/json"
    Write-Host "Client update response:"
    $UPDATE_CLIENT_RESPONSE | ConvertTo-Json -Depth 10 | Write-Host
} catch {
    Write-Host "Error updating client:" $_.Exception.Message
}

# === 7. Creating a test account for the client ===
Write-Host "=== 7. Creating a test account for the client ==="
$accountBody = @{
    clientId       = $CLIENT_ID
    accountType    = "SAVINGS"
    accountStatus  = "ACTIVE"
    openingDate    = (Get-Date -Format "yyyy-MM-dd")
    initialDeposit = 1000.00
    currency       = "SGD"
    branchId       = "BR001"
}
$accountJson = $accountBody | ConvertTo-Json -Depth 10

try {
    $ACCOUNT_RESPONSE = Invoke-RestMethod -Method Post -Uri "$BASE_URL/accounts" -Body $accountJson -ContentType "application/json"
    Write-Host "Account creation response:"
    $ACCOUNT_RESPONSE | ConvertTo-Json -Depth 10 | Write-Host
} catch {
    Write-Host "Error creating account:" $_.Exception.Message
    exit 1
}

$ACCOUNT_ID = $ACCOUNT_RESPONSE.accountId

if (-not $ACCOUNT_ID) {
    Write-Host "Failed to create test account"
    exit 1
}

Write-Host "Created test account with ID: $ACCOUNT_ID"

# === 8. Getting all accounts for the client ===
Write-Host "=== 8. Getting all accounts for the client ==="
try {
    $GET_CLIENT_ACCOUNTS_RESPONSE = Invoke-RestMethod -Method Get -Uri "$BASE_URL/clients/$CLIENT_ID/accounts"
    Write-Host "Get client accounts response:"
    $GET_CLIENT_ACCOUNTS_RESPONSE | ConvertTo-Json -Depth 10 | Write-Host
} catch {
    Write-Host "Error getting client accounts:" $_.Exception.Message
}

# === 9. Testing account endpoints ===
Write-Host "=== 9. Testing account endpoints ==="
# --- 9.1. Getting the account by ID ---
Write-Host "=== 9.1. Getting the account by ID ==="
try {
    $GET_ACCOUNT_RESPONSE = Invoke-RestMethod -Method Get -Uri "$BASE_URL/accounts/$ACCOUNT_ID"
    Write-Host "Get account response:"
    $GET_ACCOUNT_RESPONSE | ConvertTo-Json -Depth 10 | Write-Host
} catch {
    Write-Host "Error getting account by ID:" $_.Exception.Message
}

# --- 9.2. Getting accounts by client ID ---
Write-Host "=== 9.2. Getting accounts by client ID ==="
try {
    $GET_ACCOUNTS_BY_CLIENT_RESPONSE = Invoke-RestMethod -Method Get -Uri "$BASE_URL/accounts/client/$CLIENT_ID"
    Write-Host "Get accounts by client response:"
    $GET_ACCOUNTS_BY_CLIENT_RESPONSE | ConvertTo-Json -Depth 10 | Write-Host
} catch {
    Write-Host "Error getting accounts by client ID:" $_.Exception.Message
}

# === 10. Deleting the account ===
Write-Host "=== 10. Deleting the account ==="
try {
    $DELETE_ACCOUNT_RESPONSE = Invoke-RestMethod -Method Delete -Uri "$BASE_URL/accounts/$ACCOUNT_ID"
    Write-Host "Delete account response:"
    if ($DELETE_ACCOUNT_RESPONSE) {
        Write-Host $DELETE_ACCOUNT_RESPONSE
    } else {
        Write-Host "No response (success)"
    }
} catch {
    Write-Host "Error deleting account:" $_.Exception.Message
}

# === 11. Testing log endpoints ===
Write-Host "=== 11. Testing log endpoints ==="
# --- 11.1. Getting all logs ---
Write-Host "=== 11.1. Getting all logs ==="
try {
    $GET_ALL_LOGS_RESPONSE = Invoke-RestMethod -Method Get -Uri "$BASE_URL/logs"
    $allLogsJson = $GET_ALL_LOGS_RESPONSE | ConvertTo-Json -Depth 10
    Write-Host "Get all logs response (showing first 200 characters):"
    if ($allLogsJson.Length -gt 200) {
        Write-Host $allLogsJson.Substring(0,200) "..."
    } else {
        Write-Host $allLogsJson
    }
} catch {
    Write-Host "Error getting all logs:" $_.Exception.Message
}

# --- 11.2. Getting logs by client ID ---
Write-Host "=== 11.2. Getting logs by client ID ==="
try {
    $GET_LOGS_BY_CLIENT_RESPONSE = Invoke-RestMethod -Method Get -Uri "$BASE_URL/logs/client/$CLIENT_ID"
    Write-Host "Get logs by client response:"
    $GET_LOGS_BY_CLIENT_RESPONSE | ConvertTo-Json -Depth 10 | Write-Host
} catch {
    Write-Host "Error getting logs by client ID:" $_.Exception.Message
}

# --- 11.3. Getting logs by CRUD type ---
Write-Host "=== 11.3. Getting logs by CRUD type ==="
try {
    $GET_LOGS_BY_TYPE_RESPONSE = Invoke-RestMethod -Method Get -Uri "$BASE_URL/logs/type/CREATE"
    $logsByTypeJson = $GET_LOGS_BY_TYPE_RESPONSE | ConvertTo-Json -Depth 10
    Write-Host "Get logs by CRUD type response (showing first 200 characters):"
    if ($logsByTypeJson.Length -gt 200) {
        Write-Host $logsByTypeJson.Substring(0,200) "..."
    } else {
        Write-Host $logsByTypeJson
    }
} catch {
    Write-Host "Error getting logs by CRUD type:" $_.Exception.Message
}

# --- 11.4. Getting logs by agent ID ---
Write-Host "=== 11.4. Getting logs by agent ID ==="
try {
    $GET_LOGS_BY_AGENT_RESPONSE = Invoke-RestMethod -Method Get -Uri "$BASE_URL/logs/agent/test-agent"
    $logsByAgentJson = $GET_LOGS_BY_AGENT_RESPONSE | ConvertTo-Json -Depth 10
    Write-Host "Get logs by agent ID response (showing first 200 characters):"
    if ($logsByAgentJson.Length -gt 200) {
        Write-Host $logsByAgentJson.Substring(0,200) "..."
    } else {
        Write-Host $logsByAgentJson
    }
} catch {
    Write-Host "Error getting logs by agent ID:" $_.Exception.Message
}

# === 12. Deleting the client ===
Write-Host "=== 12. Deleting the client ==="
try {
    $DELETE_CLIENT_RESPONSE = Invoke-RestMethod -Method Delete -Uri "$BASE_URL/clients/$CLIENT_ID"
    Write-Host "Delete client response:"
    if ($DELETE_CLIENT_RESPONSE) {
        Write-Host $DELETE_CLIENT_RESPONSE
    } else {
        Write-Host "No response (success)"
    }
} catch {
    Write-Host "Error deleting client:" $_.Exception.Message
}

Write-Host "All tests completed successfully."
