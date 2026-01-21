import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';

interface Feed {
  id: number;
  name: string;
  unreadCount: number;
}

@Component({
  selector: 'app-feed-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './feed-list.component.html',
  styleUrl: './feed-list.component.css'
})
export class FeedListComponent implements OnInit {
  feeds: Feed[] = [];
  totalUnread = 0;

  ngOnInit(): void {
    // Mock data for now
    this.feeds = [
      { id: 1, name: 'BBC News', unreadCount: 3 },
      { id: 2, name: 'The Daily', unreadCount: 0 },
      { id: 3, name: 'TechCrunch', unreadCount: 2 },
    ];
    this.totalUnread = this.feeds.reduce((sum, feed) => sum + feed.unreadCount, 0);
  }
}
