const THRESHOLD_LOW = 12;
const THRESHOLD_MEDIUM = 6;
const THRESHOLD_HIGH = 3;
const ALPHA = 0.8;

const FLAT_Z_THRESHOLD = 6.5; // out of ~9.8; phone is flat if |z| exceeds this

export type TiltCallback = (degree: number) => void;
export type PostureCallback = (posture: 'flat' | 'upright', gravityX: number) => void;

// Generic Sensor API types (not in all TS libs)
interface AccelerometerReading {
  x: number | null;
  y: number | null;
  z: number | null;
}

interface GenericSensor extends EventTarget {
  start(): void;
  stop(): void;
  addEventListener(type: 'reading', listener: () => void): void;
  addEventListener(type: 'error', listener: (e: { error: DOMException }) => void): void;
}

type SensorBackend = 'devicemotion' | 'accelerometer' | 'none';

export class TiltSensor {
  private gravity: [number, number, number] | null = null;
  private threshold = THRESHOLD_MEDIUM;
  private callback: TiltCallback | null = null;
  private postureCallback: PostureCallback | null = null;
  private currentPosture: 'flat' | 'upright' = 'upright';
  private pendingPosture: 'flat' | 'upright' | null = null;
  private postureTimer = 0;
  private listening = false;
  private permissionGranted = false;
  private backend: SensorBackend = 'none';
  private accelerometer: (GenericSensor & AccelerometerReading) | null = null;

  constructor() {
    if ('DeviceMotionEvent' in window) {
      this.backend = 'devicemotion';
    } else if ('GravitySensor' in window || 'Accelerometer' in window) {
      this.backend = 'accelerometer';
    }
  }

  setCallback(cb: TiltCallback): void {
    this.callback = cb;
  }

  setPostureCallback(cb: PostureCallback): void {
    this.postureCallback = cb;
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
    return this.backend !== 'none';
  }

  async requestPermission(): Promise<boolean> {
    if (this.backend === 'devicemotion') {
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
        this.permissionGranted = true;
      }
    } else if (this.backend === 'accelerometer') {
      // Generic Sensor API uses Permissions API
      try {
        const result = await navigator.permissions.query({ name: 'accelerometer' as PermissionName });
        this.permissionGranted = result.state !== 'denied';
      } catch {
        // If permissions query fails, try anyway
        this.permissionGranted = true;
      }
    }
    return this.permissionGranted;
  }

  start(): void {
    if (this.listening || this.backend === 'none') return;

    if (this.backend === 'devicemotion') {
      window.addEventListener('devicemotion', this.handleMotion);
    } else if (this.backend === 'accelerometer') {
      this.startAccelerometer();
    }
    this.listening = true;
  }

  stop(): void {
    if (!this.listening) return;

    if (this.backend === 'devicemotion') {
      window.removeEventListener('devicemotion', this.handleMotion);
    } else if (this.accelerometer) {
      this.accelerometer.stop();
      this.accelerometer = null;
    }
    this.listening = false;
    this.gravity = null;
  }

  private startAccelerometer(): void {
    try {
      // Prefer GravitySensor (pre-filtered), fall back to Accelerometer
      const w = window as unknown as Record<string, unknown>;
      const SensorClass = w['GravitySensor'] ?? w['Accelerometer'];

      if (!SensorClass) return;

      const sensor = new (SensorClass as new (opts: { frequency: number }) => GenericSensor & AccelerometerReading)(
        { frequency: 60 }
      );

      sensor.addEventListener('reading', () => {
        if (sensor.x !== null && sensor.y !== null && sensor.z !== null) {
          this.processSample(sensor.x!, sensor.y!, sensor.z!);
        }
      });

      sensor.addEventListener('error', () => {
        this.backend = 'none';
      });

      sensor.start();
      this.accelerometer = sensor;
    } catch {
      this.backend = 'none';
    }
  }

  private processSample(x: number, y: number, z: number): void {
    const current: [number, number, number] = [x, y, z];

    if (this.gravity === null) {
      this.gravity = [...current];
    } else {
      for (let i = 0; i < 3; i++) {
        this.gravity[i] = this.gravity[i] + ALPHA * (current[i] - this.gravity[i]);
      }
    }

    // Posture: phone is flat when Z-axis gravity dominates (debounced)
    const absZ = Math.abs(this.gravity[2]);
    const detected: 'flat' | 'upright' = absZ > FLAT_Z_THRESHOLD ? 'flat' : 'upright';
    if (detected !== this.currentPosture) {
      if (this.pendingPosture !== detected) {
        this.pendingPosture = detected;
        clearTimeout(this.postureTimer);
        this.postureTimer = window.setTimeout(() => {
          this.currentPosture = detected;
          this.pendingPosture = null;
          this.postureCallback?.(detected, this.gravity![0]);
        }, 500);
      }
    } else {
      this.pendingPosture = null;
      clearTimeout(this.postureTimer);
    }

    const tilt = Math.atan2(-this.gravity[1], Math.abs(this.gravity[0]));
    const tiltDeg = Math.round((tilt * 180) / Math.PI);

    if (Math.abs(tiltDeg) >= this.threshold) {
      this.callback?.(tiltDeg);
    } else {
      this.callback?.(0);
    }
  }

  private handleMotion = (event: DeviceMotionEvent): void => {
    const ag = event.accelerationIncludingGravity;
    if (!ag || ag.x === null || ag.y === null || ag.z === null) return;
    this.processSample(ag.x, ag.y, ag.z);
  };
}
