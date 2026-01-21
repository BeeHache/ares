import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FeedService, Feed } from '../feed.service';

@Component({
  selector: 'app-feed-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './feed-list.component.html',
  styleUrl: './feed-list.component.css'
})
export class FeedListComponent implements OnInit {
  feeds: Feed[] = [];
  totalUnread = 0; // Not yet supported by backend

  constructor(
    private feedService: FeedService,
    private cdr: ChangeDetectorRef
  ) {
    console.log('FeedListComponent: Constructor called'); // Debug log
  }

  ngOnInit(): void {
    console.log('FeedListComponent: ngOnInit called'); // Debug log
    this.loadFeeds();
  }

  loadFeeds() {
    this.feedService.getFeeds().subscribe({
      next: (data) => {
        console.log('Feeds received from backend:', data); // Debug log
        this.feeds = data;
        this.cdr.detectChanges(); // Force update
      },
      error: (err) => console.error('Error loading feeds', err)
    });
  }

  selectFeed(feed: Feed) {
      console.log('FeedListComponent: selectFeed called with:', feed); // Debug log
      this.feedService.selectFeed(feed);
  }

  addFeed() {
    const link = prompt('Enter RSS Feed URL:');
    if (link) {
      this.feedService.addFeed(link).subscribe({
        next: (newFeed) => {
          console.log('Feed added:', newFeed); // Debug log
          this.feeds.push(newFeed);
          this.cdr.detectChanges(); // Force update
        },
        error: (err) => alert('Failed to add feed: ' + (err.error?.message || err.message))
      });
    }
  }

  deleteFeed(id: number | undefined) {
    if (id === undefined) {
        console.error('Cannot delete feed without ID');
        return;
    }
    if (confirm('Are you sure you want to unsubscribe?')) {
      this.feedService.deleteFeed(id).subscribe({
        next: () => {
          this.feeds = this.feeds.filter(f => f.id !== id);
          this.cdr.detectChanges(); // Force update
        },
        error: (err) => alert('Failed to delete feed')
      });
    }
  }

  importOpml() {
      // Simple file input trigger could be added here
      alert('OPML Import UI not implemented yet');
  }
}
