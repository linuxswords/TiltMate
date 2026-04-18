import { describe, it, expect } from 'vitest';
import { formatTime } from './time-formatter';

describe('formatTime', () => {
  it('formats zero as 0.0', () => {
    expect(formatTime(0)).toBe('0.0');
  });

  it('formats negative as 0.0', () => {
    expect(formatTime(-100)).toBe('0.0');
  });

  it('formats minutes and seconds with zero-padded seconds', () => {
    expect(formatTime(600_000)).toBe('10:00');
    expect(formatTime(65_000)).toBe('1:05');
    expect(formatTime(180_000)).toBe('3:00');
  });

  it('formats single-digit minutes without padding', () => {
    expect(formatTime(60_000)).toBe('1:00');
    expect(formatTime(120_500)).toBe('2:00');
  });

  it('switches to seconds.tenths format below 10 seconds', () => {
    expect(formatTime(9_999)).toBe('9.9');
    expect(formatTime(9_100)).toBe('9.1');
    expect(formatTime(5_000)).toBe('5.0');
    expect(formatTime(1_500)).toBe('1.5');
    expect(formatTime(500)).toBe('0.5');
    expect(formatTime(100)).toBe('0.1');
  });

  it('truncates tenths (does not round)', () => {
    expect(formatTime(9_950)).toBe('9.9');
    expect(formatTime(1_990)).toBe('1.9');
  });

  it('handles exact 10 second boundary as M:SS', () => {
    expect(formatTime(10_000)).toBe('0:10');
  });
});
