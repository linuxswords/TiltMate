import { formatTime } from './time-formatter';

const WARN_THRESHOLD_MS = 10_000;

export class PlayerClock {
  private remainingMs: number;
  private startTimeMs: number;
  private running = false;
  private lastTimestamp = 0;
  private rafId = 0;
  private incrementMs: number;

  public onTick: ((timeText: string, warn: boolean) => void) | null = null;
  public onFinished: (() => void) | null = null;

  constructor(initialMs: number, incrementSeconds: number) {
    this.remainingMs = initialMs;
    this.startTimeMs = initialMs;
    this.incrementMs = incrementSeconds * 1000;
  }

  private tick = (timestamp: number): void => {
    if (!this.running) return;

    if (this.lastTimestamp > 0) {
      const elapsed = timestamp - this.lastTimestamp;
      this.remainingMs -= elapsed;
    }
    this.lastTimestamp = timestamp;

    if (this.remainingMs <= 0) {
      this.remainingMs = 0;
      this.running = false;
      this.emitTick();
      this.onFinished?.();
      return;
    }

    this.emitTick();
    this.rafId = requestAnimationFrame(this.tick);
  };

  private emitTick(): void {
    const text = formatTime(this.remainingMs);
    const warn = this.remainingMs <= WARN_THRESHOLD_MS;
    this.onTick?.(text, warn);
  }

  start(): void {
    if (this.running) return;
    this.running = true;
    this.lastTimestamp = 0;
    this.rafId = requestAnimationFrame(this.tick);
  }

  pause(): void {
    if (!this.running) return;
    this.running = false;
    cancelAnimationFrame(this.rafId);
  }

  addIncrement(): void {
    if (this.incrementMs > 0) {
      this.remainingMs += this.incrementMs;
      this.emitTick();
    }
  }

  restart(newTimeMs?: number, newIncrementSeconds?: number): void {
    this.running = false;
    cancelAnimationFrame(this.rafId);
    if (newTimeMs !== undefined) {
      this.startTimeMs = newTimeMs;
    }
    if (newIncrementSeconds !== undefined) {
      this.incrementMs = newIncrementSeconds * 1000;
    }
    this.remainingMs = this.startTimeMs;
    this.emitTick();
  }

  setRemainingTime(ms: number): void {
    this.remainingMs = ms;
    this.emitTick();
  }

  getRemainingTime(): number {
    return this.remainingMs;
  }

  isRunning(): boolean {
    return this.running;
  }

  getDisplayText(): string {
    return formatTime(this.remainingMs);
  }

  isWarning(): boolean {
    return this.remainingMs <= WARN_THRESHOLD_MS;
  }
}
