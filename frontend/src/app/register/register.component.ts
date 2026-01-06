import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient, HttpClientModule } from '@angular/common/http';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, HttpClientModule],
  template: `
    <div class="container">
      <h2>Register</h2>
      <form (ngSubmit)="onSubmit()" #registerForm="ngForm">
        <div class="form-group">
          <label for="email">Email:</label>
          <input type="email" id="email" [(ngModel)]="email" name="email" required email #emailInput="ngModel">
          <div *ngIf="emailInput.invalid && (emailInput.dirty || emailInput.touched)" class="error-message">
            <div *ngIf="emailInput.errors?.['required']">Email is required.</div>
            <div *ngIf="emailInput.errors?.['email']">Invalid email format.</div>
          </div>
        </div>
        <div class="form-group">
          <label for="password">Password:</label>
          <input type="password" id="password" [(ngModel)]="password" name="password" required minlength="8" #passwordInput="ngModel">
          <div *ngIf="passwordInput.invalid && (passwordInput.dirty || passwordInput.touched)" class="error-message">
            <div *ngIf="passwordInput.errors?.['required']">Password is required.</div>
            <div *ngIf="passwordInput.errors?.['minlength']">Password must be at least 8 characters long.</div>
          </div>
        </div>
        <div class="form-group">
          <label for="confirmPassword">Confirm Password:</label>
          <input type="password" id="confirmPassword" [(ngModel)]="confirmPassword" name="confirmPassword" required>
        </div>
        <div class="button-group">
          <button type="submit" [disabled]="registerForm.invalid">Register</button>
          <button type="button" class="cancel-btn" (click)="onCancel()">Cancel</button>
        </div>
      </form>
      <p *ngIf="errorMessage" class="error">{{ errorMessage }}</p>
    </div>
  `,
  styles: [`
    .container { max-width: 400px; margin: 50px auto; padding: 20px; border: 1px solid #ccc; border-radius: 5px; }
    .form-group { margin-bottom: 15px; }
    label { display: block; margin-bottom: 5px; }
    input { width: 100%; padding: 8px; box-sizing: border-box; }
    input.ng-invalid.ng-touched { border-color: red; }
    .button-group { display: flex; gap: 10px; }
    button { flex: 1; padding: 10px; background-color: #007bff; color: white; border: none; border-radius: 5px; cursor: pointer; }
    button:hover { background-color: #0056b3; }
    button:disabled { background-color: #ccc; cursor: not-allowed; }
    .cancel-btn { background-color: #6c757d; }
    .cancel-btn:hover { background-color: #5a6268; }
    .error { color: red; margin-top: 10px; }
    .error-message { color: red; font-size: 0.8em; margin-top: 5px; }
  `]
})
export class RegisterComponent {
  email = '';
  password = '';
  confirmPassword = '';
  errorMessage = '';

  constructor(private http: HttpClient, private router: Router) {}

  onSubmit() {
    if (this.password !== this.confirmPassword) {
      this.errorMessage = 'Passwords do not match';
      return;
    }

    const registerData = { email: this.email, password: this.password };
    this.http.post('http://localhost:8080/api/register', registerData).subscribe({
      next: () => {
        this.router.navigate(['/login']);
      },
      error: (error) => {
        this.errorMessage = 'Registration failed. Please try again.';
        console.error('Registration error:', error);
      }
    });
  }

  onCancel() {
    this.router.navigate(['/']);
  }
}
