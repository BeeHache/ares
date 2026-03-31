import { Component, signal } from '@angular/core';
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
  email = signal<string>('');
  message = signal<string>('');
  isError = signal<boolean>(false);
  loading = signal<boolean>(false);

  constructor(private http: HttpClient) {}

  onSubmit() {
    this.message.set('');
    this.isError.set(false);
    this.loading.set(true);

    this.http.post(`${environment.apiUrl}/login/recover`, { email: this.email() }).subscribe({
      next: () => {
        this.message.set('If an account with that email exists, a recovery link has been sent.');
        this.loading.set(false);
      },
      error: (err) => {
        // For security, show the same message on error
        this.message.set('If an account with that email exists, a recovery link has been sent.');
        console.error('Password recovery error:', err);
        this.loading.set(false);
      }
    });
  }
}
