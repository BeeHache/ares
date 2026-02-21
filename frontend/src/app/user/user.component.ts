import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../auth.service';
import { FeedService, FeedSummary } from '../feed.service';
import { filter, take } from 'rxjs/operators';
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
  user: UserProfile | null = null;
  feedSummaries: FeedSummary[] = [];
  error: string | null = null;
  showImportModal = false;

  constructor(
      private http: HttpClient,
      private authService: AuthService,
      private feedService: FeedService,
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
      this.loadFeedSummaries();
    });
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

  loadFeedSummaries(): void {
      this.feedService.getFeedSummaries().subscribe({
          next: (data) => {
              this.feedSummaries = data.sort((a, b) => a.title.localeCompare(b.title));
              this.cdr.detectChanges();
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
                  this.feedSummaries = this.feedSummaries.filter(f => f.id !== id);
                  this.cdr.detectChanges();
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
}
