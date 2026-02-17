import { Component, OnInit } from '@angular/core';
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
  query = '';
  results: FeedItem[] = [];
  loading = false;

  constructor(
    private route: ActivatedRoute,
    private feedService: FeedService
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.query = params['q'];
      if (this.query) {
        this.performSearch();
      }
    });
  }

  performSearch() {
    this.loading = true;
    this.feedService.search(this.query).subscribe({
      next: (data) => {
        this.results = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Search error', err);
        this.loading = false;
      }
    });
  }
}
