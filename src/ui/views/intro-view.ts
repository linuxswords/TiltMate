import * as prefs from '../../settings/preferences';

interface Panel {
  title: string;
  body: string;
  cta: string;
}

const PANELS: Panel[] = [
  {
    title: 'Welcome to TiltMate',
    body: 'A tilt-based chess clock. Two ways to play — pick whichever suits your board.',
    cta: 'Next',
  },
  {
    title: 'Seesaw mode',
    body: 'Stand your phone upright on a 3D-printed seesaw base. Tilt left or right to switch clocks. The phone stays upright — never lay it flat for seesaw mode.',
    cta: 'Next',
  },
  {
    title: 'Table mode',
    body: 'No seesaw? Lay the phone flat or hold it in portrait. Tap your side of the screen to pass the clock — just like a regular chess clock.',
    cta: 'Got it',
  },
];

export class IntroView {
  private el: HTMLElement;
  private onClose: () => void;
  private index = 0;

  constructor(container: HTMLElement, onClose: () => void) {
    this.onClose = onClose;
    this.el = document.createElement('div');
    this.el.className = 'settings-overlay intro-overlay';
    this.el.innerHTML = this.render();
    container.appendChild(this.el);
    this.bindEvents();
  }

  private render(): string {
    const panel = PANELS[this.index];
    const dots = PANELS.map(
      (_, i) => `<span class="intro-dot ${i === this.index ? 'active' : ''}"></span>`,
    ).join('');

    return `
      <div class="settings-panel intro-panel">
        <button class="intro-skip" id="introSkip">Skip</button>
        <div class="intro-content">
          <h2>${panel.title}</h2>
          <p>${panel.body}</p>
        </div>
        <div class="intro-footer">
          <div class="intro-dots">${dots}</div>
          <button class="intro-cta" id="introCta">${panel.cta}</button>
        </div>
      </div>
    `;
  }

  private bindEvents(): void {
    this.el.querySelector('#introSkip')!.addEventListener('click', () => this.finish());
    this.el.querySelector('#introCta')!.addEventListener('click', () => {
      if (this.index < PANELS.length - 1) {
        this.index++;
        this.el.innerHTML = this.render();
        this.bindEvents();
      } else {
        this.finish();
      }
    });
  }

  private finish(): void {
    prefs.setIntroSeen(true);
    this.el.remove();
    this.onClose();
  }
}
