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
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
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
