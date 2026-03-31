import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Feed, FeedItem, FeedService } from '../feed.service';
import { LazyLoadDirective } from '../shared/lazy-load.directive';

@Component({
  selector: 'app-feed-items',
  standalone: true,
  imports: [CommonModule, FormsModule, LazyLoadDirective],
  templateUrl: './feed-items.component.html',
  styleUrl: './feed-items.component.css'
})
export class FeedItemsComponent implements OnInit {
  // Signals
  feed = signal<Feed | null>(null);
  items = signal<FeedItem[]>([]);
  loading = signal<boolean>(false);
  loadingMore = signal<boolean>(false);
  error = signal<string>('');
  searchQuery = signal<string>('');
  currentPage = signal<number>(0);
  hasMore = signal<boolean>(true);

  // Computed signal for filtered items
  filteredItems = computed(() => {
    const query = this.searchQuery().toLowerCase();
    const allItems = this.items();

    if (!query) return allItems;

    return allItems.filter(item =>
      (item.title && item.title.toLowerCase().includes(query)) ||
      (item.description && item.description.toLowerCase().includes(query))
    );
  });

  visibleItems = new Set<string>(); // Keep as set for performance, but track in UI via logic

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private feedService: FeedService
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const feedId = params.get('id');
      if (feedId) {
        this.loadFeed(feedId);
      }
    });
  }

  loadFeed(id: string) {
    this.loading.set(true);
    this.error.set('');
    this.items.set([]);
    this.currentPage.set(0);
    this.hasMore.set(true);
    this.visibleItems.clear();

    this.feedService.getFeedById(id).subscribe({
      next: (feed) => {
        this.feed.set(feed);
        this.loadItems(id, 0);
      },
      error: (err) => {
        console.error('Error loading feed:', err);
        this.error.set('Failed to load feed.');
        this.loading.set(false);
      }
    });
  }

  loadItems(feedId: string, page: number) {
      if (page > 0) this.loadingMore.set(true);

      this.feedService.getFeedItems(feedId, page).subscribe({
          next: (newItems) => {
              if (newItems.length === 0) {
                  this.hasMore.set(false);
              } else {
                  this.items.update(currentItems => [...currentItems, ...newItems]);
              }
              this.loading.set(false);
              this.loadingMore.set(false);
          },
          error: (err) => {
              console.error('Error loading items:', err);
              this.loading.set(false);
              this.loadingMore.set(false);
          }
      });
  }

  loadMore() {
      const currentFeed = this.feed();
      if (currentFeed && !this.loadingMore() && this.hasMore()) {
          this.currentPage.update(p => p + 1);
          this.loadItems(currentFeed.id, this.currentPage());
      }
  }

  onSearchChange(event: Event) {
    const input = event.target as HTMLInputElement;
    this.searchQuery.set(input.value);
  }

  onItemVisible(item: FeedItem) {
      this.visibleItems.add(item.link);
  }

  isItemVisible(item: FeedItem): boolean {
      return this.visibleItems.has(item.link);
  }

  deleteFeed() {
      const currentFeed = this.feed();
      if (!currentFeed) return;

      if (confirm(`Are you sure you want to unsubscribe from ${currentFeed.title}?`)) {
          this.feedService.deleteFeed(currentFeed.id).subscribe({
              next: () => {
                  this.feedService.selectFeed(null);
                  window.parent.postMessage('feedDeleted', '*');
              },
              error: (err) => alert('Failed to unsubscribe')
          });
      }
  }
}
