@echo off
setlocal enabledelayedexpansion

echo Pulling latest changes...
git pull
git submodule update --init --recursive

echo Updating specific submodules...
for /f "tokens=1,2" %%a in ('git submodule') do (
    pushd %%b
    for /f "delims=" %%i in ('cd') do set "current_dir=%%~nxi"
    if "!current_dir:~-7!"=="service" (
        echo Updating submodule: !current_dir!
        git pull origin main
    ) else if "!current_dir:~-7!"=="gateway" (
        echo Updating submodule: !current_dir!
        git pull origin main
    ) else if "!current_dir:~-3!"=="k8s" (
        echo Updating submodule: !current_dir!
        git pull origin main
    ) else (
        echo Skipping submodule: !current_dir! - does not end with "service", "gateway" or "k8s"
    )
    popd
)

echo Checking required tools...
echo -------------------------

:: Check minikube
where minikube >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo ❌ minikube is not installed
    set MINIKUBE_STATUS=1
) else (
    for /f "delims=" %%i in ('where minikube') do (
        echo ✅ minikube is installed (%%i)
    )
    set MINIKUBE_STATUS=0
)

:: Check kubectl
where kubectl >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo ❌ kubectl is not installed
    set KUBECTL_STATUS=1
) else (
    for /f "delims=" %%i in ('where kubectl') do (
        echo ✅ kubectl is installed (%%i)
    )
    set KUBECTL_STATUS=0
)

:: Check tilt
where tilt >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo ❌ tilt is not installed
    set TILT_STATUS=1
) else (
    for /f "delims=" %%i in ('where tilt') do (
        echo ✅ tilt is installed (%%i)
    )
    set TILT_STATUS=0
)

echo -------------------------

:: If any tool is missing, exit with error
if %MINIKUBE_STATUS% neq 0 (
    goto :missing_tools
) else if %KUBECTL_STATUS% neq 0 (
    goto :missing_tools
) else if %TILT_STATUS% neq 0 (
    goto :missing_tools
) else (
    echo ✅ All required tools are installed!
    goto :check_context
)

:missing_tools
echo ❌ Some required tools are missing. Please install them before continuing.
echo.
echo Installation guides:
echo - minikube: https://minikube.sigs.k8s.io/docs/start/
echo - kubectl: https://kubernetes.io/docs/tasks/tools/
echo - tilt: https://docs.tilt.dev/install.html
exit /b 1

:check_context
echo Checking that current config is not production...

:: Get current context
for /f "delims=" %%i in ('kubectl config current-context 2^>nul') do (
    set "context=%%i"
)

:: Check if context is minikube
if not "!context!"=="minikube" (
    goto :start_minikube
) else (
    :: Check if minikube is running
    for /f "delims=" %%i in ('minikube status --format "{{.Host}}" 2^>nul') do (
        set "status=%%i"
    )
    if not "!status!"=="Running" (
        goto :start_minikube
    ) else (
        echo Minikube is already running with the correct configuration.
    )
)
goto :end

:start_minikube
echo Starting minikube...
minikube config set memory 4096
minikube config set cpus 4
minikube delete
minikube start --driver=docker --container-runtime=containerd
minikube addons disable dashboard
minikube addons disable metrics-server
minikube addons enable registry

echo Setting up registry port-forwarding...
for /f "delims=" %%i in ('minikube ip') do set MINIKUBE_IP=%%i
start /b docker run --rm ^
    --network=host ^
    alpine/socat ^
    tcp-listen:5000,reuseaddr,fork ^
    tcp:%MINIKUBE_IP%:5000

:end
echo Type 'tilt up' to initialize the backend now!
