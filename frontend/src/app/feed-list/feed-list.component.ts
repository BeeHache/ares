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
  viewMode: 'list' | 'grid' = 'list';

  constructor(
    private feedService: FeedService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    // Load user's preferred view mode from localStorage
    const savedViewMode = localStorage.getItem('feedListViewMode') as 'list' | 'grid';
    if (savedViewMode) {
      this.viewMode = savedViewMode;
    }

    this.loadFeeds();

    this.feedService.selectedFeed$.subscribe(feed => {
        this.selectedFeedId = feed ? feed.id : null;
        this.cdr.detectChanges();
    });
  }

  toggleViewMode(): void {
    this.viewMode = this.viewMode === 'list' ? 'grid' : 'list';
    localStorage.setItem('feedListViewMode', this.viewMode);
  }

  loadFeeds() {
    this.feedService.getFeedTitles().subscribe({
      next: (data) => {
        // Sort by pubdate descending (newest first)
        this.feeds = data.sort((a, b) => {
            const dateA = a.pubdate ? new Date(a.pubdate).getTime() : 0;
            const dateB = b.pubdate ? new Date(b.pubdate).getTime() : 0;
            return dateB - dateA;
        });
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
        feed.title?.toLowerCase().includes(lowerCaseQuery)
      );
    }
  }

  selectFeed(feed: FeedTitle) {
      this.feedService.selectFeed(feed);
  }
}
