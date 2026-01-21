import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { FeedListComponent } from '../feed-list/feed-list.component';

interface UserProfile {
  email: string;
  feeds: any[];
}

@Component({
  selector: 'app-user',
  standalone: true,
  imports: [CommonModule, FeedListComponent],
  templateUrl: './user.component.html',
  styleUrl: './user.component.css'
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
