import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
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
  feed: Feed | null = null;
  items: FeedItem[] = [];
  filteredItems: FeedItem[] = [];
  loading = false;
  loadingMore = false;
  error = '';
  searchQuery = '';
  currentPage = 0;
  hasMore = true;
  visibleItems = new Set<string>(); // Track visible items by link (as ID)

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private feedService: FeedService,
    private cdr: ChangeDetectorRef
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
    this.loading = true;
    this.error = '';
    this.items = [];
    this.currentPage = 0;
    this.hasMore = true;
    this.visibleItems.clear();

    // Load Feed Metadata
    this.feedService.getFeedById(id).subscribe({
      next: (feed) => {
        this.feed = feed;
        // Don't use feed.items from here, fetch them separately
        this.loadItems(id, 0);
      },
      error: (err) => {
        console.error('Error loading feed:', err);
        this.error = 'Failed to load feed.';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  loadItems(feedId: string, page: number) {
      if (page > 0) this.loadingMore = true;

      this.feedService.getFeedItems(feedId, page).subscribe({
          next: (newItems) => {
              if (newItems.length === 0) {
                  this.hasMore = false;
              } else {
                  this.items = [...this.items, ...newItems];
                  this.filterItems();
              }
              this.loading = false;
              this.loadingMore = false;
              this.cdr.detectChanges();
          },
          error: (err) => {
              console.error('Error loading items:', err);
              this.loading = false;
              this.loadingMore = false;
          }
      });
  }

  loadMore() {
      if (this.feed && !this.loadingMore && this.hasMore) {
          this.currentPage++;
          this.loadItems(this.feed.id, this.currentPage);
      }
  }

  filterItems() {
    if (!this.searchQuery) {
      this.filteredItems = this.items;
    } else {
      const lowerCaseQuery = this.searchQuery.toLowerCase();
      this.filteredItems = this.items.filter(item =>
        (item.title && item.title.toLowerCase().includes(lowerCaseQuery)) ||
        (item.description && item.description.toLowerCase().includes(lowerCaseQuery))
      );
    }
  }

  onItemVisible(item: FeedItem) {
      this.visibleItems.add(item.link);
  }

  isItemVisible(item: FeedItem): boolean {
      return this.visibleItems.has(item.link);
  }

  deleteFeed() {
      if (!this.feed) return;

      if (confirm(`Are you sure you want to unsubscribe from ${this.feed.title}?`)) {
          this.feedService.deleteFeed(this.feed.id).subscribe({
              next: () => {
                  this.feedService.selectFeed(null); // Clear selection
                  window.parent.postMessage('feedDeleted', '*');
              },
              error: (err) => alert('Failed to unsubscribe')
          });
      }
  }
}
