import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { FeedService, FeedTitle } from '../feed.service';

@Component({
  selector: 'app-feed-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './feed-list.component.html',
  styleUrl: './feed-list.component.css'
})
export class FeedListComponent implements OnInit {
  feeds: FeedTitle[] = [];
  filteredFeeds: FeedTitle[] = [];
  totalUnread = 0;
  selectedFeedId: string | null = null;
  searchQuery = '';

  constructor(
    private feedService: FeedService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadFeeds();

    this.feedService.selectedFeed$.subscribe(feed => {
        this.selectedFeedId = feed ? feed.id : null;
        this.cdr.detectChanges();
    });
  }

  loadFeeds() {
    this.feedService.getFeedTitles().subscribe({
      next: (data) => {
        this.feeds = data;
        this.filterFeeds();
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Error loading feeds', err)
    });
  }

  filterFeeds() {
    if (!this.searchQuery) {
      this.filteredFeeds = this.feeds;
    } else {
      const lowerCaseQuery = this.searchQuery.toLowerCase();
      this.filteredFeeds = this.feeds.filter(feed =>
        feed.title.toLowerCase().includes(lowerCaseQuery)
      );
    }
  }

  selectFeed(feed: FeedTitle) {
      this.feedService.selectFeed(feed);
  }
}
