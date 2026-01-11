import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';

interface UserProfile {
  email: string;
  feeds: any[]; // You can create a more specific type for feeds later
}

@Component({
  selector: 'app-user',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="container" *ngIf="user; else loading">
      <h2>User Profile</h2>
      <p><strong>Email:</strong> {{ user.email }}</p>
      <p><strong>Number of Feeds:</strong> {{ user.feeds.length || 0 }}</p>
    </div>
    <ng-template #loading>
      <p>Loading user data...</p>
    </ng-template>
    <div *ngIf="error" class="error">
      <p>{{ error }}</p>
    </div>
  `,
  styles: [`
    .container { max-width: 600px; margin: 50px auto; padding: 20px; border: 1px solid #ccc; border-radius: 5px; }
    .error { color: red; margin-top: 10px; }
  `]
})
export class UserComponent implements OnInit {
  user: UserProfile | null = null;
  error: string | null = null;

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.http.get<UserProfile>('http://localhost:8080/api/user/').subscribe({
      next: (data) => {
        this.user = data;
      },
      error: (err) => {
        this.error = 'Failed to load user data. Please try logging in again.';
        console.error('User profile error:', err);
      }
    });
  }
}
