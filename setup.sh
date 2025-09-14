#!/bin/bash

git pull

git submodule update --init --recursive

git submodule foreach '
  if [[ $(basename $(pwd)) == *service || $(basename $(pwd)) == *gateway || $(basename $(pwd)) == *k8s ]]; then
    echo "Updating submodule: $(basename $(pwd))"
    git pull origin main
  else
    echo "Skipping submodule: $(basename $(pwd)) - does not end with \"service\", \"gateway\" or \"k8s"\"
  fi
'

check_command() {
    if ! command -v $1 &> /dev/null; then
        echo "❌ $1 is not installed"
        return 1
    else
        echo "✅ $1 is installed ($(command -v $1))"
        return 0
    fi
}

echo "Checking required tools..."
echo "-------------------------"

check_command minikube
MINIKUBE_STATUS=$?

# Check kubectl
check_command kubectl
KUBECTL_STATUS=$?

# Check tilt
check_command tilt
TILT_STATUS=$?

echo "-------------------------"

# If any tool is missing, exit with error
if [ $MINIKUBE_STATUS -ne 0 ] || [ $KUBECTL_STATUS -ne 0 ] || [ $TILT_STATUS -ne 0 ]; then
    echo "❌ Some required tools are missing. Please install them before continuing."

    # Installation instructions
    echo -e "\nInstallation guides:"
    echo "- minikube: https://minikube.sigs.k8s.io/docs/start/"
    echo "- kubectl: https://kubernetes.io/docs/tasks/tools/"
    echo "- tilt: https://docs.tilt.dev/install.html"

    exit 1
else
    echo "✅ All required tools are installed!"
fi


echo "Checking that current config is not production..."
if [ "$(kubectl config current-context)" != "minikube" ] || [ "$(minikube status --format '{{.Host}}')" != "Running" ]; then
    echo "Starting minikube..."
    minikube config set memory 4096
    minikube config set cpus 4
    minikube delete
    minikube start --driver=docker --container-runtime=containerd
    minikube addons disable dashboard
    minikube addons disable metrics-server
    minikube addons enable registry

    echo "Setting up registry port-forwarding..."
    docker run --rm -d \
        --network=host \
        alpine/socat \
        tcp-listen:5000,reuseaddr,fork \
        tcp:$(minikube ip):5000
fi

echo "Type tilt up to initialise the backend now!"
