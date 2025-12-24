# Testing Guide

## Quick Start

```bash
# Setup (first time)
./setup-dev-env.sh

# Run tests
make test

# View reports
make test-report
```

## Test Files

**Unit Tests** (`app/src/test/`)
- `TimeFormatterTest.java` - Time formatting
- `TimeSettingsTest.java` - Time presets (3+0, 5+5, etc.)
- `TimeSettingsManagerTest.java` - Settings management

**Instrumented Tests** (`app/src/androidTest/`)
- `ExampleInstrumentedTest.java` - Android context

## Running Tests

### Using Makefile
```bash
make test              # Unit tests only
make test-all          # Unit + instrumented (needs device)
make test-verbose      # Detailed output
```

### Using Gradle
```bash
./gradlew test                    # Unit tests
./gradlew connectedAndroidTest    # Instrumented tests
```

## Test Reports

Reports are generated in `app/build/reports/tests/testDebugUnitTest/index.html`

## Prerequisites

- **JDK 21+** - Required
- **Android SDK** - Optional for unit tests, required for APK build

Setup:
```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
export ANDROID_HOME=$HOME/Android/Sdk
```

## Writing Tests

### Simple Test
```java
@Test
void testSomething() {
    assertThat(result).isEqualTo(expected);
}
```

### Parameterized Test
```java
static Stream<Arguments> testData() {
    return Stream.of(
        arguments(input1, expected1),
        arguments(input2, expected2)
    );
}

@ParameterizedTest
@MethodSource("testData")
void testWithParameters(String input, String expected) {
    assertThat(process(input)).isEqualTo(expected);
}
```

## Troubleshooting

**"JAVA_HOME is not set"**
```bash
sudo apt-get install openjdk-17-jdk
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
```

**"No connected devices"** (for instrumented tests)
```bash
adb devices              # Check devices
emulator -avd <name>     # Start emulator
```
