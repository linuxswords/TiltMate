#!/bin/bash
#
# Chess Clock - Development Environment Setup Script
#
# This script helps set up the development environment for Chess Clock
# on Linux systems (Ubuntu/Debian-based)
#
# Usage: ./setup-dev-env.sh
#

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

echo -e "${CYAN}=====================================${NC}"
echo -e "${CYAN}Chess Clock - Dev Environment Setup${NC}"
echo -e "${CYAN}=====================================${NC}"
echo ""

# Function to print status messages
print_status() {
    echo -e "${CYAN}==>${NC} $1"
}

print_success() {
    echo -e "${GREEN}✓${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}⚠${NC} $1"
}

print_error() {
    echo -e "${RED}✗${NC} $1"
}

# Check if running on Linux
if [[ "$OSTYPE" != "linux-gnu"* ]]; then
    print_warning "This script is designed for Linux (Ubuntu/Debian)"
    print_warning "You may need to adapt commands for your OS"
    echo ""
fi

# Check Java installation
print_status "Checking Java installation..."
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d. -f1)
    if [ "$JAVA_VERSION" -ge 21 ]; then
        print_success "Java $JAVA_VERSION is already installed"
    else
        print_warning "Java version is too old (requires 21+)"
        echo "  Current version: $JAVA_VERSION"
    fi
else
    print_warning "Java is not installed"
    echo ""
    print_status "Installing OpenJDK 21..."

    if command -v apt-get &> /dev/null; then
        sudo apt-get update
        sudo apt-get install -y openjdk-21-jdk
        print_success "OpenJDK 21 installed"
    else
        print_error "Cannot install Java automatically (apt-get not found)"
        echo "  Please install JDK 21+ manually"
        exit 1
    fi
fi

# Set JAVA_HOME
print_status "Setting up JAVA_HOME..."
if [ -z "$JAVA_HOME" ]; then
    # Try to find Java installation
    if [ -d "/usr/lib/jvm/java-17-openjdk-amd64" ]; then
        JAVA_HOME_PATH="/usr/lib/jvm/java-17-openjdk-amd64"
    elif [ -d "/usr/lib/jvm/java-17-openjdk" ]; then
        JAVA_HOME_PATH="/usr/lib/jvm/java-17-openjdk"
    else
        JAVA_HOME_PATH=$(dirname $(dirname $(readlink -f $(which java))))
    fi

    export JAVA_HOME="$JAVA_HOME_PATH"
    print_success "JAVA_HOME set to: $JAVA_HOME"

    # Add to .bashrc if not already present
    if ! grep -q "JAVA_HOME" ~/.bashrc; then
        print_status "Adding JAVA_HOME to ~/.bashrc..."
        echo "" >> ~/.bashrc
        echo "# Java Home for Android development" >> ~/.bashrc
        echo "export JAVA_HOME=$JAVA_HOME_PATH" >> ~/.bashrc
        echo "export PATH=\$PATH:\$JAVA_HOME/bin" >> ~/.bashrc
        print_success "JAVA_HOME added to ~/.bashrc"
    fi
else
    print_success "JAVA_HOME already set: $JAVA_HOME"
fi

# Check Android SDK
print_status "Checking Android SDK..."
if [ -n "$ANDROID_HOME" ]; then
    print_success "ANDROID_HOME is set: $ANDROID_HOME"
elif [ -n "$ANDROID_SDK_ROOT" ]; then
    print_success "ANDROID_SDK_ROOT is set: $ANDROID_SDK_ROOT"
    export ANDROID_HOME="$ANDROID_SDK_ROOT"
else
    print_warning "Android SDK not found"
    echo ""
    echo "  Android SDK is required for:"
    echo "    - Building APK files"
    echo "    - Running instrumented tests"
    echo ""
    echo "  Installation options:"
    echo "    1. Install Android Studio (recommended)"
    echo "       https://developer.android.com/studio"
    echo ""
    echo "    2. Install command-line tools only:"
    echo "       https://developer.android.com/studio#command-tools"
    echo ""
    echo "  After installation, set ANDROID_HOME:"
    echo "    export ANDROID_HOME=\$HOME/Android/Sdk"
    echo "    echo 'export ANDROID_HOME=\$HOME/Android/Sdk' >> ~/.bashrc"
    echo ""
fi

# Check Gradle wrapper
print_status "Checking Gradle wrapper..."
if [ -f "./gradlew" ]; then
    print_success "Gradle wrapper found"
    chmod +x ./gradlew
else
    print_error "Gradle wrapper not found"
    echo "  This should not happen. Re-clone the repository."
    exit 1
fi

# Verify Make is installed
print_status "Checking Make installation..."
if command -v make &> /dev/null; then
    print_success "Make is installed"
else
    print_warning "Make is not installed"
    echo ""
    print_status "Installing Make..."
    if command -v apt-get &> /dev/null; then
        sudo apt-get install -y make
        print_success "Make installed"
    else
        print_error "Cannot install Make automatically"
        echo "  Please install make manually"
    fi
fi

# Summary
echo ""
echo -e "${CYAN}=====================================${NC}"
echo -e "${CYAN}Setup Summary${NC}"
echo -e "${CYAN}=====================================${NC}"
echo ""

# Test environment
print_status "Testing environment setup..."
echo ""

if [ -n "$JAVA_HOME" ] && [ -f "./gradlew" ]; then
    print_success "Basic environment is ready!"
    echo ""
    echo "  You can now run:"
    echo "    make check-env    # Verify setup"
    echo "    make test         # Run tests"
    echo "    make build        # Build APK"
    echo "    make help         # See all commands"
else
    print_warning "Environment setup incomplete"
    echo ""
    echo "  Please ensure:"
    echo "    - JAVA_HOME is set"
    echo "    - Android SDK is installed (for building APK)"
fi

echo ""
if [ -z "$ANDROID_HOME" ]; then
    print_warning "Note: ANDROID_HOME not set"
    echo "  - Unit tests will work"
    echo "  - APK building requires Android SDK"
    echo ""
fi

echo -e "${GREEN}Setup script completed!${NC}"
echo ""
echo "Restart your terminal or run: source ~/.bashrc"
