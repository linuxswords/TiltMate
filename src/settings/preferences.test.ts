import { describe, it, expect, beforeEach, vi } from 'vitest';
import {
  setTimeSetting, getTimeSetting,
  setTickingEnabled, isTickingEnabled,
  setTiltSensitivity, getTiltSensitivity,
  setShowMovesEnabled, isShowMovesEnabled,
  setShowHintsEnabled, isShowHintsEnabled,
} from './preferences';
import { PRESETS, createCustom } from './time-settings';

function createMockStorage(): Storage {
  const store = new Map<string, string>();
  return {
    getItem: (key: string) => store.get(key) ?? null,
    setItem: (key: string, value: string) => { store.set(key, value); },
    removeItem: (key: string) => { store.delete(key); },
    clear: () => { store.clear(); },
    get length() { return store.size; },
    key: (index: number) => [...store.keys()][index] ?? null,
  };
}

beforeEach(() => {
  vi.stubGlobal('localStorage', createMockStorage());
});

describe('time setting persistence', () => {
  it('returns default (10+5) when nothing saved', () => {
    const ts = getTimeSetting();
    expect(ts.minutes).toBe(10);
    expect(ts.increment).toBe(5);
  });

  it('persists and restores a preset', () => {
    setTimeSetting(PRESETS[0]); // 3+0
    const ts = getTimeSetting();
    expect(ts.minutes).toBe(3);
    expect(ts.increment).toBe(0);
  });

  it('persists and restores a custom time', () => {
    const custom = createCustom(25, 15);
    setTimeSetting(custom);
    const ts = getTimeSetting();
    expect(ts.minutes).toBe(25);
    expect(ts.increment).toBe(15);
    expect(ts.name).toBe('CUSTOM');
  });

  it('clears custom values when switching to preset', () => {
    setTimeSetting(createCustom(25, 15));
    setTimeSetting(PRESETS[2]); // 5+0
    const ts = getTimeSetting();
    expect(ts.minutes).toBe(5);
    expect(ts.increment).toBe(0);
  });
});

describe('ticking enabled', () => {
  it('defaults to false', () => {
    expect(isTickingEnabled()).toBe(false);
  });

  it('persists true', () => {
    setTickingEnabled(true);
    expect(isTickingEnabled()).toBe(true);
  });
});

describe('tilt sensitivity', () => {
  it('defaults to 1 (medium)', () => {
    expect(getTiltSensitivity()).toBe(1);
  });

  it('persists values', () => {
    setTiltSensitivity(0);
    expect(getTiltSensitivity()).toBe(0);
    setTiltSensitivity(2);
    expect(getTiltSensitivity()).toBe(2);
  });
});

describe('show moves', () => {
  it('defaults to false', () => {
    expect(isShowMovesEnabled()).toBe(false);
  });

  it('persists true', () => {
    setShowMovesEnabled(true);
    expect(isShowMovesEnabled()).toBe(true);
  });
});

describe('show hints', () => {
  it('defaults to true', () => {
    expect(isShowHintsEnabled()).toBe(true);
  });

  it('persists false', () => {
    setShowHintsEnabled(false);
    expect(isShowHintsEnabled()).toBe(false);
  });
});
