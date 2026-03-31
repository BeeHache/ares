import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { FeedService, FeedItem } from '../feed.service';

@Component({
  selector: 'app-search',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './search.component.html',
  styleUrl: './search.component.css'
})
export class SearchComponent implements OnInit {
  query = signal<string>('');
  results = signal<FeedItem[]>([]);
  loading = signal<boolean>(false);

  constructor(
    private route: ActivatedRoute,
    private feedService: FeedService
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.query.set(params['q']);
      if (this.query()) {
        this.performSearch();
      }
    });
  }

  performSearch() {
    this.loading.set(true);
    this.feedService.search(this.query()).subscribe({
      next: (data) => {
        this.results.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Search error', err);
        this.loading.set(false);
      }
    });
  }
}
