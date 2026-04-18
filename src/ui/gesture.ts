const DOUBLE_CLICK_DELTA = 300;
const LONG_PRESS_DELAY = 600;

export class GestureDetector {
  private lastClickTime = 0;
  private longPressTimer = 0;
  private didLongPress = false;

  private element: HTMLElement;
  private handlers: {
    onSingleClick: () => void;
    onDoubleClick: () => void;
    onLongPress: () => void;
  };

  constructor(
    element: HTMLElement,
    handlers: {
      onSingleClick: () => void;
      onDoubleClick: () => void;
      onLongPress: () => void;
    },
  ) {
    this.element = element;
    this.handlers = handlers;
    this.element.addEventListener('pointerdown', this.onPointerDown);
    this.element.addEventListener('pointerup', this.onPointerUp);
    this.element.addEventListener('pointercancel', this.onPointerCancel);
    // Prevent context menu on long press
    this.element.addEventListener('contextmenu', (e) => e.preventDefault());
  }

  private onPointerDown = (_e: PointerEvent): void => {
    this.didLongPress = false;
    this.longPressTimer = window.setTimeout(() => {
      this.didLongPress = true;
      this.handlers.onLongPress();
    }, LONG_PRESS_DELAY);
  };

  private onPointerUp = (_e: PointerEvent): void => {
    clearTimeout(this.longPressTimer);
    if (this.didLongPress) return;

    const now = Date.now();
    if (now - this.lastClickTime < DOUBLE_CLICK_DELTA) {
      this.handlers.onDoubleClick();
      this.lastClickTime = 0;
    } else {
      this.handlers.onSingleClick();
    }
    this.lastClickTime = now;
  };

  private onPointerCancel = (): void => {
    clearTimeout(this.longPressTimer);
  };

  destroy(): void {
    this.element.removeEventListener('pointerdown', this.onPointerDown);
    this.element.removeEventListener('pointerup', this.onPointerUp);
    this.element.removeEventListener('pointercancel', this.onPointerCancel);
    clearTimeout(this.longPressTimer);
  }
}
