import { Component, OnInit, NgZone, ChangeDetectorRef, Inject, PLATFORM_ID, HostListener } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FeedListComponent } from '../feed-list/feed-list.component';
import { FeedTitle, FeedService } from '../feed.service';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';

@Component({
  selector: 'app-feeds-page',
  standalone: true,
  imports: [CommonModule, FeedListComponent],
  templateUrl: './feeds-page.component.html',
  styleUrl: './feeds-page.component.css'
})
export class FeedsPageComponent implements OnInit {
  selectedFeed: FeedTitle | null = null;
  isMobile = false;
  showSidebar = true;
  iframeSrc: SafeResourceUrl | null = null;

  constructor(
      private feedService: FeedService,
      private zone: NgZone,
      private cdr: ChangeDetectorRef,
      @Inject(PLATFORM_ID) private platformId: Object,
      private sanitizer: DomSanitizer
  ) {
  }

  ngOnInit(): void {
    this.checkScreenSize();

    this.zone.run(() => {
        this.feedService.selectedFeed$.subscribe(feed => {
            this.selectedFeed = feed;

            if (feed && feed.id) {
                this.iframeSrc = this.sanitizer.bypassSecurityTrustResourceUrl(`/feed-items/${feed.id}`);
            } else {
                this.iframeSrc = null;
            }

            if (this.isMobile && feed) {
                this.showSidebar = false;
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
          this.isMobile = window.innerWidth < 768;
          if (!this.isMobile) {
              this.showSidebar = true;
          } else if (this.selectedFeed) {
              this.showSidebar = false;
          } else {
              this.showSidebar = true;
          }
      }
  }

  backToFeedList() {
      this.selectedFeed = null;
      this.iframeSrc = null;
      this.feedService.selectFeed(null);
      this.showSidebar = true;
  }
}
