import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { PasswordInputComponent } from '../shared/password-input/password-input.component';
import { EmailInputComponent } from '../shared/email-input/email-input.component';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, HttpClientModule, PasswordInputComponent, EmailInputComponent],
  templateUrl: './register.component.html',
  styleUrl: './register.component.css'
})
export class RegisterComponent implements OnInit {
  // Signals for form data
  email = signal<string>('');
  password = signal<string>('');
  confirmPassword = signal<string>('');

  // Signal for server errors
  serverErrors = signal<string[]>([]);

  // Computed signal for client-side validation errors
  clientErrors = computed(() => {
    const errors: string[] = [];
    const emailVal = this.email();
    const passVal = this.password();
    const confirmVal = this.confirmPassword();

    // Email validation
    if (emailVal && !/^[a-zA-Z0-9_+&*-]+(?:\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\.)+[a-zA-Z]{2,7}$/.test(emailVal)) {
      errors.push('Invalid email format.');
    }

    // Password validation
    if (passVal) {
      if (passVal.length < 8) {
        errors.push('Password must be at least 8 characters long.');
      }
      if (!/[A-Z]/.test(passVal)) {
        errors.push('Password must contain at least one uppercase letter.');
      }
      if (!/[a-z]/.test(passVal)) {
        errors.push('Password must contain at least one lowercase letter.');
      }
      if (!/\d/.test(passVal)) {
        errors.push('Password must contain at least one digit.');
      }
      if (!/[@$!%*?&]/.test(passVal)) {
        errors.push('Password must contain at least one special character.');
      }
    }

    // Confirm Password validation
    if (passVal && confirmVal && passVal !== confirmVal) {
      errors.push('Passwords do not match.');
    }

    return errors;
  });

  // Combined errors
  allErrors = computed(() => [...this.clientErrors(), ...this.serverErrors()]);

  constructor(private http: HttpClient, private router: Router) {}

  ngOnInit(): void {}

  onSubmit() {
    this.serverErrors.set([]); // Clear previous server errors

    if (this.clientErrors().length > 0 || !this.email() || !this.password()) {
      return;
    }

    const registerData = { email: this.email(), password: this.password() };
    this.http.post(`${environment.apiUrl}/register`, registerData).subscribe({
      next: () => {
        this.router.navigate(['/login']);
      },
      error: (err) => {
        console.error('Registration error:', err);
        let errorMsg = 'An unexpected error occurred during registration.';
        if (err.error && typeof err.error === 'object' && err.error.message) {
          errorMsg = err.error.message;
        } else if (typeof err.error === 'string') {
          errorMsg = err.error;
        }
        this.serverErrors.set([errorMsg]);
      }
    });
  }

  onCancel() {
    this.router.navigate(['/']);
  }
}
