import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

interface Feed {
  id: string;
  title: string;
  url: string;
  link: string;
  subscribers: number;
  pubdate: string;
}

interface Page<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  number: number;
  size: number;
}

@Component({
  selector: 'app-feed-management',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './feed-management.component.html',
  styleUrl: './feed-management.component.css'
})
export class FeedManagementComponent implements OnInit {
  feeds: Feed[] = [];
  currentPage = 0;
  totalPages = 0;
  pageSize = 20;
  loading = false;

  constructor(
    private http: HttpClient,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadFeeds(0);
  }

  loadFeeds(page: number) {
    this.loading = true;
    this.http.get<Page<Feed>>(`${environment.apiUrl}/admin/feeds?page=${page}&size=${this.pageSize}`)
      .subscribe({
        next: (data) => {
          this.feeds = data.content;
          this.currentPage = data.number;
          this.totalPages = data.totalPages;
          this.loading = false;
          this.cdr.detectChanges(); // Force update
        },
        error: (err) => {
          console.error('Error loading feeds', err);
          this.loading = false;
          this.cdr.detectChanges();
        }
      });
  }

  deleteFeed(id: string, title: string) {
    if (confirm(`Are you sure you want to delete feed "${title}"? This will remove it for ALL users.`)) {
      this.http.delete(`${environment.apiUrl}/admin/feeds/${id}`)
        .subscribe({
          next: () => this.loadFeeds(this.currentPage),
          error: (err) => alert('Failed to delete feed')
        });
    }
  }

  refreshFeed(id: string) {
    this.http.post(`${environment.apiUrl}/admin/feeds/${id}/refresh`, {})
      .subscribe({
        next: () => {
          alert('Feed refresh triggered.');
          this.loadFeeds(this.currentPage);
        },
        error: (err) => alert('Failed to refresh feed')
      });
  }
}
