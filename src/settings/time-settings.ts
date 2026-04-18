export interface TimeSettings {
  readonly minutes: number;
  readonly increment: number;
  readonly label: string;
  readonly name: string;
}

// FIDE presets
export const PRESETS: readonly TimeSettings[] = [
  { minutes: 3, increment: 0, label: '3+0', name: 'THREE_PLUS_ZERO' },
  { minutes: 3, increment: 2, label: '3+2', name: 'THREE_PLUS_TWO' },
  { minutes: 5, increment: 0, label: '5+0', name: 'FIVE_PLUS_ZERO' },
  { minutes: 5, increment: 3, label: '5+3', name: 'FIVE_PLUS_THREE' },
  { minutes: 10, increment: 0, label: '10+0', name: 'TEN_PLUS_ZERO' },
  { minutes: 10, increment: 5, label: '10+5', name: 'TEN_PLUS_FIVE' },
  { minutes: 15, increment: 10, label: '15+10', name: 'FIFTEEN_PLUS_TEN' },
] as const;

export const DEFAULT_PRESET = PRESETS[5]; // 10+5

export function createCustom(minutes: number, increment: number): TimeSettings {
  if (minutes < 1 || minutes > 180) throw new RangeError('Minutes must be between 1 and 180');
  if (increment < 0 || increment > 60) throw new RangeError('Increment must be between 0 and 60');
  return { minutes, increment, label: `${minutes}+${increment}`, name: 'CUSTOM' };
}

export function minutesAsMs(ts: TimeSettings): number {
  return ts.minutes * 60 * 1000;
}

export function isCustom(ts: TimeSettings): boolean {
  return ts.name === 'CUSTOM';
}
