import { Component, input, signal, forwardRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ControlValueAccessor, NG_VALUE_ACCESSOR, FormsModule } from '@angular/forms';
import { CapsLockDirective } from '../caps-lock.directive';

@Component({
  selector: 'app-password-input',
  standalone: true,
  imports: [CommonModule, FormsModule, CapsLockDirective],
  templateUrl: './password-input.component.html',
  styleUrl: './password-input.component.css',
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => PasswordInputComponent),
      multi: true
    }
  ]
})
export class PasswordInputComponent implements ControlValueAccessor {
  // Signal-based inputs
  label = input<string>('Password');
  placeholder = input<string>('');
  required = input<boolean>(false);
  minlength = input<number | null>(null);
  name = input<string>('password');
  id = input<string>('password');

  // Internal state signals
  value = signal<string>('');
  showPassword = signal<boolean>(false);
  isDisabled = signal<boolean>(false);
  capsLockOn = signal<boolean>(false);

  onChange: any = () => {};
  onTouched: any = () => {};

  toggleVisibility() {
    this.showPassword.update(show => !show);
  }

  // ControlValueAccessor implementation
  writeValue(value: string): void {
    this.value.set(value || '');
  }

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.isDisabled.set(isDisabled);
  }

  onInputChange(event: Event) {
    const val = (event.target as HTMLInputElement).value;
    this.value.set(val);
    this.onChange(val);
    this.onTouched();
  }
}
