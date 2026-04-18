import { describe, it, expect } from 'vitest';
import { PRESETS, DEFAULT_PRESET, createCustom, minutesAsMs, isCustom } from './time-settings';

describe('PRESETS', () => {
  it('has 7 FIDE presets', () => {
    expect(PRESETS).toHaveLength(7);
  });

  it('has correct labels', () => {
    const labels = PRESETS.map(p => p.label);
    expect(labels).toEqual(['3+0', '3+2', '5+0', '5+3', '10+0', '10+5', '15+10']);
  });

  it('default preset is 10+5', () => {
    expect(DEFAULT_PRESET.minutes).toBe(10);
    expect(DEFAULT_PRESET.increment).toBe(5);
  });
});

describe('createCustom', () => {
  it('creates a custom time setting', () => {
    const custom = createCustom(20, 10);
    expect(custom.minutes).toBe(20);
    expect(custom.increment).toBe(10);
    expect(custom.label).toBe('20+10');
    expect(custom.name).toBe('CUSTOM');
  });

  it('throws on minutes out of range', () => {
    expect(() => createCustom(0, 5)).toThrow(RangeError);
    expect(() => createCustom(181, 5)).toThrow(RangeError);
  });

  it('throws on increment out of range', () => {
    expect(() => createCustom(10, -1)).toThrow(RangeError);
    expect(() => createCustom(10, 61)).toThrow(RangeError);
  });

  it('accepts boundary values', () => {
    expect(createCustom(1, 0).label).toBe('1+0');
    expect(createCustom(180, 60).label).toBe('180+60');
  });
});

describe('minutesAsMs', () => {
  it('converts minutes to milliseconds', () => {
    expect(minutesAsMs(PRESETS[0])).toBe(180_000);  // 3 min
    expect(minutesAsMs(PRESETS[4])).toBe(600_000);  // 10 min
    expect(minutesAsMs(PRESETS[6])).toBe(900_000);  // 15 min
  });
});

describe('isCustom', () => {
  it('returns false for presets', () => {
    for (const preset of PRESETS) {
      expect(isCustom(preset)).toBe(false);
    }
  });

  it('returns true for custom settings', () => {
    expect(isCustom(createCustom(7, 3))).toBe(true);
  });
});
