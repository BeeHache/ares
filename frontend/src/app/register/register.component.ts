import { Component, OnInit } from '@angular/core';
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
  private _email = '';
  private _password = '';
  private _confirmPassword = '';
  errorMessages: string[] = [];

  constructor(private http: HttpClient, private router: Router) {}

  ngOnInit(): void {
    this.validateInputs(); // Initial validation to set button state
  }

  get email(): string { return this._email; }
  set email(value: string) {
    this._email = value;
    this.validateInputs();
  }

  get password(): string { return this._password; }
  set password(value: string) {
    this._password = value;
    this.validateInputs();
  }

  get confirmPassword(): string { return this._confirmPassword; }
  set confirmPassword(value: string) {
    this._confirmPassword = value;
    this.validateInputs();
  }

  private validateInputs(): boolean {
    this.errorMessages = [];

    // Email validation
    if (!this._email) {
      this.errorMessages.push('Email is required.');
    } else if (!/^[a-zA-Z0-9_+&*-]+(?:\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\.)+[a-zA-Z]{2,7}$/.test(this._email)) {
      this.errorMessages.push('Invalid email format.');
    }

    // Password validation
    if (!this._password) {
      this.errorMessages.push('Password is required.');
    } else {
      if (this._password.length < 8) {
        this.errorMessages.push('Password must be at least 8 characters long.');
      }
      if (!/[A-Z]/.test(this._password)) {
        this.errorMessages.push('Password must contain at least one uppercase letter.');
      }
      if (!/[a-z]/.test(this._password)) {
        this.errorMessages.push('Password must contain at least one lowercase letter.');
      }
      if (!/\d/.test(this._password)) {
        this.errorMessages.push('Password must contain at least one digit.');
      }
      if (!/[@$!%*?&]/.test(this._password)) {
        this.errorMessages.push('Password must contain at least one special character.');
      }
    }

    // Confirm Password validation
    if (this._password !== this._confirmPassword) {
      this.errorMessages.push('Passwords do not match.');
    }

    return this.errorMessages.length === 0;
  }

  onSubmit() {
    if (!this.validateInputs()) {
      return; // Stop if client-side validation fails
    }

    const registerData = { email: this._email, password: this._password };
    this.http.post(`${environment.apiUrl}/register`, registerData).subscribe({
      next: () => {
        this.router.navigate(['/login']);
      },
      error: (err) => {
        console.error('Registration error:', err);
        if (err.error && typeof err.error === 'object' && err.error.message) {
          this.errorMessages.push(err.error.message);
        } else if (typeof err.error === 'string') {
          this.errorMessages.push(err.error);
        } else {
          this.errorMessages.push('An unexpected error occurred during registration.');
        }
      }
    });
  }

  onCancel() {
    this.router.navigate(['/']);
  }
}
