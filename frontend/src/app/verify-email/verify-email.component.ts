import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { HttpClient, HttpClientModule } from '@angular/common/http';

@Component({
  selector: 'app-verify-email',
  standalone: true,
  imports: [CommonModule, HttpClientModule, RouterModule],
  templateUrl: './verify-email.component.html',
  styleUrl: './verify-email.component.css'
})
export class VerifyEmailComponent implements OnInit {
  status: 'loading' | 'success' | 'error' = 'loading';
  message = 'Verifying your email...';

  constructor(
    private route: ActivatedRoute,
    private http: HttpClient,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    const code = this.route.snapshot.paramMap.get('code');
    if (code) {
      this.verifyEmail(code);
    } else {
      this.status = 'error';
      this.message = 'Invalid verification link.';
    }
  }

  verifyEmail(code: string) {
    this.http.get(`http://localhost:8080/api/register/confirm/${code}`, { responseType: 'text' })
      .subscribe({
        next: (response) => {
          this.status = 'success';
          this.message = 'Email verified successfully! You can now login.';
          this.cdr.detectChanges();
        },
        error: (err) => {
          this.status = 'error';
          this.message = err.error || 'Verification failed. The link may be invalid or expired.';
          this.cdr.detectChanges();
        }
      });
  }
}
