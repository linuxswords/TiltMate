# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- **Show Move Counter** - New advanced setting to display the number of moves made during a game
  - Toggle switch in Advanced Settings screen
  - Move counter display on main game screen (18sp, white text)
  - Counter increments on each tilt/move
  - Counter resets when game restarts
  - Setting defaults to disabled for existing users
- Custom gear icon (ic_settings_gear.xml) for Advanced Settings button

### Changed
- **Java Version** - Upgraded project from Java 17 to Java 21
  - Updated build.gradle compilation targets
  - Updated all documentation (README.md, TESTING.md, Makefile)
  - Updated setup-dev-env.sh installation script
- **Settings Screen UI Improvements**
  - Swapped positions and actions of quit/return buttons
    - Exit button (revert icon) now returns to game
    - Cancel button (power icon) now quits app
  - Improved button layout and alignment
    - Buttons centered horizontally between time settings and right edge
    - Gear icon aligned vertically with gap between time setting rows
    - Reduced spacing between buttons (12dp margins)
- **Advanced Settings Switch Styling** - Enhanced visual feedback
  - Active switches show green accent color
  - Inactive switches show transparent/gray color
  - Applied to both ticking sound and move counter switches

### Fixed
- **Clock State Preservation** - Fixed bug where clocks would reset when navigating to/from settings
  - Added proper Activity lifecycle state management (onSaveInstanceState, onPause, onResume)
  - Clock times now preserved across configuration changes and navigation
  - Move count preserved when returning from settings
  - Running clock state tracked and restored correctly
  - Added getRemainingTime() and setRemainingTime() methods to PlayerClock and PausableCountDownTimer
- **Navigation Pattern** - Changed SettingsActivity to use finish() instead of creating new MainActivity instances

## [1.0.0] - Initial Release

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

[Unreleased]: https://github.com/linuxswords/TiltMate/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/linuxswords/TiltMate/releases/tag/v1.0.0
