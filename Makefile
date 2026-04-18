# TiltMate - Makefile
#
# Prerequisites:
#   - mise (https://mise.jdx.dev) - run `mise install` to set up Node.js
#   - Or Node.js 22+ installed directly
#
# Quick Start:
#   make help    - Show all available commands
#   make dev     - Start dev server
#   make build   - Production build
#   make check   - Type check

.PHONY: help dev build check clean ci release release-list

.DEFAULT_GOAL := help

CYAN := \033[0;36m
GREEN := \033[0;32m
YELLOW := \033[0;33m
RED := \033[0;31m
NC := \033[0m

##@ General

help: ## Display this help message
	@echo -e "$(CYAN)TiltMate - Available Make Targets$(NC)"
	@echo ""
	@awk 'BEGIN {FS = ":.*##"; printf "Usage:\n  make \033[0;36m<target>\033[0m\n"} \
		/^[a-zA-Z_-]+:.*?##/ { printf "  \033[0;36m%-20s\033[0m %s\n", $$1, $$2 } \
		/^##@/ { printf "\n\033[0;33m%s\033[0m\n", substr($$0, 5) } ' $(MAKEFILE_LIST)

check-env: ## Check if required tools are available
	@echo -e "$(CYAN)Checking environment...$(NC)"
	@command -v node > /dev/null 2>&1 && echo -e "$(GREEN)✓ Node.js: $$(node --version)$(NC)" || (echo -e "$(RED)✗ Node.js not found$(NC)" && exit 1)
	@[ -f package-lock.json ] && echo -e "$(GREEN)✓ package-lock.json found$(NC)" || echo -e "$(YELLOW)⚠ Run 'npm install' first$(NC)"

##@ Development

dev: ## Start dev server
	@echo -e "$(CYAN)Starting dev server...$(NC)"
	npx vite --host

preview: build ## Preview production build locally
	@echo -e "$(CYAN)Previewing production build...$(NC)"
	npx vite preview --host

##@ Building

build: ## Production build
	@echo -e "$(CYAN)Building for production...$(NC)"
	npx vite build
	@echo -e "$(GREEN)Build complete! Output in dist/$(NC)"

check: ## Type check
	@echo -e "$(CYAN)Running type check...$(NC)"
	npx tsc --noEmit
	@echo -e "$(GREEN)Type check passed!$(NC)"

##@ CI/CD

test: ## Run tests
	@echo -e "$(CYAN)Running tests...$(NC)"
	npx vitest run
	@echo -e "$(GREEN)Tests passed!$(NC)"

ci: check test build ## Full CI pipeline: type check + test + build
	@echo -e "$(GREEN)CI pipeline passed!$(NC)"

##@ Maintenance

clean: ## Clean build artifacts
	@echo -e "$(CYAN)Cleaning...$(NC)"
	rm -rf dist
	@echo -e "$(GREEN)Clean complete!$(NC)"

##@ Release

release: ## Create and push release tag (usage: make release VERSION=2.0.0)
	@if [ -z "$(VERSION)" ]; then \
		echo -e "$(RED)Error: VERSION not specified$(NC)"; \
		echo "Usage: make release VERSION=2.0.0"; \
		exit 1; \
	fi
	@echo -e "$(CYAN)Creating release tag v$(VERSION)...$(NC)"
	@git tag -a v$(VERSION) -m "Release v$(VERSION)"
	@git push origin v$(VERSION)
	@echo -e "$(GREEN)Tag v$(VERSION) pushed! Release workflow starting...$(NC)"
	@echo "View at: https://github.com/linuxswords/TiltMate/actions"

release-list: ## List all releases
	@if command -v gh > /dev/null 2>&1; then \
		gh release list; \
	else \
		echo "View at: https://github.com/linuxswords/TiltMate/releases"; \
	fi

##@ Information

info: ## Display project information
	@echo -e "$(CYAN)Project Information:$(NC)"
	@echo "  Name: TiltMate"
	@echo "  Type: Web Application (PWA)"
	@echo "  Language: TypeScript"
	@echo "  Build: Vite"
	@echo "  Source: src/"
	@echo ""
	@echo -e "$(CYAN)Recent tags:$(NC)"
	@git tag -l --sort=-v:refname | head -5 || echo "No tags yet"
