import { Component, OnInit, signal, HostListener } from '@angular/core';
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
  // Signals
  feeds = signal<Feed[]>([]);
  currentPage = signal<number>(0);
  pageSize = signal<number>(20);
  loading = signal<boolean>(false); // For initial load
  loadingMore = signal<boolean>(false); // For subsequent loads
  hasMore = signal<boolean>(true);

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.loadFeeds(this.currentPage());
  }

  @HostListener('window:scroll', ['$event'])
  onScroll(event: Event): void {
    if (this.loading() || this.loadingMore() || !this.hasMore()) {
      return;
    }

    const scrollHeight = document.documentElement.scrollHeight;
    const scrollTop = document.documentElement.scrollTop || document.body.scrollTop;
    const clientHeight = document.documentElement.clientHeight;

    if (scrollTop + clientHeight >= scrollHeight - 100) { // 100px from bottom
      this.loadMore();
    }
  }

  loadFeeds(page: number, append: boolean = false) {
    if (page === 0) {
      this.loading.set(true);
    } else {
      this.loadingMore.set(true);
    }

    this.http.get<Page<Feed>>(`${environment.apiUrl}/admin/feeds?page=${page}&size=${this.pageSize()}`)
      .subscribe({
        next: (data) => {
          if (append) {
            this.feeds.update(currentFeeds => [...currentFeeds, ...data.content]);
          } else {
            this.feeds.set(data.content);
          }
          this.currentPage.set(data.number);
          this.hasMore.set(data.number < data.totalPages - 1);
          this.loading.set(false);
          this.loadingMore.set(false);
        },
        error: (err) => {
          console.error('Error loading feeds', err);
          this.loading.set(false);
          this.loadingMore.set(false);
        }
      });
  }

  loadMore(): void {
    if (this.hasMore() && !this.loadingMore()) {
      this.loadFeeds(this.currentPage() + 1, true);
    }
  }

  deleteFeed(id: string, title: string) {
    if (confirm(`Are you sure you want to delete feed "${title}"? This will remove it for ALL users.`)) {
      this.http.delete(`${environment.apiUrl}/admin/feeds/${id}`)
        .subscribe({
          next: () => {
            // Reload current page to reflect deletion
            this.feeds.update(currentFeeds => currentFeeds.filter(feed => feed.id !== id));
            // If the current page becomes empty, load previous page or first page
            if (this.feeds().length === 0 && this.currentPage() > 0) {
                this.currentPage.update(p => p - 1);
            }
            this.loadFeeds(this.currentPage());
          },
          error: (err) => alert('Failed to delete feed')
        });
    }
  }

  refreshFeed(id: string) {
    this.http.post(`${environment.apiUrl}/admin/feeds/${id}/refresh`, {})
      .subscribe({
        next: () => {
          alert('Feed refresh triggered.');
          // Optionally refresh the specific feed in the list or reload current page
          this.loadFeeds(this.currentPage());
        },
        error: (err) => alert('Failed to refresh feed')
      });
  }
}
