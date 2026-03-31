import { Component, OnInit, Inject, PLATFORM_ID, HostListener, signal, effect } from '@angular/core';
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
  selectedFeed = signal<FeedTitle | null>(null);
  isMobile = signal<boolean>(false);
  showSidebar = signal<boolean>(true);
  iframeSrc = signal<SafeResourceUrl | null>(null);

  constructor(
      private feedService: FeedService,
      @Inject(PLATFORM_ID) private platformId: Object,
      private sanitizer: DomSanitizer
  ) {
    effect(() => {
      const feed = this.feedService.selectedFeed(); // React to the signal from FeedService
      this.selectedFeed.set(feed);

      if (feed && feed.id) {
          this.iframeSrc.set(this.sanitizer.bypassSecurityTrustResourceUrl(`/feed-items/${feed.id}`));
      } else {
          this.iframeSrc.set(null);
      }

      if (this.isMobile() && feed) {
          this.showSidebar.set(false);
      }
    });
  }

  ngOnInit(): void {
    this.checkScreenSize();
  }

  @HostListener('window:resize', ['$event'])
  onResize(event: any) {
    this.checkScreenSize();
  }

  checkScreenSize() {
      if (isPlatformBrowser(this.platformId)) {
          this.isMobile.set(window.innerWidth < 768);
          if (!this.isMobile()) {
              this.showSidebar.set(true);
          } else if (this.selectedFeed()) {
              this.showSidebar.set(false);
          } else {
              this.showSidebar.set(true);
          }
      }
  }

  backToFeedList() {
      this.selectedFeed.set(null);
      this.iframeSrc.set(null);
      this.feedService.selectFeed(null); // Update the service's signal
      this.showSidebar.set(true);
  }
}
