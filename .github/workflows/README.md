# GitHub Actions Workflows

[![Android CI](https://github.com/linuxswords/TiltMate/actions/workflows/android-ci.yml/badge.svg)](https://github.com/linuxswords/TiltMate/actions/workflows/android-ci.yml)
[![Release](https://img.shields.io/github/v/release/linuxswords/TiltMate)](https://github.com/linuxswords/TiltMate/releases/latest)

## Workflows

### 1. CI Workflow (`android-ci.yml`)

**Triggers:**
- Push to `main` or `develop`
- Pull requests to `main` or `develop`
- Manual dispatch

**Jobs:**
```
Test (unit tests) ─┐
                   ├─> Build (debug APK)
Lint (checks)     ─┘
```

**Artifacts** (7-day retention):
- `test-reports` - HTML test reports
- `lint-reports` - Lint analysis
- `TiltMate-debug` - Debug APK

## Usage

### View Status
```bash
# Using GitHub CLI
make ci-status      # List recent runs
make ci-view        # Open in browser
make ci-logs        # View latest logs

# Or visit
# https://github.com/linuxswords/TiltMate/actions
```

### Download Artifacts
```bash
# Using GitHub CLI
gh run download <run-id>

# Or download from GitHub UI
# Actions → Run → Artifacts section
```

### Run Locally
Replicate CI checks on your machine:
```bash
make ci             # Full pipeline
make test           # Tests only
make lint           # Lint only
make build          # Build only
```

## Troubleshooting

**Tests fail:**
- Download test reports artifact
- Run `make test` locally
- Check test logs in Actions tab

**Lint fails:**
- Download lint reports artifact
- Run `make lint` locally
- Fix issues and push again

**Build fails:**
- Ensure tests and lint pass first
- Run `make build` locally

## Badge

```markdown
[![Android CI](https://github.com/linuxswords/TiltMate/actions/workflows/android-ci.yml/badge.svg)](https://github.com/linuxswords/TiltMate/actions/workflows/android-ci.yml)
```

- 🟢 Green = Passing
- 🔴 Red = Failing
- 🟡 Yellow = Running

---

### 2. Release Workflow (`release.yml`)

**Triggers:**
- Push tags matching `v*.*.*` (e.g., `v1.0.0`)
- Manual dispatch with version input

**Jobs:**
```
Test → Lint → Build Release APK → Create GitHub Release → Upload APK
```

**Creates:**
- GitHub Release page with changelog
- APK download (`TiltMate-release.apk`)
- Version tags and release notes

**Usage:**

```bash
# Tag-based release (automatic)
git tag -a v1.0.0 -m "Release v1.0.0"
git push origin v1.0.0

# Manual trigger (via GitHub UI)
# Actions → Release → Run workflow → Enter version
```

See [RELEASE.md](../../RELEASE.md) for detailed release instructions.
