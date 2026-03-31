import { Component, OnInit, signal, computed } from '@angular/core';
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
  // Signals
  feeds = signal<FeedTitle[]>([]);
  searchQuery = signal<string>('');
  viewMode = signal<'list' | 'grid'>('list');

  // Computed signals
  filteredFeeds = computed(() => {
    const query = this.searchQuery().toLowerCase();
    const allFeeds = this.feeds();

    if (!query) return allFeeds;

    return allFeeds.filter(feed =>
      feed.title?.toLowerCase().includes(query)
    );
  });

  constructor(
    public feedService: FeedService
  ) {}

  ngOnInit(): void {
    // Load user's preferred view mode from localStorage
    const savedViewMode = localStorage.getItem('feedListViewMode') as 'list' | 'grid';
    if (savedViewMode) {
      this.viewMode.set(savedViewMode);
    }

    this.loadFeeds();
  }

  toggleViewMode(): void {
    const nextMode = this.viewMode() === 'list' ? 'grid' : 'list';
    this.viewMode.set(nextMode);
    localStorage.setItem('feedListViewMode', nextMode);
  }

  loadFeeds() {
    this.feedService.getFeedTitles().subscribe({
      next: (data) => {
        // Sort by pubdate descending (newest first) and set signal
        const sortedData = data.sort((a, b) => {
            const dateA = a.pubdate ? new Date(a.pubdate).getTime() : 0;
            const dateB = b.pubdate ? new Date(b.pubdate).getTime() : 0;
            return dateB - dateA;
        });
        this.feeds.set(sortedData);
      },
      error: (err) => console.error('Error loading feeds', err)
    });
  }

  onSearchChange(event: Event) {
    const input = event.target as HTMLInputElement;
    this.searchQuery.set(input.value);
  }

  selectFeed(feed: FeedTitle) {
      this.feedService.selectFeed(feed);
  }
}
