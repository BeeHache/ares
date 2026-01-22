import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../auth.service';
import { filter, take } from 'rxjs/operators';
import { environment } from '../../environments/environment';

interface UserProfile {
  email: string;
  // Feeds will no longer be directly displayed here
}

@Component({
  selector: 'app-user',
  standalone: true,
  imports: [CommonModule], // Removed FeedListComponent, FeedItemsComponent
  templateUrl: './user.component.html',
  styleUrl: './user.component.css'
})
export class UserComponent implements OnInit {
  user: UserProfile | null = null;
  error: string | null = null;
  // selectedFeed: Feed | null = null; // Removed selectedFeed

  constructor(
      private http: HttpClient,
      private authService: AuthService,
      private cdr: ChangeDetectorRef
  ) {
    console.log('UserComponent: Constructor called');
  }

  ngOnInit(): void {
    console.log('UserComponent: ngOnInit called');

    this.authService.currentUser$.pipe(
      filter(user => user !== null),
      take(1)
    ).subscribe(() => {
      console.log('UserComponent: Auth confirmed, loading user profile...');
      this.loadUserProfile();
    });

    // Removed feedService subscription
  }

  loadUserProfile(): void {
    this.http.get<UserProfile>(`${environment.apiUrl}/user/`).subscribe({
      next: (data) => {
        console.log('UserComponent: User profile loaded');
        this.user = data;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.error = `Failed to load user data. Status: ${err.status}. Please try logging in again.`;
        console.error('User profile error:', err);
        this.cdr.detectChanges();
      }
    });
  }
}
