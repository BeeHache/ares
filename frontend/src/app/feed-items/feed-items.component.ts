import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Feed, FeedItem, FeedService } from '../feed.service';

@Component({
  selector: 'app-feed-items',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './feed-items.component.html',
  styleUrl: './feed-items.component.css'
})
export class FeedItemsComponent implements OnInit {
  feed: Feed | null = null;
  items: FeedItem[] = [];
  filteredItems: FeedItem[] = [];
  loading = false;
  error = '';
  searchQuery = '';

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
    this.feedService.getFeedById(id).subscribe({
      next: (feed) => {
        this.feed = feed;
        this.items = feed.items || [];
        this.items.sort((a, b) => {
            const dateA = a.date ? new Date(a.date).getTime() : 0;
            const dateB = b.date ? new Date(b.date).getTime() : 0;
            return dateB - dateA;
        });
        this.filterItems(); // Initialize filtered list
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error loading feed:', err);
        this.error = 'Failed to load feed.';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
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
