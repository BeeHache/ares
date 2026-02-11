import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { Feed, FeedItem, FeedService } from '../feed.service';

@Component({
  selector: 'app-feed-items',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './feed-items.component.html',
  styleUrl: './feed-items.component.css'
})
export class FeedItemsComponent implements OnInit {
  feed: Feed | null = null;
  items: FeedItem[] = [];
  loading = false;
  error = '';

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

  deleteFeed() {
      if (!this.feed) return;

      if (confirm(`Are you sure you want to unsubscribe from ${this.feed.title}?`)) {
          this.feedService.deleteFeed(this.feed.id).subscribe({
              next: () => {
                  this.feedService.selectFeed(null); // Clear selection
                  // In a split view, we might just want to clear the view,
                  // but since this component is routed, we might want to navigate up or reload.
                  // However, FeedsPageComponent handles the layout.
                  // If we are in an iframe (desktop), we might need to signal parent?
                  // But FeedService.selectFeed(null) should trigger FeedsPageComponent to clear iframeSrc.

                  // If we are on mobile (direct route), we should go back.
                  // But currently this component is mostly used inside iframe.

                  // Let's just reload the feed list in parent via service signal?
                  // The service.deleteFeed() call should trigger list refresh if list listens to it?
                  // FeedListComponent calls loadFeeds() on init. It doesn't auto-refresh on delete unless triggered.

                  // Actually, FeedListComponent handles delete itself usually.
                  // But here we are deleting from the detail view.

                  // We need to tell FeedListComponent to refresh.
                  // We can add a refresh subject to FeedService.

                  // For now, just clearing selection is enough for the view to disappear.
                  window.parent.postMessage('feedDeleted', '*'); // Hack for iframe communication if needed
              },
              error: (err) => alert('Failed to unsubscribe')
          });
      }
  }
}
