import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';

interface FeedItem {
  id: number;
  title: string;
  feedName: string;
  date: Date;
  snippet: string;
  isRead: boolean;
}

@Component({
  selector: 'app-feed-items',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './feed-items.component.html',
  styleUrl: './feed-items.component.css'
})
export class FeedItemsComponent implements OnInit {
  items: FeedItem[] = [];

  ngOnInit(): void {
    // Mock data
    this.items = [
      { id: 1, title: 'Breaking News: Something Happened', feedName: 'BBC News', date: new Date(), snippet: 'A major event occurred today, shaking the foundations of...', isRead: false },
      { id: 2, title: 'TechCrunch Weekly: AI Advances', feedName: 'TechCrunch', date: new Date(Date.now() - 86400000), snippet: 'New developments in artificial intelligence are set to change the industry...', isRead: true },
      { id: 3, title: 'Another Story from BBC', feedName: 'BBC News', date: new Date(Date.now() - 172800000), snippet: 'In other news, a local cat has been elected mayor...', isRead: false },
    ];
  }
}
