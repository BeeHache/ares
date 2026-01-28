import { Component, Input, forwardRef } from '@angular/core';
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
  @Input() label: string = 'Email';
  @Input() id: string = 'email';
  @Input() name: string = 'email';
  @Input() required: boolean = false;
  @Input() initialEmail: string = ''; // New input for initial email
  @Input() editable: boolean = true; // New input for editability, defaults to true

  value: string = '';
  disabled = false;

  // These are required for ControlValueAccessor
  onChange: any = () => {};
  onTouched: any = () => {};

  constructor() {
    // Initialize value with initialEmail if not editable
    if (!this.editable && this.initialEmail) {
      this.value = this.initialEmail;
    }
  }

  writeValue(value: string): void {
    // If not editable, prioritize initialEmail, otherwise use the form value
    this.value = !this.editable && this.initialEmail ? this.initialEmail : value;
  }

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    // Component is disabled if explicitly set or if not editable
    this.disabled = isDisabled || !this.editable;
  }

  onInput(event: Event) {
    // Only update value if editable
    if (this.editable) {
      const value = (event.target as HTMLInputElement).value;
      this.value = value;
      this.onChange(value);
      this.onTouched();
    }
  }
}
