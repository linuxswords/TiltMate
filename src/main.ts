import { PlayerClock } from './game/player-clock';
import { TiltSensor } from './sensor/tilt-sensor';
import { KeyboardFallback } from './sensor/keyboard-fallback';
import { TickingSoundManager } from './sound/ticking-sound';
import { GestureDetector } from './ui/gesture';
import { SettingsView } from './ui/views/settings-view';
import { minutesAsMs } from './settings/time-settings';
import * as prefs from './settings/preferences';
import './style.css';

// --- State ---
let currentTiltDegree = 0;
let moveCount = 0;
let gameStarted = false;
let gameFinished = false;
let settingsOpen = false;

// --- DOM ---
const app = document.getElementById('app')!;
app.innerHTML = `
  <div id="game" class="game-screen">
    <div class="clock-side left-side">
      <div class="color-indicator" id="colorLeft"></div>
      <div class="clock-display" id="clockLeft"></div>
    </div>
    <div class="center-strip">
      <div class="time-label" id="timeLabel"></div>
      <div class="move-count" id="moveCount"></div>
      <div class="hint" id="hint"></div>
    </div>
    <div class="clock-side right-side">
      <div class="color-indicator" id="colorRight"></div>
      <div class="clock-display" id="clockRight"></div>
    </div>
  </div>
`;

const clockLeftEl = document.getElementById('clockLeft')!;
const clockRightEl = document.getElementById('clockRight')!;
const timeLabelEl = document.getElementById('timeLabel')!;
const moveCountEl = document.getElementById('moveCount')!;
const hintEl = document.getElementById('hint')!;
const colorLeftEl = document.getElementById('colorLeft')!;
const colorRightEl = document.getElementById('colorRight')!;
const gameEl = document.getElementById('game')!;

// --- Initialize clocks ---
function createClocks() {
  const ts = prefs.getTimeSetting();
  const ms = minutesAsMs(ts);

  const left = new PlayerClock(ms, ts.increment);
  const right = new PlayerClock(ms, ts.increment);

  left.onTick = (text, warn) => {
    clockLeftEl.textContent = text;
    clockLeftEl.classList.toggle('warn', warn);
  };
  right.onTick = (text, warn) => {
    clockRightEl.textContent = text;
    clockRightEl.classList.toggle('warn', warn);
  };

  left.onFinished = () => onClockFinished();
  right.onFinished = () => onClockFinished();

  // Show initial time
  const initialText = left.getDisplayText();
  clockLeftEl.textContent = initialText;
  clockRightEl.textContent = initialText;
  clockLeftEl.classList.remove('warn');
  clockRightEl.classList.remove('warn');

  timeLabelEl.textContent = ts.label;

  return { left, right };
}

let clocks = createClocks();

// --- Sound ---
const tickingSound = new TickingSoundManager();
tickingSound.init();
tickingSound.setEnabled(prefs.isTickingEnabled());

// --- Tilt sensor ---
const tiltSensor = new TiltSensor();
tiltSensor.setSensitivity(prefs.getTiltSensitivity());
tiltSensor.setCallback(onTilt);

// --- Keyboard fallback ---
const keyboard = new KeyboardFallback();
keyboard.setCallback((action) => {
  switch (action) {
    case 'tilt-left':
      onTilt(-10);
      break;
    case 'tilt-right':
      onTilt(10);
      break;
    case 'tilt-center':
      onTilt(0);
      break;
    case 'tap':
      onSingleClick();
      break;
    case 'reset':
      restartAllClocks();
      break;
  }
});
keyboard.start();

// Start tilt sensor
if (tiltSensor.isAvailable()) {
  tiltSensor.requestPermission().then((granted) => {
    if (granted) tiltSensor.start();
  });
}

// --- Lock landscape orientation ---
screen.orientation?.lock?.('landscape').catch(() => {});

// --- Wake lock ---
let wakeLock: WakeLockSentinel | null = null;

async function requestWakeLock() {
  if ('wakeLock' in navigator) {
    try {
      wakeLock = await navigator.wakeLock.request('screen');
    } catch {
      // Wake lock request failed (e.g. low battery)
    }
  }
}

document.addEventListener('visibilitychange', () => {
  if (document.visibilityState === 'visible' && gameStarted) {
    requestWakeLock();
  }
});

// --- Fullscreen ---
function requestFullscreen() {
  if (document.fullscreenElement) return;
  document.documentElement.requestFullscreen?.().catch(() => {});
}

// --- Game logic ---
function onTilt(degree: number) {
  if (gameFinished || settingsOpen) return;

  if (degree !== 0) {
    if (currentTiltDegree === 0) {
      currentTiltDegree = degree;
      if (!gameStarted) updateColorIndicators(degree);
    } else if (Math.sign(currentTiltDegree) !== Math.sign(degree)) {
      currentTiltDegree = degree;
      if (!gameStarted) {
        updateColorIndicators(degree);
      }
      if (gameStarted) {
        if (Math.sign(currentTiltDegree) === -1) {
          toggleSwitch(clocks.left, clocks.right);
        } else {
          toggleSwitch(clocks.right, clocks.left);
        }
      }
    }
  }
}

function toggleSwitch(toActivate: PlayerClock, toPause: PlayerClock) {
  toPause.addIncrement();
  toPause.pause();
  toActivate.start();
  tickingSound.start();
  moveCount++;
  updateMoveDisplay();
  updateClockOpacity();
}

function onSingleClick() {
  if (gameFinished || settingsOpen) return;

  if (!gameStarted && currentTiltDegree !== 0) {
    if (currentTiltDegree < 0) {
      clocks.left.start();
    } else {
      clocks.right.start();
    }
    tickingSound.start();
    gameStarted = true;
    requestWakeLock();
    requestFullscreen();
    showHint('Tap to pause');
    updateClockOpacity();
  } else if (gameStarted) {
    if (!clocks.left.isRunning() && !clocks.right.isRunning()) {
      // Resume
      if (currentTiltDegree !== 0) {
        if (currentTiltDegree < 0) {
          clocks.left.start();
        } else {
          clocks.right.start();
        }
        tickingSound.start();
        showHint('Tap to pause');
        updateClockOpacity();
      }
    } else {
      // Pause
      clocks.left.pause();
      clocks.right.pause();
      tickingSound.stop();
      showHint('Tap to continue \u00b7 Long press for settings \u00b7 Double tap to reset');
      updateClockOpacity();
    }
  }
}

function restartAllClocks() {
  clocks.left.pause();
  clocks.right.pause();
  tickingSound.stop();

  clocks = createClocks();
  moveCount = 0;
  gameStarted = false;
  gameFinished = false;
  updateMoveDisplay();
  updateClockOpacity();
  updateColorIndicators(currentTiltDegree);
  showHint('Tap to start \u00b7 Long press for settings');
}

function onClockFinished() {
  tickingSound.stop();
  gameFinished = true;
}

function updateColorIndicators(degree: number) {
  if (degree < 0) {
    colorLeftEl.textContent = '\u265F';
    colorLeftEl.className = 'color-indicator black-piece';
    colorRightEl.textContent = '\u2659';
    colorRightEl.className = 'color-indicator white-piece';
    clockLeftEl.classList.remove('dimmed');
    clockRightEl.classList.add('dimmed');
  } else if (degree > 0) {
    colorRightEl.textContent = '\u265F';
    colorRightEl.className = 'color-indicator black-piece';
    colorLeftEl.textContent = '\u2659';
    colorLeftEl.className = 'color-indicator white-piece';
    clockRightEl.classList.remove('dimmed');
    clockLeftEl.classList.add('dimmed');
  }
}

function updateClockOpacity() {
  if (!gameStarted) return;
  const leftRunning = clocks.left.isRunning();
  const rightRunning = clocks.right.isRunning();
  clockLeftEl.classList.toggle('dimmed', !leftRunning);
  clockRightEl.classList.toggle('dimmed', !rightRunning);
}

function updateMoveDisplay() {
  if (prefs.isShowMovesEnabled()) {
    moveCountEl.textContent = `${moveCount} moves`;
    moveCountEl.style.display = '';
  } else {
    moveCountEl.style.display = 'none';
  }
}

function showHint(text: string) {
  if (prefs.isShowHintsEnabled()) {
    hintEl.textContent = text;
    hintEl.style.display = '';
  } else {
    hintEl.style.display = 'none';
  }
}

function openSettings() {
  if (settingsOpen) return;
  settingsOpen = true;

  // Pause clocks while in settings
  const leftWasRunning = clocks.left.isRunning();
  const rightWasRunning = clocks.right.isRunning();
  clocks.left.pause();
  clocks.right.pause();
  tickingSound.stop();

  new SettingsView(app, (changed) => {
    settingsOpen = false;

    // Reload preferences
    tickingSound.setEnabled(prefs.isTickingEnabled());
    tiltSensor.setSensitivity(prefs.getTiltSensitivity());
    updateMoveDisplay();
    if (!prefs.isShowHintsEnabled()) {
      hintEl.style.display = 'none';
    }

    if (changed || (!leftWasRunning && !rightWasRunning)) {
      // Time setting changed or clocks were idle — reset
      restartAllClocks();
    } else {
      // Resume whichever clock was running
      if (leftWasRunning) {
        clocks.left.start();
        tickingSound.start();
      } else if (rightWasRunning) {
        clocks.right.start();
        tickingSound.start();
      }
      updateClockOpacity();
    }
  });
}

// --- Gesture detection ---
new GestureDetector(gameEl, {
  onSingleClick,
  onDoubleClick: restartAllClocks,
  onLongPress: openSettings,
});

// --- Initial state ---
updateMoveDisplay();
showHint('Tap to start \u00b7 Long press for settings');

// Suppress unused variable warning
void wakeLock;

// --- Service Worker ---
if ('serviceWorker' in navigator) {
  navigator.serviceWorker.register('/sw.js').catch(() => {});
}
