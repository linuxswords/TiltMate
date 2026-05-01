import type { TimeSettings } from '../../settings/time-settings';
import { PRESETS, createCustom, isCustom } from '../../settings/time-settings';
import * as prefs from '../../settings/preferences';
import { IntroView } from './intro-view';

export class SettingsView {
  private el: HTMLElement;
  private onClose: (changed: boolean) => void;
  private currentSetting: TimeSettings;

  constructor(container: HTMLElement, onClose: (changed: boolean) => void) {
    this.onClose = onClose;
    this.currentSetting = prefs.getTimeSetting();

    this.el = document.createElement('div');
    this.el.className = 'settings-overlay';
    this.el.innerHTML = this.render();
    container.appendChild(this.el);

    this.bindEvents();
    this.highlightActive();
  }

  private render(): string {
    const presetButtons = PRESETS.map(
      (p) =>
        `<button class="preset-btn" data-name="${p.name}" data-minutes="${p.minutes}" data-increment="${p.increment}">${p.label}</button>`,
    ).join('');

    return `
      <div class="settings-panel">
        <div class="settings-header">
          <h2>Time Control</h2>
          <button class="close-btn" id="settingsClose">&times;</button>
        </div>
        <div class="preset-grid">${presetButtons}</div>
        <div class="custom-section">
          <h3>Custom</h3>
          <div class="custom-controls">
            <div class="custom-field">
              <label>Minutes</label>
              <div class="stepper">
                <button class="step-btn" id="minMinus">&minus;</button>
                <span id="customMinutes">${isCustom(this.currentSetting) ? this.currentSetting.minutes : 10}</span>
                <button class="step-btn" id="minPlus">+</button>
              </div>
            </div>
            <div class="custom-field">
              <label>Increment (s)</label>
              <div class="stepper">
                <button class="step-btn" id="incMinus">&minus;</button>
                <span id="customIncrement">${isCustom(this.currentSetting) ? this.currentSetting.increment : 0}</span>
                <button class="step-btn" id="incPlus">+</button>
              </div>
            </div>
            <button class="apply-custom-btn" id="applyCustom">Apply</button>
          </div>
        </div>
        <div class="settings-nav">
          <button class="nav-btn" id="advancedSettingsBtn">Advanced Settings</button>
          <button class="nav-btn" id="infoBtn">About</button>
        </div>
      </div>
    `;
  }

  private bindEvents(): void {
    // Close
    this.el.querySelector('#settingsClose')!.addEventListener('click', () => this.close(false));
    this.el.addEventListener('click', (e) => {
      if (e.target === this.el) this.close(false);
    });

    // Preset selection
    this.el.querySelectorAll('.preset-btn').forEach((btn) => {
      btn.addEventListener('click', () => {
        const name = (btn as HTMLElement).dataset.name!;
        const preset = PRESETS.find((p) => p.name === name)!;
        prefs.setTimeSetting(preset);
        this.currentSetting = preset;
        this.highlightActive();
        this.close(true);
      });
    });

    // Custom steppers with hold-to-repeat
    this.setupStepper('minMinus', 'customMinutes', -1, 1, 180);
    this.setupStepper('minPlus', 'customMinutes', 1, 1, 180);
    this.setupStepper('incMinus', 'customIncrement', -1, 0, 60);
    this.setupStepper('incPlus', 'customIncrement', 1, 0, 60);

    // Apply custom
    this.el.querySelector('#applyCustom')!.addEventListener('click', () => {
      const minutes = parseInt(this.el.querySelector('#customMinutes')!.textContent!, 10);
      const increment = parseInt(this.el.querySelector('#customIncrement')!.textContent!, 10);
      try {
        const custom = createCustom(minutes, increment);
        prefs.setTimeSetting(custom);
        this.currentSetting = custom;
        this.highlightActive();
        this.close(true);
      } catch {
        // Invalid range — ignore
      }
    });

    // Navigation
    this.el.querySelector('#advancedSettingsBtn')!.addEventListener('click', () => {
      this.showAdvanced();
    });

    this.el.querySelector('#infoBtn')!.addEventListener('click', () => {
      this.showInfo();
    });
  }

  private setupStepper(btnId: string, displayId: string, delta: number, min: number, max: number): void {
    const btn = this.el.querySelector(`#${btnId}`)!;
    const display = this.el.querySelector(`#${displayId}`)!;
    let interval = 0;
    let timeout = 0;

    const step = () => {
      const current = parseInt(display.textContent!, 10);
      const next = Math.min(max, Math.max(min, current + delta));
      display.textContent = String(next);
    };

    btn.addEventListener('pointerdown', (e) => {
      e.preventDefault();
      step();
      timeout = window.setTimeout(() => {
        interval = window.setInterval(step, 100);
      }, 500);
    });

    const stopRepeat = () => {
      clearTimeout(timeout);
      clearInterval(interval);
    };
    btn.addEventListener('pointerup', stopRepeat);
    btn.addEventListener('pointerleave', stopRepeat);
    btn.addEventListener('pointercancel', stopRepeat);
  }

  private highlightActive(): void {
    this.el.querySelectorAll('.preset-btn').forEach((btn) => {
      const el = btn as HTMLElement;
      const matches =
        el.dataset.minutes === String(this.currentSetting.minutes) &&
        el.dataset.increment === String(this.currentSetting.increment);
      el.classList.toggle('active', matches);
    });
  }

  private showAdvanced(): void {
    this.el.querySelector('.settings-panel')!.innerHTML = `
      <div class="settings-header">
        <h2>Advanced Settings</h2>
        <button class="close-btn" id="advancedBack">&larr;</button>
      </div>
      <div class="advanced-options">
        <label class="toggle-row">
          <span>Clock ticking sound</span>
          <input type="checkbox" id="tickingToggle" ${prefs.isTickingEnabled() ? 'checked' : ''}>
        </label>
        <label class="toggle-row">
          <span>Show move counter</span>
          <input type="checkbox" id="movesToggle" ${prefs.isShowMovesEnabled() ? 'checked' : ''}>
        </label>
        <label class="toggle-row">
          <span>Show hints</span>
          <input type="checkbox" id="hintsToggle" ${prefs.isShowHintsEnabled() ? 'checked' : ''}>
        </label>
        <div class="sensitivity-section">
          <span>Tilt sensitivity</span>
          <div class="sensitivity-buttons">
            <button class="sens-btn ${prefs.getTiltSensitivity() === 0 ? 'active' : ''}" data-level="0">Low</button>
            <button class="sens-btn ${prefs.getTiltSensitivity() === 1 ? 'active' : ''}" data-level="1">Medium</button>
            <button class="sens-btn ${prefs.getTiltSensitivity() === 2 ? 'active' : ''}" data-level="2">High</button>
          </div>
        </div>
        <div class="keyboard-hint">
          <p>Keyboard controls (desktop):</p>
          <p><kbd>&larr;</kbd> / <kbd>&rarr;</kbd> Tilt &nbsp; <kbd>Space</kbd> Tap &nbsp; <kbd>R</kbd> Reset</p>
        </div>
        <div class="reload-section">
          <button class="reload-btn" id="reloadAppBtn">Reload App</button>
          <p class="reload-hint">Clears the cache and fetches the latest version.</p>
        </div>
      </div>
    `;

    this.el.querySelector('#advancedBack')!.addEventListener('click', () => {
      // Re-render main settings
      this.el.innerHTML = this.render();
      this.bindEvents();
      this.highlightActive();
    });

    this.el.querySelector('#tickingToggle')!.addEventListener('change', (e) => {
      prefs.setTickingEnabled((e.target as HTMLInputElement).checked);
    });

    this.el.querySelector('#movesToggle')!.addEventListener('change', (e) => {
      prefs.setShowMovesEnabled((e.target as HTMLInputElement).checked);
    });

    this.el.querySelector('#hintsToggle')!.addEventListener('change', (e) => {
      prefs.setShowHintsEnabled((e.target as HTMLInputElement).checked);
    });

    this.el.querySelectorAll('.sens-btn').forEach((btn) => {
      btn.addEventListener('click', () => {
        const level = parseInt((btn as HTMLElement).dataset.level!, 10);
        prefs.setTiltSensitivity(level);
        this.el.querySelectorAll('.sens-btn').forEach((b) => b.classList.remove('active'));
        btn.classList.add('active');
      });
    });

    this.el.querySelector('#reloadAppBtn')!.addEventListener('click', async () => {
      const btn = this.el.querySelector('#reloadAppBtn') as HTMLButtonElement;
      btn.disabled = true;
      btn.textContent = 'Reloading…';
      try {
        if ('serviceWorker' in navigator) {
          const regs = await navigator.serviceWorker.getRegistrations();
          await Promise.all(regs.map((r) => r.unregister()));
        }
        if ('caches' in window) {
          const keys = await caches.keys();
          await Promise.all(keys.map((k) => caches.delete(k)));
        }
      } finally {
        const url = new URL(window.location.href);
        url.searchParams.set('_r', Date.now().toString());
        window.location.replace(url.toString());
      }
    });
  }

  private showInfo(): void {
    this.el.querySelector('.settings-panel')!.innerHTML = `
      <div class="settings-header">
        <h2>About TiltMate</h2>
        <button class="close-btn" id="infoBack">&larr;</button>
      </div>
      <div class="info-content">
        <p>A tilt-based chess clock. Place your phone on a seesaw base and tilt to switch clocks.</p>
        <p class="version">Web Version</p>
        <div class="info-links">
          <a href="https://github.com/linuxswords/TiltMate" target="_blank" rel="noopener" class="info-link">GitHub</a>
          <button class="info-link info-link-btn" id="showIntroBtn">Show intro</button>
        </div>
        <div class="base-model-section">
          <p>3D-printable seesaw base model:</p>
          <a href="/models/model-1.stl" download class="info-link">Download STL</a>
        </div>
      </div>
    `;

    this.el.querySelector('#infoBack')!.addEventListener('click', () => {
      this.el.innerHTML = this.render();
      this.bindEvents();
      this.highlightActive();
    });

    this.el.querySelector('#showIntroBtn')!.addEventListener('click', () => {
      const parent = this.el.parentElement!;
      this.close(false);
      new IntroView(parent, () => {});
    });
  }

  private close(changed: boolean): void {
    this.el.remove();
    this.onClose(changed);
  }
}
