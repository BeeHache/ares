import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { EmailInputComponent } from '../shared/email-input/email-input.component';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, FormsModule, HttpClientModule, EmailInputComponent],
  templateUrl: './forgot-password.component.html',
  styleUrl: './forgot-password.component.css'
})
export class ForgotPasswordComponent {
  email = '';
  message = '';
  isError = false;

  constructor(private http: HttpClient) {}

  onSubmit() {
    this.message = '';
    this.isError = false;

    this.http.post(`${environment.apiUrl}/login/recover`, { email: this.email }).subscribe({
      next: () => {
        this.message = 'If an account with that email exists, a recovery link has been sent.';
      },
      error: (err) => {
        // For security, show the same message on error
        this.message = 'If an account with that email exists, a recovery link has been sent.';
        console.error('Password recovery error:', err);
      }
    });
  }
}
