import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Feed, FeedItem } from '../feed.service';

@Component({
  selector: 'app-feed-items',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './feed-items.component.html',
  styleUrl: './feed-items.component.css'
})
export class FeedItemsComponent {
  private _feed: Feed | null = null;
  items: FeedItem[] = [];

  @Input()
  set feed(value: Feed | null) {
    console.log('FeedItemsComponent setter called with:', value); // Debug log
    this._feed = value;
    if (value) {
      this.items = value.items || [];
      console.log('Items to display:', this.items);

      // Sort by date descending if needed
      this.items.sort((a, b) => {
          const dateA = a.date ? new Date(a.date).getTime() : 0;
          const dateB = b.date ? new Date(b.date).getTime() : 0;
          return dateB - dateA;
      });
    } else {
        this.items = [];
    }
  }

  get feed(): Feed | null {
    return this._feed;
  }
}
