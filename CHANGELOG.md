# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.7.0] - 2025-12-26

### Added
- **Custom Time Controls** - Major new feature allowing user-defined time settings
  - Create custom time controls with any combination of minutes (1-180) and increment (0-60 seconds)
  - Custom button with edit icon replaces 15+5 preset in settings grid
  - Intuitive dialog with side-by-side minute and increment controls
  - Clear labels explaining "Minutes (initial time)" and "Increment (per move, sec)"
  - Hold-to-repeat functionality on +/- buttons for faster value adjustment
  - Custom times persist across app restarts
  - Dynamic button label shows current custom time (e.g., "20+10") when active
  - Dialog pre-populates with existing custom time for easy editing
  - Optimized landscape-friendly layout

### Changed
- **TimeSettings Architecture** - Refactored from enum to flexible class-based system
  - Converted to final class with static preset constants maintaining backward compatibility
  - Added factory method `createCustom()` for creating custom time instances
  - Implemented content-based equality for custom time comparison
  - Enhanced persistence layer to support custom time storage in SharedPreferences
- **Settings UI** - Replaced 15+5 preset button with "Custom" button featuring edit icon
- **Time Display** - Time setting label now updates immediately when returning from settings

### Fixed
- **Clock Update on Settings Return** - Clocks now correctly update to new time when returning from settings screen
- **Restart Behavior** - Double-tap restart now uses current time setting instead of cached value
- **Migration** - Old 15+5 preset automatically migrates to custom time (15+5) on first launch

## [1.6.1] - 2025-12-25

### Fixed
- **Increment Timing** - Fixed bug where time increment was added at wrong moment
  - Increment now correctly added when clock is paused/switched (when move is made)
  - Previously increment was incorrectly added when time resumed
  - Ensures proper chess clock behavior per standard chess rules

## [1.6.0] - 2025-12-24

### Added
- Funding support with Buy Me a Coffee/Tea links
- Custom gear icon (ic_settings_gear.xml) for Advanced Settings button

### Changed
- **Settings Screen UI Improvements**
  - Swapped positions and actions of quit/return buttons
    - Exit button (revert icon) now returns to game
    - Cancel button (power icon) now quits app
  - Improved button layout and alignment
    - Buttons centered horizontally between time settings and right edge
    - Gear icon aligned vertically with gap between time setting rows
    - Reduced spacing between buttons (12dp margins)

### Fixed
- **Clock State Preservation** - Fixed bug where clocks would reset when navigating to/from settings
  - Added proper Activity lifecycle state management (onSaveInstanceState, onPause, onResume)
  - Clock times now preserved across configuration changes and navigation
  - Move count preserved when returning from settings
  - Running clock state tracked and restored correctly
  - Added getRemainingTime() and setRemainingTime() methods to PlayerClock and PausableCountDownTimer
- **Navigation Pattern** - Changed SettingsActivity to use finish() instead of creating new MainActivity instances

## [1.5.0] - 2025-12-23

### Added
- **Show Move Counter** - New advanced setting to display the number of moves made during a game
  - Toggle switch in Advanced Settings screen
  - Move counter display on main game screen (18sp, white text)
  - Counter increments on each tilt/move
  - Counter resets when game restarts
  - Setting defaults to disabled for existing users

### Changed
- Settings UI harmonization with consistent button styling and layout across settings screens

## [1.4.0] - 2025-12-22

### Added
- **Settings Screen** - New dedicated screen for time control selection
  - Grid layout with all time presets (3+0 through 15+5)
  - Quick access to advanced settings
  - Visual feedback for selected time controls

## [1.3.0] - 2025-12-22

### Added
- **Settings Manager** - Persistent storage for time control preferences
  - Settings persist across app restarts
  - Automatic restoration of last used time control

### Changed
- Release workflow improvements for better APK packaging
- Documentation updates and cleanup

## [1.2.0] - 2025-12-22

### Changed
- **App Rebranding** - Changed name from "Chess Clock" to "TiltMate"
- Improved build tooling with better Make output formatting
- Added .tools-versions file for version management

## [1.1.0] - 2025-12-22

### Added
- AAB (Android App Bundle) build support for Play Store
- Custom app icon and branding assets

### Changed
- **Java Version** - Upgraded project from Java 17 to Java 21
  - Updated build.gradle compilation targets
  - Updated all documentation (README.md, TESTING.md, Makefile)
  - Updated setup-dev-env.sh installation script
- Migrated test framework from JUnit 4 to JUnit 5
- Complete app rename to TiltMate
- CI/CD improvements with debug keystore generation
- Better Gradle configuration

## [1.0.0] - 2025-11-09 - Initial Release

### Added
- Chess clock functionality with tilt-based control
- Multiple time control presets (3+0, 3+5, 5+0, 5+5, 10+0, 10+5, 15+0, 15+5)
- Advanced settings screen
- Tilt sensitivity adjustment (low, medium, high)
- Ticking sound toggle
- Pause/resume functionality
- Settings persistence using SharedPreferences
- Material Design UI with ConstraintLayout
- Keep screen on during gameplay

[Unreleased]: https://github.com/linuxswords/TiltMate/compare/v1.7.0...HEAD
[1.7.0]: https://github.com/linuxswords/TiltMate/compare/v1.6.1...v1.7.0
[1.6.1]: https://github.com/linuxswords/TiltMate/compare/v1.6.0...v1.6.1
[1.6.0]: https://github.com/linuxswords/TiltMate/compare/v1.5.0...v1.6.0
[1.5.0]: https://github.com/linuxswords/TiltMate/compare/v1.4.0...v1.5.0
[1.4.0]: https://github.com/linuxswords/TiltMate/compare/v1.3.0...v1.4.0
[1.3.0]: https://github.com/linuxswords/TiltMate/compare/v1.2.0...v1.3.0
[1.2.0]: https://github.com/linuxswords/TiltMate/compare/v1.1.0...v1.2.0
[1.1.0]: https://github.com/linuxswords/TiltMate/compare/v1.0.0...v1.1.0
[1.0.0]: https://github.com/linuxswords/TiltMate/releases/tag/v1.0.0
