import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FeedService, FeedTitle, Feed } from '../feed.service';
import { OpmlImportComponent } from '../opml-import/opml-import.component';

@Component({
  selector: 'app-feed-list',
  standalone: true,
  imports: [CommonModule, OpmlImportComponent],
  templateUrl: './feed-list.component.html',
  styleUrl: './feed-list.component.css'
})
export class FeedListComponent implements OnInit {
  feeds: FeedTitle[] = [];
  totalUnread = 0; // Not yet supported by backend
  showImportModal = false;

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
    this.feedService.getFeedTitles().subscribe({
      next: (data) => {
        console.log('Feeds received from backend:', data); // Debug log
        this.feeds = data;
        this.cdr.detectChanges(); // Force update
      },
      error: (err) => console.error('Error loading feeds', err)
    });
  }

  selectFeed(feed: FeedTitle) {
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

  deleteFeed(id: string | undefined) {
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

  openImportModal() {
      this.showImportModal = true;
  }

  closeImportModal() {
      this.showImportModal = false;
  }

  onImportSuccess() {
      this.loadFeeds(); // Refresh list
  }
}
