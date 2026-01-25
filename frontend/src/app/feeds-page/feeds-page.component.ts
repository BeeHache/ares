import { Component, OnInit, NgZone, ChangeDetectorRef, Inject, PLATFORM_ID, HostListener } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
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
  isMobile = false;
  showSidebar = true;

  constructor(
      private feedService: FeedService,
      private zone: NgZone,
      private cdr: ChangeDetectorRef,
      @Inject(PLATFORM_ID) private platformId: Object
  ) {
    console.log('FeedsPageComponent: Constructor called');
  }

  ngOnInit(): void {
    console.log('FeedsPageComponent: ngOnInit called');
    this.checkScreenSize();

    this.zone.run(() => {
        this.feedService.selectedFeed$.subscribe(feed => {
            console.log('FeedsPageComponent: Subscription fired with:', feed);
            this.selectedFeed = feed;

            if (this.isMobile && feed) {
                this.showSidebar = false; // Hide sidebar on mobile when feed selected
            }

            this.cdr.detectChanges();
        });
    });
  }

  @HostListener('window:resize', ['$event'])
  onResize(event: any) {
    this.checkScreenSize();
  }

  checkScreenSize() {
      if (isPlatformBrowser(this.platformId)) {
          this.isMobile = window.innerWidth < 768; // Mobile breakpoint
          if (!this.isMobile) {
              this.showSidebar = true; // Always show sidebar on desktop
          } else if (this.selectedFeed) {
              this.showSidebar = false; // Hide sidebar if feed selected on mobile
          } else {
              this.showSidebar = true; // Show sidebar if no feed selected
          }
      }
  }

  backToFeedList() {
      this.selectedFeed = null;
      this.feedService.selectFeed(null); // Clear selection in service
      this.showSidebar = true;
  }
}
