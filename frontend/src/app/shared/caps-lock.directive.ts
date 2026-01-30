import { Directive, Output, EventEmitter, HostListener } from '@angular/core';

@Directive({
  selector: '[appCapsLock]',
  standalone: true
})
export class CapsLockDirective {
  @Output() appCapsLock = new EventEmitter<boolean>();

  @HostListener('window:keydown', ['$event'])
  @HostListener('window:keyup', ['$event'])
  @HostListener('window:click', ['$event'])
  @HostListener('window:mousedown', ['$event'])
  checkCapsLock(event: KeyboardEvent | MouseEvent): void {
    if (event.getModifierState) {
      const isCapsLockOn = event.getModifierState('CapsLock');
      this.appCapsLock.emit(isCapsLockOn);
    }
  }
}
