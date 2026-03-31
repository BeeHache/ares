import { Component, input, signal, forwardRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ControlValueAccessor, NG_VALUE_ACCESSOR, FormsModule } from '@angular/forms';

@Component({
  selector: 'app-email-input',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './email-input.component.html',
  styleUrl: './email-input.component.css',
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => EmailInputComponent),
      multi: true
    }
  ]
})
export class EmailInputComponent implements ControlValueAccessor {
  // Signal-based inputs
  label = input<string>('Email');
  id = input<string>('email');
  name = input<string>('email');
  required = input<boolean>(false);
  initialEmail = input<string>('');
  editable = input<boolean>(true);

  // Internal state signals
  value = signal<string>('');
  isDisabled = signal<boolean>(false);

  // ControlValueAccessor callbacks
  onChange: any = () => {};
  onTouched: any = () => {};

  writeValue(value: string): void {
    const finalValue = !this.editable() && this.initialEmail() ? this.initialEmail() : value;
    this.value.set(finalValue || '');
  }

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.isDisabled.set(isDisabled || !this.editable());
  }

  onInputChange(event: Event) {
    if (this.editable()) {
      const val = (event.target as HTMLInputElement).value;
      this.value.set(val);
      this.onChange(val);
      this.onTouched();
    }
  }
}
