import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
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
}
