import { Component, OnInit, Inject, PLATFORM_ID, NgZone, ChangeDetectorRef, signal } from '@angular/core';
import { isPlatformBrowser, CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule, ActivatedRoute } from '@angular/router';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { AuthService } from '../auth.service';
import { PasswordInputComponent } from '../shared/password-input/password-input.component';
import { environment } from '../../environments/environment';
import { jwtDecode } from 'jwt-decode';

interface DecodedToken {
  roles?: string[];
  [key: string]: any;
}

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, HttpClientModule, RouterModule, PasswordInputComponent],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent implements OnInit {
  email = '';
  password = '';
  errorMessage = '';
  returnUrl: string | null = null;
  githubLoginUrl = `${environment.apiUrl.replace('/api', '')}/oauth2/authorization/github`;
  googleLoginUrl = `${environment.apiUrl.replace('/api', '')}/oauth2/authorization/google`;
  facebookLoginUrl = `${environment.apiUrl.replace('/api', '')}/oauth2/authorization/facebook`;
  appleLoginUrl = `${environment.apiUrl.replace('/api', '')}/oauth2/authorization/apple`;
  microsoftLoginUrl = `${environment.apiUrl.replace('/api', '')}/oauth2/authorization/microsoft`;

  // Signal for feature flags
  features = signal<{ [key: string]: boolean }>({});

  constructor(
    private http: HttpClient,
    private router: Router,
    private route: ActivatedRoute,
    private authService: AuthService,
    private ngZone: NgZone,
    private cdr: ChangeDetectorRef,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {
    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || null;
  }

  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      this.loadFeatures();
    }
  }

  loadFeatures() {
    this.http.get<{ [key: string]: boolean }>(`${environment.apiUrl}/features`).subscribe({
      next: (data) => {
        this.features.set(data);
      },
      error: (err) => console.error('Failed to load features', err)
    });
  }

  onSubmit() {
    this.errorMessage = '';

    if (isPlatformBrowser(this.platformId)) {
      const loginData = { username: this.email, password: this.password };
      this.http.post<any>(`${environment.apiUrl}/login`, loginData).subscribe({
        next: (response) => {
          if (response.token) {
              this.authService.login(response.token);
              this.ngZone.run(() => {
                this.redirectUser(response.token);
              });
          }
        },
        error: (err) => {
          this.ngZone.run(() => {
            console.error('Login error full object:', err);
            let errorBody = err.error;
            if (typeof errorBody === 'string') {
                try { errorBody = JSON.parse(errorBody); } catch (e) {}
            }

            if (errorBody && typeof errorBody === 'object' && errorBody.message) {
              this.errorMessage = errorBody.message;
            } else if (typeof errorBody === 'string') {
              this.errorMessage = errorBody;
            } else if (err.status === 401) {
              this.errorMessage = 'Invalid email or password.';
            } else {
              this.errorMessage = 'An unexpected error occurred.';
            }
            this.cdr.detectChanges(); // Still needed for non-signal errorMessage
          });
        }
      });
    }
  }

  redirectUser(token: string) {
    if (this.returnUrl) {
      this.router.navigateByUrl(this.returnUrl);
      return;
    }

    try {
      const decodedToken: DecodedToken = jwtDecode(token);
      const roles = decodedToken.roles || [];

      if (roles.includes('ROLE_ADMIN')) {
        this.router.navigate(['/admin']);
      } else if (roles.includes('ROLE_USER')) {
        this.router.navigate(['/feeds']);
      }
    } catch (error) {
      this.router.navigate(['/']);
    }
  }

  onCancel() {
    this.router.navigate(['/']);
  }
}
