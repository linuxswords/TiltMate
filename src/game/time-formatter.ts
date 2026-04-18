const LOW_TIME_THRESHOLD_MS = 10_000;

export function formatTime(ms: number): string {
  if (ms <= 0) return '0.0';

  if (ms < LOW_TIME_THRESHOLD_MS) {
    const seconds = Math.floor(ms / 1000);
    const tenths = Math.floor((ms % 1000) / 100);
    return `${seconds}.${tenths}`;
  }

  const minutes = Math.floor(ms / 60_000);
  const seconds = Math.floor((ms % 60_000) / 1000);
  return `${minutes}:${seconds.toString().padStart(2, '0')}`;
}
