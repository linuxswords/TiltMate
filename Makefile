# TiltMate - Makefile
#
# Prerequisites:
#   - Java Development Kit (JDK) 17 or higher
#   - Android SDK (can be installed via Android Studio or sdkmanager)
#   - ANDROID_HOME environment variable set to Android SDK location
#   - JAVA_HOME environment variable set to JDK location
#
# Quick Start:
#   make help          - Show all available commands
#   make test          - Run all unit tests
#   make build         - Build debug APK
#   make build-release - Build release APK
#   make clean         - Clean build artifacts

.PHONY: help test test-unit test-instrumented build build-debug build-release \
        install clean check-env lint assemble run-tests-verbose

# Default target
.DEFAULT_GOAL := help

# Color output
CYAN := \033[0;36m
GREEN := \033[0;32m
YELLOW := \033[0;33m
RED := \033[0;31m
NC := \033[0m # No Color

##@ General

help: ## Display this help message
	@echo "$(CYAN)TiltMate - Available Make Targets$(NC)"
	@echo ""
	@awk 'BEGIN {FS = ":.*##"; printf "Usage:\n  make $(CYAN)<target>$(NC)\n"} \
		/^[a-zA-Z_-]+:.*?##/ { printf "  $(CYAN)%-20s$(NC) %s\n", $$1, $$2 } \
		/^##@/ { printf "\n$(YELLOW)%s$(NC)\n", substr($$0, 5) } ' $(MAKEFILE_LIST)
	@echo ""
	@echo "$(YELLOW)Prerequisites:$(NC)"
	@echo "  - JDK 17+ (set JAVA_HOME)"
	@echo "  - Android SDK (set ANDROID_HOME)"
	@echo "  Run 'make check-env' to verify setup"

check-env: ## Check if required environment variables are set
	@echo "$(CYAN)Checking environment setup...$(NC)"
	@if [ -z "$$JAVA_HOME" ]; then \
		echo "$(RED)✗ JAVA_HOME is not set$(NC)"; \
		echo "  Install JDK 17+ and set JAVA_HOME"; \
		exit 1; \
	else \
		echo "$(GREEN)✓ JAVA_HOME is set: $$JAVA_HOME$(NC)"; \
	fi
	@if [ -z "$$ANDROID_HOME" ] && [ -z "$$ANDROID_SDK_ROOT" ]; then \
		echo "$(YELLOW)⚠ ANDROID_HOME is not set (optional for unit tests)$(NC)"; \
	else \
		echo "$(GREEN)✓ ANDROID_HOME is set: $$ANDROID_HOME$(NC)"; \
	fi
	@if [ ! -f "./gradlew" ]; then \
		echo "$(RED)✗ Gradle wrapper not found$(NC)"; \
		exit 1; \
	else \
		echo "$(GREEN)✓ Gradle wrapper found$(NC)"; \
	fi
	@echo "$(GREEN)Environment check passed!$(NC)"

##@ Testing

test: test-unit ## Run all unit tests (default test target)
	@echo "$(GREEN)All unit tests completed!$(NC)"

test-unit: check-env ## Run unit tests only (no Android device required)
	@echo "$(CYAN)Running unit tests...$(NC)"
	./gradlew test --console=plain
	@echo "$(GREEN)Unit tests completed successfully!$(NC)"

test-verbose: check-env ## Run unit tests with verbose output
	@echo "$(CYAN)Running unit tests with verbose output...$(NC)"
	./gradlew test --console=plain --info

test-instrumented: check-env ## Run instrumented tests (requires Android device/emulator)
	@echo "$(CYAN)Running instrumented tests...$(NC)"
	@echo "$(YELLOW)Note: Requires a connected Android device or running emulator$(NC)"
	./gradlew connectedAndroidTest --console=plain

test-all: check-env ## Run both unit and instrumented tests
	@echo "$(CYAN)Running all tests (unit + instrumented)...$(NC)"
	./gradlew test connectedAndroidTest --console=plain
	@echo "$(GREEN)All tests completed!$(NC)"

test-report: ## Generate and display test report location
	@echo "$(CYAN)Test reports generated at:$(NC)"
	@echo "  Unit tests: file://$(PWD)/app/build/reports/tests/testDebugUnitTest/index.html"
	@echo "  Instrumented: file://$(PWD)/app/build/reports/androidTests/connected/index.html"

##@ Building

build: build-debug ## Build debug APK (default build target)

build-debug: check-env ## Build debug APK
	@echo "$(CYAN)Building debug APK...$(NC)"
	./gradlew assembleDebug --console=plain
	@echo "$(GREEN)Debug APK built successfully!$(NC)"
	@echo "$(CYAN)Location:$(NC) app/build/outputs/apk/debug/linuxswords-TiltMate-debug.apk"

build-release: check-env ## Build release APK
	@echo "$(CYAN)Building release APK...$(NC)"
	./gradlew assembleRelease --console=plain
	@echo "$(GREEN)Release APK built successfully!$(NC)"
	@echo "$(CYAN)Location:$(NC) app/build/outputs/apk/release/linuxswords-TiltMate-release.apk"

assemble: check-env ## Assemble all variants
	@echo "$(CYAN)Assembling all APK variants...$(NC)"
	./gradlew assemble --console=plain

install: build-debug ## Build and install debug APK to connected device
	@echo "$(CYAN)Installing debug APK to device...$(NC)"
	./gradlew installDebug --console=plain
	@echo "$(GREEN)App installed successfully!$(NC)"

##@ Code Quality

lint: check-env ## Run Android lint checks
	@echo "$(CYAN)Running lint checks...$(NC)"
	./gradlew lint --console=plain
	@echo "$(GREEN)Lint checks completed!$(NC)"
	@echo "$(CYAN)Report:$(NC) app/build/reports/lint-results.html"

##@ Maintenance

clean: ## Clean build artifacts and cache
	@echo "$(CYAN)Cleaning build artifacts...$(NC)"
	./gradlew clean --console=plain
	@echo "$(GREEN)Clean completed!$(NC)"

clean-all: clean ## Deep clean including Gradle cache
	@echo "$(CYAN)Performing deep clean...$(NC)"
	rm -rf .gradle
	rm -rf app/build
	rm -rf build
	@echo "$(GREEN)Deep clean completed!$(NC)"

gradle-refresh: ## Refresh Gradle dependencies
	@echo "$(CYAN)Refreshing Gradle dependencies...$(NC)"
	./gradlew --refresh-dependencies --console=plain

##@ Development Workflow

dev: clean test build ## Complete dev cycle: clean, test, build
	@echo "$(GREEN)Development cycle completed!$(NC)"

ci: clean test-unit lint build-debug ## CI pipeline: clean, test, lint, build
	@echo "$(GREEN)CI pipeline completed successfully!$(NC)"

quick: ## Quick build without tests (for rapid iteration)
	@echo "$(CYAN)Quick build (skipping tests)...$(NC)"
	./gradlew assembleDebug --console=plain
	@echo "$(GREEN)Quick build completed!$(NC)"

##@ Release

release-check: ## Check if ready for release
	@echo "$(CYAN)Pre-release checklist:$(NC)"
	@echo ""
	@make test > /dev/null 2>&1 && echo "$(GREEN)✓$(NC) Tests pass" || echo "$(RED)✗$(NC) Tests fail"
	@make lint > /dev/null 2>&1 && echo "$(GREEN)✓$(NC) Lint passes" || echo "$(YELLOW)⚠$(NC) Lint has warnings"
	@make build-release > /dev/null 2>&1 && echo "$(GREEN)✓$(NC) Release builds" || echo "$(RED)✗$(NC) Release build fails"
	@echo ""
	@echo "$(CYAN)Current version:$(NC)"
	@grep "versionName" app/build.gradle | head -1 || echo "Not found"
	@echo ""
	@echo "$(CYAN)Recent tags:$(NC)"
	@git tag -l --sort=-v:refname | head -5 || echo "No tags yet"

release-tag: ## Create a release tag (usage: make release-tag VERSION=1.0.0)
	@if [ -z "$(VERSION)" ]; then \
		echo "$(RED)Error: VERSION not specified$(NC)"; \
		echo "Usage: make release-tag VERSION=1.0.0"; \
		exit 1; \
	fi
	@echo "$(CYAN)Creating release tag v$(VERSION)...$(NC)"
	@git tag -a v$(VERSION) -m "Release v$(VERSION)"
	@echo "$(GREEN)Tag v$(VERSION) created!$(NC)"
	@echo ""
	@echo "$(CYAN)Push tag to trigger release:$(NC)"
	@echo "  git push origin v$(VERSION)"

release-push: ## Push latest tag to trigger release
	@LATEST_TAG=$$(git describe --tags --abbrev=0 2>/dev/null); \
	if [ -z "$$LATEST_TAG" ]; then \
		echo "$(RED)No tags found$(NC)"; \
		exit 1; \
	fi; \
	echo "$(CYAN)Pushing tag $$LATEST_TAG to trigger release...$(NC)"; \
	git push origin $$LATEST_TAG; \
	echo "$(GREEN)Tag pushed! Release workflow starting...$(NC)"; \
	echo "View at: https://github.com/linuxswords/TiltMate/actions"

release-list: ## List all releases
	@echo "$(CYAN)Releases:$(NC)"
	@if command -v gh > /dev/null 2>&1; then \
		gh release list; \
	else \
		echo "$(YELLOW)GitHub CLI (gh) not installed$(NC)"; \
		echo "View at: https://github.com/linuxswords/TiltMate/releases"; \
	fi

release-view: ## View latest release
	@if command -v gh > /dev/null 2>&1; then \
		gh release view --web; \
	else \
		echo "$(CYAN)Latest release:$(NC) https://github.com/linuxswords/TiltMate/releases/latest"; \
	fi

##@ CI/CD

ci-status: ## Check GitHub Actions workflow status (requires gh CLI)
	@echo "$(CYAN)GitHub Actions Workflow Status:$(NC)"
	@if command -v gh > /dev/null 2>&1; then \
		gh run list --workflow=android-ci.yml --limit 5; \
	else \
		echo "$(YELLOW)GitHub CLI (gh) not installed$(NC)"; \
		echo "Install: https://cli.github.com/"; \
		echo "Or view status at: https://github.com/linuxswords/TiltMate/actions"; \
	fi

ci-view: ## Open GitHub Actions in browser
	@echo "$(CYAN)Opening GitHub Actions...$(NC)"
	@if command -v gh > /dev/null 2>&1; then \
		gh workflow view android-ci.yml --web; \
	else \
		echo "$(YELLOW)GitHub CLI (gh) not installed$(NC)"; \
		echo "View at: https://github.com/linuxswords/TiltMate/actions"; \
	fi

ci-logs: ## View latest CI run logs (requires gh CLI)
	@echo "$(CYAN)Fetching latest CI logs...$(NC)"
	@if command -v gh > /dev/null 2>&1; then \
		gh run view --log; \
	else \
		echo "$(YELLOW)GitHub CLI (gh) not installed$(NC)"; \
		echo "Install: https://cli.github.com/"; \
	fi

##@ Information

info: ## Display project information
	@echo "$(CYAN)Project Information:$(NC)"
	@echo "  Name: TiltMate"
	@echo "  Type: Android Application"
	@echo "  Language: Java"
	@echo "  Build System: Gradle $(shell ./gradlew --version | grep Gradle | cut -d' ' -f2)"
	@echo "  Min SDK: 21 (Android 5.0)"
	@echo "  Target SDK: 36 (Android 15)"
	@echo ""
	@echo "$(CYAN)Project Structure:$(NC)"
	@echo "  Source: app/src/main/java/org/linuxswords/games/chessclock/"
	@echo "  Tests: app/src/test/java/org/linuxswords/games/chessclock/"
	@echo "  Layouts: app/src/main/res/layout/"

dependencies: check-env ## Show project dependencies
	@echo "$(CYAN)Project dependencies:$(NC)"
	./gradlew dependencies --configuration debugCompileClasspath --console=plain | head -50

tasks: check-env ## List all available Gradle tasks
	@echo "$(CYAN)Available Gradle tasks:$(NC)"
	./gradlew tasks --console=plain
