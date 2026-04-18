# GitHub Actions Workflows

[![CI](https://github.com/linuxswords/TiltMate/actions/workflows/ci.yml/badge.svg)](https://github.com/linuxswords/TiltMate/actions/workflows/ci.yml)
[![Release](https://img.shields.io/github/v/release/linuxswords/TiltMate)](https://github.com/linuxswords/TiltMate/releases/latest)

## Workflows

### 1. CI Workflow (`ci.yml`)

**Triggers:**
- Push to `main` or `develop`
- Pull requests to `main` or `develop`
- Manual dispatch

**Steps:** Install deps, type check, build

**Artifacts** (7-day retention):
- `dist` - Production build

### 2. Release Workflow (`release.yml`)

**Triggers:**
- Push tags matching `v*.*.*` (e.g., `v2.0.0`)
- Manual dispatch with version input

**Steps:** Build, create GitHub Release, deploy to GitHub Pages

**Usage:**

```bash
# Tag-based release (automatic)
git tag -a v2.0.0 -m "Release v2.0.0"
git push origin v2.0.0

# Or run locally
make release VERSION=2.0.0
```
