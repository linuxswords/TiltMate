const THRESHOLD_LOW = 12;
const THRESHOLD_MEDIUM = 6;
const THRESHOLD_HIGH = 3;
const ALPHA = 0.8;

export type TiltCallback = (degree: number) => void;

export class TiltSensor {
  private gravity: [number, number, number] | null = null;
  private threshold = THRESHOLD_MEDIUM;
  private callback: TiltCallback | null = null;
  private listening = false;
  private permissionGranted = false;
  private available = false;

  constructor() {
    this.available = 'DeviceMotionEvent' in window;
  }

  setCallback(cb: TiltCallback): void {
    this.callback = cb;
  }

  setSensitivity(level: number): void {
    switch (level) {
      case 0: this.threshold = THRESHOLD_LOW; break;
      case 1: this.threshold = THRESHOLD_MEDIUM; break;
      case 2: this.threshold = THRESHOLD_HIGH; break;
      default: this.threshold = THRESHOLD_MEDIUM;
    }
  }

  isAvailable(): boolean {
    return this.available;
  }

  async requestPermission(): Promise<boolean> {
    // iOS 13+ requires explicit permission
    const DME = DeviceMotionEvent as unknown as {
      requestPermission?: () => Promise<string>;
    };
    if (typeof DME.requestPermission === 'function') {
      try {
        const result = await DME.requestPermission();
        this.permissionGranted = result === 'granted';
      } catch {
        this.permissionGranted = false;
      }
    } else {
      // Android / desktop Chrome — no permission needed
      this.permissionGranted = true;
    }
    return this.permissionGranted;
  }

  start(): void {
    if (this.listening || !this.available) return;
    window.addEventListener('devicemotion', this.handleMotion);
    this.listening = true;
  }

  stop(): void {
    if (!this.listening) return;
    window.removeEventListener('devicemotion', this.handleMotion);
    this.listening = false;
    this.gravity = null;
  }

  private handleMotion = (event: DeviceMotionEvent): void => {
    const ag = event.accelerationIncludingGravity;
    if (!ag || ag.x === null || ag.y === null || ag.z === null) return;

    const current: [number, number, number] = [ag.x, ag.y, ag.z];

    if (this.gravity === null) {
      this.gravity = [...current];
    } else {
      for (let i = 0; i < 3; i++) {
        this.gravity[i] = this.gravity[i] + ALPHA * (current[i] - this.gravity[i]);
      }
    }

    // Same calculation as Android: atan2(-Y, |X|)
    const tilt = Math.atan2(-this.gravity[1], Math.abs(this.gravity[0]));
    const tiltDeg = Math.round((tilt * 180) / Math.PI);

    if (Math.abs(tiltDeg) >= this.threshold) {
      this.callback?.(tiltDeg);
    } else {
      this.callback?.(0);
    }
  };
}
