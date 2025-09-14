# Local Development Setup Guide

This guide will help you set up your local development environment for the project.

## Prerequisites

Before you begin, ensure you have the following tools installed on your system:

- [minikube](https://minikube.sigs.k8s.io/docs/start/) - Local Kubernetes cluster
- [kubectl](https://kubernetes.io/docs/tasks/tools/) - Kubernetes command-line tool
- [tilt](https://docs.tilt.dev/install.html) - Modern development environment
- [Docker](https://docs.docker.com/get-docker/) - Containerization platform
- [Gradle](https://gradle.org/install/) - Build automation tool
- [Java 17](https://adoptium.net/) - Java Development Kit

## Setup Steps

1. **Install Dependencies**
   ```bash
   # For macOS (using Homebrew)
   brew install gradle
   brew install --cask temurin17 # For Java 17
   brew install --cask docker
   brew install kubectl
   brew install minikube

   # For Ubuntu/Debian
   sudo apt update
   sudo apt install gradle
   sudo apt install openjdk-17-jdk
   sudo apt install docker.io
   ```

2. **Clone the Repository**
   ```bash
   git clone [repository-url]
   cd [repository-name]
   ```

3. **Run the Setup Script**
   ```bash
   ./setup.sh
   ```
   This script will:
  - Pull the latest changes from the main repository
  - Update and initialize all submodules
  - Check for required tools (minikube, kubectl, tilt)
  - Configure and start minikube with appropriate resources (4GB RAM, 4 CPUs)
  - Enable necessary minikube addons

4. **Start the Development Environment**
   After the setup script completes successfully, start the development environment by running:
   ```bash
   tilt up
   ```

## Environment Variables

Make sure you have the following environment variables set:
- `JAVA_HOME` - pointing to Java 17 installation
- `GRADLE_HOME` - pointing to Gradle installation
- Docker should be running before starting minikube

## Directory Structure
```
project/
├── setup.sh
├── Tiltfile
├── service1/
│   ├── build.gradle
│   └── ...
├── service2/
│   ├── build.gradle
│   └── ...
└── ...
```

## What the Setup Does

- Ensures all required tools are installed
- Configures minikube with appropriate resources
- Sets up the Kubernetes development environment
- Prepares all microservices for local development

## Troubleshooting

If you encounter any issues during setup:

1. **Missing Tools**
   - The script will provide installation links if any required tools are missing
   - Follow the provided links to install the missing tools

2. **Minikube Issues**
   - Ensure you have sufficient system resources
   - Try running `minikube delete` and then rerun the setup script

3. **Submodule Issues**
   - Run `git submodule update --init --recursive` manually
   - Check if you have access to all required repositories

## System Requirements

- Minimum 8GB RAM recommended
- 4 CPU cores recommended
- Sufficient disk space for Docker images and Kubernetes cluster
