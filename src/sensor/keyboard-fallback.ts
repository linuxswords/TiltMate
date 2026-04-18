export type KeyAction = 'tilt-left' | 'tilt-right' | 'tilt-center' | 'tap' | 'reset';

export type KeyCallback = (action: KeyAction) => void;

export class KeyboardFallback {
  private callback: KeyCallback | null = null;
  private currentKey: string | null = null;

  setCallback(cb: KeyCallback): void {
    this.callback = cb;
  }

  start(): void {
    window.addEventListener('keydown', this.handleKeyDown);
    window.addEventListener('keyup', this.handleKeyUp);
  }

  stop(): void {
    window.removeEventListener('keydown', this.handleKeyDown);
    window.removeEventListener('keyup', this.handleKeyUp);
  }

  private handleKeyDown = (e: KeyboardEvent): void => {
    if (e.repeat) return;

    switch (e.key) {
      case 'ArrowLeft':
        e.preventDefault();
        this.currentKey = 'ArrowLeft';
        this.callback?.('tilt-left');
        break;
      case 'ArrowRight':
        e.preventDefault();
        this.currentKey = 'ArrowRight';
        this.callback?.('tilt-right');
        break;
      case ' ':
        e.preventDefault();
        this.callback?.('tap');
        break;
      case 'r':
      case 'R':
        this.callback?.('reset');
        break;
    }
  };

  private handleKeyUp = (e: KeyboardEvent): void => {
    if (e.key === 'ArrowLeft' || e.key === 'ArrowRight') {
      if (this.currentKey === e.key) {
        this.currentKey = null;
        this.callback?.('tilt-center');
      }
    }
  };
}
