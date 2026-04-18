import type { TimeSettings } from './time-settings';
import { PRESETS, DEFAULT_PRESET, createCustom } from './time-settings';

const PREFIX = 'tiltmate_';

function get(key: string): string | null {
  return localStorage.getItem(PREFIX + key);
}

function set(key: string, value: string): void {
  localStorage.setItem(PREFIX + key, value);
}

function remove(key: string): void {
  localStorage.removeItem(PREFIX + key);
}

export function setTimeSetting(ts: TimeSettings): void {
  if (ts.name === 'CUSTOM') {
    set('time_setting', 'CUSTOM');
    set('custom_minutes', String(ts.minutes));
    set('custom_increment', String(ts.increment));
  } else {
    set('time_setting', ts.name);
    remove('custom_minutes');
    remove('custom_increment');
  }
}

export function getTimeSetting(): TimeSettings {
  const name = get('time_setting') ?? DEFAULT_PRESET.name;

  if (name === 'CUSTOM') {
    const minutes = parseInt(get('custom_minutes') ?? '10', 10);
    const increment = parseInt(get('custom_increment') ?? '5', 10);
    try {
      return createCustom(minutes, increment);
    } catch {
      return DEFAULT_PRESET;
    }
  }

  return PRESETS.find(p => p.name === name) ?? DEFAULT_PRESET;
}

export function setTickingEnabled(enabled: boolean): void {
  set('ticking_enabled', String(enabled));
}

export function isTickingEnabled(): boolean {
  return get('ticking_enabled') === 'true';
}

export function setTiltSensitivity(level: number): void {
  set('tilt_sensitivity', String(level));
}

export function getTiltSensitivity(): number {
  const v = get('tilt_sensitivity');
  return v !== null ? parseInt(v, 10) : 1;
}

export function setShowMovesEnabled(enabled: boolean): void {
  set('show_moves', String(enabled));
}

export function isShowMovesEnabled(): boolean {
  return get('show_moves') === 'true';
}

export function setShowHintsEnabled(enabled: boolean): void {
  set('show_hints', String(enabled));
}

export function isShowHintsEnabled(): boolean {
  const v = get('show_hints');
  return v === null ? true : v === 'true';
}
