import { Component, OnInit, ChangeDetectorRef, effect, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { AuthService } from '../auth.service';
import { FeedService, FeedSummary } from '../feed.service';
import { environment } from '../../environments/environment';
import { OpmlImportComponent } from '../opml-import/opml-import.component';

interface UserProfile {
  email: string;
}

@Component({
  selector: 'app-user',
  standalone: true,
  imports: [CommonModule, OpmlImportComponent],
  templateUrl: './user.component.html',
  styleUrl: './user.component.css'
})
export class UserComponent implements OnInit {
  user = signal<UserProfile | null>(null);
  feedSummaries = signal<FeedSummary[]>([]);
  error = signal<string | null>(null);
  showImportModal = false;
  showDeleteConfirm = false;
  showDeleteSuccess = false;

  constructor(
      private http: HttpClient,
      private authService: AuthService,
      private feedService: FeedService,
      private cdr: ChangeDetectorRef,
      private router: Router
  ) {
    console.log('UserComponent: Constructor called');

    // React to auth state changes using effect
    effect(() => {
      const currentUser = this.authService.currentUser();
      if (currentUser) {
        console.log('UserComponent: Auth confirmed via Signal, loading user profile...');
        this.loadUserProfile();
        this.loadFeedSummaries();
      }
    });
  }

  ngOnInit(): void {
    console.log('UserComponent: ngOnInit called');
  }

  loadUserProfile(): void {
    this.http.get<UserProfile>(`${environment.apiUrl}/user/`).subscribe({
      next: (data) => {
        console.log('UserComponent: User profile loaded');
        this.user.set(data);
      },
      error: (err) => {
        this.error.set(`Failed to load user data. Status: ${err.status}. Please try logging in again.`);
        console.error('User profile error:', err);
      }
    });
  }

  loadFeedSummaries(): void {
      this.feedService.getFeedSummaries().subscribe({
          next: (data) => {
              this.feedSummaries.set(data.sort((a, b) => a.title.localeCompare(b.title)));
          },
          error: (err) => console.error('Error loading feed summaries', err)
      });
  }

  addFeed() {
    const link = prompt('Enter RSS Feed URL:');
    if (link) {
      this.feedService.addFeed(link).subscribe({
        next: (newFeed) => {
          alert('Feed added successfully!');
          this.loadFeedSummaries(); // Refresh list
        },
        error: (err) => alert('Failed to add feed: ' + (err.error?.message || err.message))
      });
    }
  }

  deleteFeed(id: string, title: string) {
      if (confirm(`Are you sure you want to unsubscribe from ${title}?`)) {
          this.feedService.deleteFeed(id).subscribe({
              next: () => {
                  this.feedSummaries.update(feeds => feeds.filter(f => f.id !== id));
              },
              error: (err) => alert('Failed to unsubscribe')
          });
      }
  }

  openImportModal() {
      this.showImportModal = true;
  }

  closeImportModal() {
      this.showImportModal = false;
  }

  onImportSuccess() {
      alert('OPML Import started successfully.');
      this.closeImportModal();
      this.loadFeedSummaries(); // Refresh list
  }

  exportFeeds() {
    this.http.get(`${environment.apiUrl}/user/export`, { responseType: 'blob' }).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'ares-feeds.opml';
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);
      },
      error: (err) => {
        console.error('Export failed', err);
        alert('Failed to export feeds.');
      }
    });
  }

  confirmDelete() {
    this.showDeleteConfirm = true;
  }

  cancelDelete() {
    this.showDeleteConfirm = false;
  }

  deleteAccount() {
    this.http.delete(`${environment.apiUrl}/user/`).subscribe({
      next: () => {
        this.showDeleteConfirm = false;
        this.showDeleteSuccess = true;
      },
      error: (err) => {
        console.error('Delete account failed', err);
        alert('Failed to delete account. Please try again.');
        this.showDeleteConfirm = false;
      }
    });
  }

  onDeleteSuccess() {
    this.showDeleteSuccess = false;
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
