import * as prefs from '../../settings/preferences';
import iconImg from '../../../assets/images/tiltmate-icon-512.webp';
import heroImg from '../../../assets/doc/hero-image.webp';

const TABLE_MODE_SVG = `
<svg viewBox="0 0 220 140" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
  <rect x="20" y="20" width="180" height="100" rx="12" fill="#1f1f1f" stroke="#555" stroke-width="2"/>
  <line x1="110" y1="28" x2="110" y2="112" stroke="#555" stroke-width="1" stroke-dasharray="3 3"/>
  <circle cx="65" cy="70" r="10" fill="none" stroke="#4fc3f7" stroke-width="2" opacity="0.5"/>
  <circle cx="65" cy="70" r="16" fill="none" stroke="#4fc3f7" stroke-width="2" opacity="0.25"/>
  <circle cx="65" cy="70" r="4" fill="#4fc3f7"/>
  <text x="65" y="105" font-family="sans-serif" font-size="11" fill="#aaa" text-anchor="middle">tap</text>
  <text x="155" y="105" font-family="sans-serif" font-size="11" fill="#aaa" text-anchor="middle">tap</text>
</svg>
`.trim();

interface Panel {
  title: string;
  body: string;
  cta: string;
  visual: string;
}

const PANELS: Panel[] = [
  {
    title: 'Welcome to TiltMate',
    body: 'A tilt-based chess clock. Two ways to play — pick whichever suits your board.',
    cta: 'Next',
    visual: `<img src="${iconImg}" alt="" class="intro-image intro-image-icon">`,
  },
  {
    title: 'Seesaw mode',
    body: 'Stand your phone upright on a 3D-printed seesaw base. Tilt left or right to switch clocks. The phone stays upright — never lay it flat for seesaw mode.',
    cta: 'Next',
    visual: `<img src="${heroImg}" alt="" class="intro-image">`,
  },
  {
    title: 'Table mode',
    body: 'No seesaw? Lay the phone flat or hold it in portrait. Tap your side of the screen to pass the clock — just like a regular chess clock.',
    cta: 'Got it',
    visual: `<div class="intro-image intro-image-svg">${TABLE_MODE_SVG}</div>`,
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
          <div class="intro-visual">${panel.visual}</div>
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
    this.el.querySelector('#introCta')!.addEventListener('click', () => this.next());

    const panel = this.el.querySelector('.intro-panel') as HTMLElement;
    let startX = 0;
    let startY = 0;
    let tracking = false;
    panel.addEventListener('pointerdown', (e) => {
      tracking = true;
      startX = e.clientX;
      startY = e.clientY;
    });
    panel.addEventListener('pointerup', (e) => {
      if (!tracking) return;
      tracking = false;
      const dx = e.clientX - startX;
      const dy = e.clientY - startY;
      if (Math.abs(dx) > 50 && Math.abs(dx) > Math.abs(dy)) {
        if (dx < 0) this.next();
        else this.prev();
      }
    });
    panel.addEventListener('pointercancel', () => { tracking = false; });
  }

  private next(): void {
    if (this.index < PANELS.length - 1) {
      this.index++;
      this.el.innerHTML = this.render();
      this.bindEvents();
    } else {
      this.finish();
    }
  }

  private prev(): void {
    if (this.index > 0) {
      this.index--;
      this.el.innerHTML = this.render();
      this.bindEvents();
    }
  }

  private finish(): void {
    prefs.setIntroSeen(true);
    this.el.remove();
    this.onClose();
  }
}
