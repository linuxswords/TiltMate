export class TickingSoundManager {
  private ctx: AudioContext | null = null;
  private tickBuffer: AudioBuffer | null = null;
  private sourceNode: AudioBufferSourceNode | null = null;
  private gainNode: GainNode | null = null;
  private enabled = false;
  private playing = false;

  async init(): Promise<void> {
    this.ctx = new AudioContext();
    this.gainNode = this.ctx.createGain();
    this.gainNode.gain.value = 0.3;
    this.gainNode.connect(this.ctx.destination);

    try {
      const response = await fetch('/sounds/tick.wav');
      const buffer = await response.arrayBuffer();
      this.tickBuffer = await this.ctx.decodeAudioData(buffer);
    } catch {
      console.warn('Failed to load tick sound');
    }
  }

  setEnabled(enabled: boolean): void {
    this.enabled = enabled;
    if (!enabled && this.playing) {
      this.stop();
    }
  }

  start(): void {
    if (!this.enabled || this.playing || !this.tickBuffer || !this.ctx || !this.gainNode) return;

    // Resume context if suspended (browser autoplay policy)
    if (this.ctx.state === 'suspended') {
      this.ctx.resume();
    }

    this.playing = true;
    this.sourceNode = this.ctx.createBufferSource();
    this.sourceNode.buffer = this.tickBuffer;
    this.sourceNode.loop = true;
    this.sourceNode.connect(this.gainNode);
    this.sourceNode.start();
  }

  stop(): void {
    this.playing = false;
    if (this.sourceNode) {
      try {
        this.sourceNode.stop();
      } catch { /* already stopped */ }
      this.sourceNode.disconnect();
      this.sourceNode = null;
    }
  }

  release(): void {
    this.stop();
    if (this.ctx) {
      this.ctx.close();
      this.ctx = null;
    }
  }
}
