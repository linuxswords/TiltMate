import { describe, it, expect, vi, beforeEach } from 'vitest';
import { PlayerClock } from './player-clock';

// Mock requestAnimationFrame / cancelAnimationFrame
beforeEach(() => {
  vi.stubGlobal('requestAnimationFrame', vi.fn().mockReturnValue(1));
  vi.stubGlobal('cancelAnimationFrame', vi.fn());
});

describe('PlayerClock', () => {
  it('initializes with correct time', () => {
    const clock = new PlayerClock(300_000, 5); // 5 min, 5s increment
    expect(clock.getRemainingTime()).toBe(300_000);
    expect(clock.getDisplayText()).toBe('5:00');
    expect(clock.isRunning()).toBe(false);
  });

  it('starts and sets running state', () => {
    const clock = new PlayerClock(300_000, 0);
    clock.start();
    expect(clock.isRunning()).toBe(true);
    expect(requestAnimationFrame).toHaveBeenCalled();
  });

  it('does not double-start', () => {
    const clock = new PlayerClock(300_000, 0);
    clock.start();
    clock.start();
    expect(requestAnimationFrame).toHaveBeenCalledTimes(1);
  });

  it('pauses and sets not-running state', () => {
    const clock = new PlayerClock(300_000, 0);
    clock.start();
    clock.pause();
    expect(clock.isRunning()).toBe(false);
    expect(cancelAnimationFrame).toHaveBeenCalled();
  });

  it('does nothing on pause when not running', () => {
    const clock = new PlayerClock(300_000, 0);
    clock.pause();
    expect(cancelAnimationFrame).not.toHaveBeenCalled();
  });

  it('adds increment', () => {
    const clock = new PlayerClock(300_000, 5);
    clock.addIncrement();
    expect(clock.getRemainingTime()).toBe(305_000);
  });

  it('does not add increment when increment is 0', () => {
    const clock = new PlayerClock(300_000, 0);
    clock.addIncrement();
    expect(clock.getRemainingTime()).toBe(300_000);
  });

  it('restarts to initial time', () => {
    const clock = new PlayerClock(300_000, 5);
    clock.addIncrement();
    clock.restart();
    expect(clock.getRemainingTime()).toBe(300_000);
    expect(clock.isRunning()).toBe(false);
  });

  it('restarts with new time', () => {
    const clock = new PlayerClock(300_000, 5);
    clock.restart(600_000, 10);
    expect(clock.getRemainingTime()).toBe(600_000);
  });

  it('sets remaining time', () => {
    const clock = new PlayerClock(300_000, 0);
    clock.setRemainingTime(100_000);
    expect(clock.getRemainingTime()).toBe(100_000);
  });

  it('reports warning state below 10 seconds', () => {
    const clock = new PlayerClock(300_000, 0);
    expect(clock.isWarning()).toBe(false);
    clock.setRemainingTime(9_999);
    expect(clock.isWarning()).toBe(true);
  });

  it('fires onTick callback', () => {
    const clock = new PlayerClock(300_000, 0);
    const tickFn = vi.fn();
    clock.onTick = tickFn;
    clock.setRemainingTime(5_000);
    expect(tickFn).toHaveBeenCalledWith('5.0', true);
  });

  it('fires onFinished when time runs out', () => {
    const clock = new PlayerClock(100, 0);
    const finishFn = vi.fn();
    clock.onFinished = finishFn;

    // Start to get the tick registered
    clock.start();
    const tickFn = vi.mocked(requestAnimationFrame).mock.calls[0][0];

    // First tick sets lastTimestamp
    tickFn(1000);
    // Second tick elapses 200ms (more than 100ms remaining)
    tickFn(1200);

    expect(finishFn).toHaveBeenCalled();
    expect(clock.getRemainingTime()).toBe(0);
    expect(clock.isRunning()).toBe(false);
  });
});
