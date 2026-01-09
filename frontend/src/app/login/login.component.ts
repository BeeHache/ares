import { Component, OnInit, Inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser, CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule, ActivatedRoute } from '@angular/router';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { AuthService } from '../auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, HttpClientModule, RouterModule],
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
        <div class="button-group">
          <button type="submit">Login</button>
          <button type="button" class="cancel-btn" (click)="onCancel()">Cancel</button>
        </div>
      </form>
      <p *ngIf="errorMessage" class="error">{{ errorMessage }}</p>
      <p class="register-link">
        Don't have an account? <a routerLink="/register">Register here</a>
      </p>
    </div>
  `,
  styles: [`
    .container { max-width: 400px; margin: 50px auto; padding: 20px; border: 1px solid #ccc; border-radius: 5px; }
    .form-group { margin-bottom: 15px; }
    label { display: block; margin-bottom: 5px; }
    input { width: 100%; padding: 8px; box-sizing: border-box; }
    .button-group { display: flex; gap: 10px; }
    button { flex: 1; padding: 10px; background-color: #28a745; color: white; border: none; border-radius: 5px; cursor: pointer; }
    button:hover { background-color: #218838; }
    .cancel-btn { background-color: #6c757d; }
    .cancel-btn:hover { background-color: #5a6268; }
    .error { color: red; margin-top: 10px; }
    .register-link { text-align: center; margin-top: 20px; }
  `]
})
export class LoginComponent implements OnInit {
  email = '';
  password = '';
  errorMessage = '';
  returnUrl: string;

  constructor(
    private http: HttpClient,
    private router: Router,
    private route: ActivatedRoute,
    private authService: AuthService,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {
    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/';
  }

  ngOnInit(): void {}

  onSubmit() {
    if (isPlatformBrowser(this.platformId)) {
      const loginData = { email: this.email, password: this.password };
      this.http.post<any>('http://localhost:8080/api/login', loginData).subscribe({
        next: (response) => {
          if (response.token) {
              this.authService.login(response.token);
              this.router.navigate([this.returnUrl]);
          }
        },
        error: (err) => {
          this.errorMessage = err.error?.message || 'An unexpected error occurred.';
          console.error('Login error:', err);
        }
      });
    }
  }

  onCancel() {
    this.router.navigate(['/']);
  }
}
