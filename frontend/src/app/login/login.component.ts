import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient, HttpClientModule } from '@angular/common/http';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, HttpClientModule],
  template: `
    <div class="container">
      <h2>Login</h2>
      <form (ngSubmit)="onSubmit()">
        <div class="form-group">
          <label for="email">Email:</label>
          <input type="email" id="email" [(ngModel)]="email" name="email" required>
        </div>
        <div class="form-group">
          <label for="password">Password:</label>
          <input type="password" id="password" [(ngModel)]="password" name="password" required>
        </div>
        <button type="submit">Login</button>
      </form>
      <p *ngIf="errorMessage" class="error">{{ errorMessage }}</p>
    </div>
  `,
  styles: [`
    .container { max-width: 400px; margin: 50px auto; padding: 20px; border: 1px solid #ccc; border-radius: 5px; }
    .form-group { margin-bottom: 15px; }
    label { display: block; margin-bottom: 5px; }
    input { width: 100%; padding: 8px; box-sizing: border-box; }
    button { width: 100%; padding: 10px; background-color: #28a745; color: white; border: none; border-radius: 5px; cursor: pointer; }
    button:hover { background-color: #218838; }
    .error { color: red; margin-top: 10px; }
  `]
})
export class LoginComponent {
  email = '';
  password = '';
  errorMessage = '';

  constructor(private http: HttpClient, private router: Router) {}

  onSubmit() {
    const loginData = { email: this.email, password: this.password };
    this.http.post<any>('http://localhost:8080/api/login', loginData).subscribe({
      next: (response) => {
        // Assuming the response contains the token
        if (response.token) {
            localStorage.setItem('token', response.token);
            this.router.navigate(['/']);
        }
      },
      error: (error) => {
        this.errorMessage = 'Invalid email or password';
        console.error('Login error:', error);
      }
    });
  }
}
