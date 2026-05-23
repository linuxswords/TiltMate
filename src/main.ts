import { PlayerClock } from './game/player-clock';
import { TiltSensor } from './sensor/tilt-sensor';
import { KeyboardFallback } from './sensor/keyboard-fallback';
import { TickingSoundManager } from './sound/ticking-sound';
import { GestureDetector } from './ui/gesture';
import { SettingsView } from './ui/views/settings-view';
import { IntroView } from './ui/views/intro-view';
import { minutesAsMs } from './settings/time-settings';
import * as prefs from './settings/preferences';
import './style.css';

const PAWN_SVG = `
<svg viewBox="0 0 24 32" xmlns="http://www.w3.org/2000/svg" fill="currentColor" aria-hidden="true">
  <circle cx="12" cy="7" r="4.5"/>
  <rect x="7.5" y="12" width="9" height="2.5" rx="1"/>
  <polygon points="9.5,14.5 14.5,14.5 16.5,22 7.5,22"/>
  <rect x="4" y="22" width="16" height="2.5"/>
  <rect x="3" y="24" width="18" height="3" rx="0.5"/>
</svg>
`.trim();

function setPiece(el: HTMLElement, color: 'white' | 'black') {
  el.innerHTML = PAWN_SVG;
  el.className = `color-indicator ${color}-piece`;
}

// --- State ---
let currentTiltDegree = 0;
let moveCount = 0;
let gameStarted = false;
let gameFinished = false;
let settingsOpen = false;
let isTableMode = window.matchMedia('(orientation: portrait)').matches;
let sensorFlat = false;
let sensorGravityX = 0;

// --- DOM ---
const app = document.getElementById('app')!;
app.innerHTML = `
  <div id="game" class="game-screen">
    <div class="clock-side left-side" id="leftSide">
      <div class="color-indicator" id="colorLeft"></div>
      <div class="clock-display" id="clockLeft"></div>
    </div>
    <div class="center-strip">
      <div class="time-label" id="timeLabel"></div>
      <div class="move-count" id="moveCount"></div>
      <div class="hint" id="hint"></div>
    </div>
    <div class="clock-side right-side" id="rightSide">
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
const leftSideEl = document.getElementById('leftSide')!;
const rightSideEl = document.getElementById('rightSide')!;

// --- Mode detection ---
function applyMode(flat: boolean, gravityX = 0) {
  // A portrait viewport means the phone is held vertically — there's no real
  // seesaw possible, so use chess-clock (table) tap behavior. The sensor's
  // flat/upright reading only switches between table and seesaw when the
  // viewport is landscape.
  const viewportIsPortrait = window.innerHeight > window.innerWidth;
  const tableMode = flat || viewportIsPortrait;
  isTableMode = tableMode;
  gameEl.classList.toggle('flat-mode', flat);

  const needsRotation = !flat && viewportIsPortrait;
  gameEl.classList.toggle('seesaw-rotated', needsRotation);
  gameEl.classList.toggle('seesaw-rotated-cw', needsRotation && gravityX >= 0);
  gameEl.classList.toggle('seesaw-rotated-ccw', needsRotation && gravityX < 0);

  if (tableMode && !gameStarted) {
    clearColorIndicators();
  }

  updateHints();
}

applyMode(isTableMode);

// Re-apply mode on viewport rotation (sensor handles flat/upright; viewport handles layout)
window.matchMedia('(orientation: portrait)').addEventListener('change', (e) => {
  if (tiltSensor.isAvailable()) {
    applyMode(sensorFlat, sensorGravityX);
  } else {
    applyMode(e.matches);
  }
});

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
tiltSensor.setPostureCallback((posture, gravityX) => {
  sensorFlat = posture === 'flat';
  sensorGravityX = gravityX;
  applyMode(sensorFlat, sensorGravityX);
});

if (tiltSensor.isAvailable()) {
  tiltSensor.requestPermission().then((granted) => {
    if (granted) tiltSensor.start();
  });
}

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

// --- Wake lock ---
let wakeLock: WakeLockSentinel | null = null;

async function requestWakeLock() {
  if ('wakeLock' in navigator) {
    try {
      wakeLock = await navigator.wakeLock.request('screen');
    } catch {
      // Wake lock request failed
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

// =====================
// Seesaw mode (landscape)
// =====================

function onTilt(degree: number) {
  if (isTableMode) return;
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

function onSingleClick() {
  if (gameFinished || settingsOpen) return;

  if (isTableMode) {
    // Sides stop propagation, so this only fires for center-strip taps.
    if (gameStarted && (clocks.left.isRunning() || clocks.right.isRunning())) {
      clocks.left.pause();
      clocks.right.pause();
      tickingSound.stop();
      showHint('Tap your side to resume · Long press for settings · Double tap to reset');
      updateClockOpacity();
    }
    return;
  }

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
      clocks.left.pause();
      clocks.right.pause();
      tickingSound.stop();
      showHint('Tap to continue \u00b7 Long press for settings \u00b7 Double tap to reset');
      updateClockOpacity();
    }
  }
}

// =====================
// Table mode (portrait)
// =====================

function onSideTap(side: 'left' | 'right') {
  if (!isTableMode) return;
  if (gameFinished || settingsOpen) return;

  const tappedClock = side === 'left' ? clocks.left : clocks.right;
  const otherClock = side === 'left' ? clocks.right : clocks.left;

  if (!gameStarted) {
    // Tapper becomes black, the other side (white) starts running
    setColorForSide(side, 'black');
    otherClock.start();
    tickingSound.start();
    gameStarted = true;
    requestWakeLock();
    requestFullscreen();
    showHint('Tap your side to switch');
    updateClockOpacity();
    return;
  }

  if (clocks.left.isRunning()) {
    toggleSwitch(clocks.right, clocks.left);
    return;
  }
  if (clocks.right.isRunning()) {
    toggleSwitch(clocks.left, clocks.right);
    return;
  }

  tappedClock.start();
  tickingSound.start();
  showHint('Tap your side to switch');
  updateClockOpacity();
}

function setupSideTap(el: HTMLElement, side: 'left' | 'right') {
  let longPressTimer = 0;
  let didLongPress = false;

  el.addEventListener('pointerdown', (e) => {
    if (!isTableMode) return;
    e.stopPropagation();
    didLongPress = false;
    longPressTimer = window.setTimeout(() => {
      didLongPress = true;
      openSettings();
    }, 600);
  });

  el.addEventListener('pointerup', (e) => {
    if (!isTableMode) return;
    e.stopPropagation();
    clearTimeout(longPressTimer);
    if (!didLongPress) onSideTap(side);
  });

  el.addEventListener('pointercancel', () => {
    clearTimeout(longPressTimer);
  });
}

setupSideTap(leftSideEl, 'left');
setupSideTap(rightSideEl, 'right');

// =====================
// Shared
// =====================

function toggleSwitch(toActivate: PlayerClock, toPause: PlayerClock) {
  toPause.addIncrement();
  toPause.pause();
  toActivate.start();
  tickingSound.start();
  moveCount++;
  updateMoveDisplay();
  updateClockOpacity();
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
  if (isTableMode) {
    clearColorIndicators();
  } else {
    updateColorIndicators(currentTiltDegree);
  }
  updateHints();
}

function onClockFinished() {
  tickingSound.stop();
  gameFinished = true;
  updateClockOpacity();
}

function clearColorIndicators() {
  colorLeftEl.textContent = '';
  colorLeftEl.className = 'color-indicator';
  colorRightEl.textContent = '';
  colorRightEl.className = 'color-indicator';
}

function setColorForSide(side: 'left' | 'right', color: 'white' | 'black') {
  const sideEl = side === 'left' ? colorLeftEl : colorRightEl;
  const otherEl = side === 'left' ? colorRightEl : colorLeftEl;
  if (color === 'black') {
    setPiece(sideEl, 'black');
    setPiece(otherEl, 'white');
  } else {
    setPiece(sideEl, 'white');
    setPiece(otherEl, 'black');
  }
}

function updateColorIndicators(degree: number) {
  if (degree < 0) {
    setPiece(colorLeftEl, 'white');
    setPiece(colorRightEl, 'black');
    clockLeftEl.classList.remove('dimmed');
    clockRightEl.classList.add('dimmed');
  } else if (degree > 0) {
    setPiece(colorRightEl, 'white');
    setPiece(colorLeftEl, 'black');
    clockRightEl.classList.remove('dimmed');
    clockLeftEl.classList.add('dimmed');
  }
}

function updateClockOpacity() {
  if (!gameStarted) {
    clockLeftEl.classList.remove('dimmed');
    clockRightEl.classList.remove('dimmed');
    leftSideEl.classList.remove('active-side');
    rightSideEl.classList.remove('active-side');
    return;
  }
  const leftRunning = clocks.left.isRunning();
  const rightRunning = clocks.right.isRunning();
  clockLeftEl.classList.toggle('dimmed', !leftRunning);
  clockRightEl.classList.toggle('dimmed', !rightRunning);
  leftSideEl.classList.toggle('active-side', leftRunning);
  rightSideEl.classList.toggle('active-side', rightRunning);
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

function updateHints() {
  if (!gameStarted) {
    if (isTableMode) {
      showHint('Tap your side to start \u00b7 Long press for settings');
    } else {
      showHint('Tap to start \u00b7 Long press for settings');
    }
  }
}

function openSettings() {
  if (settingsOpen) return;
  settingsOpen = true;

  const leftWasRunning = clocks.left.isRunning();
  const rightWasRunning = clocks.right.isRunning();
  clocks.left.pause();
  clocks.right.pause();
  tickingSound.stop();

  new SettingsView(app, (changed) => {
    settingsOpen = false;

    tickingSound.setEnabled(prefs.isTickingEnabled());
    tiltSensor.setSensitivity(prefs.getTiltSensitivity());
    updateMoveDisplay();
    if (!prefs.isShowHintsEnabled()) {
      hintEl.style.display = 'none';
    }

    if (changed || (!leftWasRunning && !rightWasRunning)) {
      restartAllClocks();
    } else {
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

// --- Gesture detection (seesaw mode: whole screen) ---
new GestureDetector(gameEl, {
  onSingleClick,
  onDoubleClick: restartAllClocks,
  onLongPress: openSettings,
});

// --- Initial state ---
updateMoveDisplay();
updateHints();

// --- First-visit intro ---
if (!prefs.isIntroSeen()) {
  new IntroView(app, () => {});
}

// Suppress unused variable warning
void wakeLock;

// --- Service Worker ---
if ('serviceWorker' in navigator) {
  navigator.serviceWorker.register('/sw.js').catch(() => {});
}
