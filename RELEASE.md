# Release Guide

## Creating a Release

### Method 1: Tag-based Release (Recommended)

1. **Update version in `app/build.gradle`**
   ```gradle
   versionCode 2
   versionName "1.1.0"
   ```

2. **Commit and push changes**
   ```bash
   git add app/build.gradle
   git commit -m "chore: bump version to 1.1.0"
   git push origin main
   ```

3. **Create and push a tag**
   ```bash
   git tag -a v1.1.0 -m "Release v1.1.0"
   git push origin v1.1.0
   ```

4. **GitHub Actions automatically**:
   - Runs tests
   - Runs lint checks
   - Builds release APK
   - Creates GitHub release
   - Uploads APK to release page

### Method 2: Manual Trigger

1. Go to [Actions → Release](https://github.com/linuxswords/TiltMate/actions/workflows/release.yml)
2. Click "Run workflow"
3. Enter version number (e.g., `1.1.0`)
4. Click "Run workflow"

## Release Workflow

The release workflow (`release.yml`) performs:

```
Tests → Lint → Build Release APK → Create Release → Upload APK
```

## Release Page

The automated release includes:

- **APK Download** - `linuxswords-TiltMate-release.apk`
- **Version Info** - Version number and build details
- **Changelog** - Commits since last release
- **Installation Instructions** - How to install on Android
- **Feature List** - App capabilities
- **Requirements** - Min/target Android versions

## Version Naming

Follow [Semantic Versioning](https://semver.org/):

- **Major** (`v2.0.0`) - Breaking changes
- **Minor** (`v1.1.0`) - New features, backwards compatible
- **Patch** (`v1.0.1`) - Bug fixes

**Examples:**
- `v1.0.0` - Initial release
- `v1.1.0` - Added sound effects
- `v1.1.1` - Fixed timer bug
- `v2.0.0` - Redesigned UI (breaking changes)

## Checklist Before Release

- [ ] All tests pass (`make test`)
- [ ] Lint checks pass (`make lint`)
- [ ] Version bumped in `app/build.gradle`
- [ ] CHANGELOG updated (optional)
- [ ] Tested on device
- [ ] Screenshots updated (if UI changed)

## View Releases

- **All releases**: https://github.com/linuxswords/TiltMate/releases
- **Latest release**: https://github.com/linuxswords/TiltMate/releases/latest

## Example Release Commands

```bash
# Release v1.0.0
git tag -a v1.0.0 -m "Initial release"
git push origin v1.0.0

# Release v1.1.0 with changes
git add .
git commit -m "feat: add sound effects"
git tag -a v1.1.0 -m "Release v1.1.0 - Sound effects"
git push origin main
git push origin v1.1.0

# Delete a tag (if needed)
git tag -d v1.0.0
git push origin :refs/tags/v1.0.0
```

## Badges

Add these badges to show release info:

```markdown
[![Release](https://img.shields.io/github/v/release/linuxswords/TiltMate)](https://github.com/linuxswords/TiltMate/releases/latest)
[![Downloads](https://img.shields.io/github/downloads/linuxswords/TiltMate/total)](https://github.com/linuxswords/TiltMate/releases)
```
