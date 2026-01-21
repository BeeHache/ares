import { Component, OnInit, Inject, PLATFORM_ID, NgZone, ChangeDetectorRef } from '@angular/core';
import { isPlatformBrowser, CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule, ActivatedRoute } from '@angular/router';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { AuthService } from '../auth.service';
import { PasswordInputComponent } from '../shared/password-input/password-input.component';

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
  returnUrl: string;

  constructor(
    private http: HttpClient,
    private router: Router,
    private route: ActivatedRoute,
    private authService: AuthService,
    private ngZone: NgZone,
    private cdr: ChangeDetectorRef,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {
    // If a returnUrl is provided, use it. Otherwise, default to the '/feeds' page.
    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/feeds';
  }

  ngOnInit(): void {}

  onSubmit() {
    this.errorMessage = ''; // Clear previous error

    if (isPlatformBrowser(this.platformId)) {
      const loginData = { email: this.email, password: this.password };
      this.http.post<any>('http://localhost:8080/api/login', loginData).subscribe({
        next: (response) => {
          if (response.token) {
              this.authService.login(response.token);
              this.ngZone.run(() => {
                this.router.navigateByUrl(this.returnUrl);
              });
          }
        },
        error: (err) => {
          this.ngZone.run(() => {
            console.error('Login error full object:', err);
            console.error('Login error body:', err.error);

            let errorBody = err.error;
            if (typeof errorBody === 'string') {
                try {
                    errorBody = JSON.parse(errorBody);
                } catch (e) {
                    // Not JSON
                }
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

            console.log('Setting errorMessage to:', this.errorMessage);
            this.cdr.detectChanges(); // Force update
          });
        }
      });
    }
  }

  onCancel() {
    this.router.navigate(['/']);
  }
}
