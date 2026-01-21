import { Component, OnInit, NgZone, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FeedListComponent } from '../feed-list/feed-list.component';
import { FeedItemsComponent } from '../feed-items/feed-items.component';
import { Feed, FeedService } from '../feed.service';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-feeds-page',
  standalone: true,
  imports: [CommonModule, FeedListComponent, FeedItemsComponent],
  templateUrl: './feeds-page.component.html',
  styleUrl: './feeds-page.component.css'
})
export class FeedsPageComponent implements OnInit {
  selectedFeed: Feed | null = null;

  constructor(
      private feedService: FeedService,
      private zone: NgZone,
      private cdr: ChangeDetectorRef
  ) {
    console.log('FeedsPageComponent: Constructor called');
  }

  ngOnInit(): void {
    console.log('FeedsPageComponent: ngOnInit called');
    this.zone.run(() => {
        this.feedService.selectedFeed$.subscribe(feed => {
            console.log('FeedsPageComponent: Subscription fired with:', feed);
            this.selectedFeed = feed;
            this.cdr.detectChanges(); // Ensure UI updates
        });
    });
  }
}
