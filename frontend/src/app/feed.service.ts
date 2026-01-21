import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';

export interface FeedItem {
  id?: number;
  title: string;
  description?: string;
  link: string;
  date?: string;
  // Add other fields as needed
}

export interface Feed {
  id?: number;
  title: string;
  description?: string;
  link: string;
  isPodcast: boolean;
  lastModified?: string;
  image?: any;
  items?: FeedItem[];
  enclosures?: any[];
  date?: string;
}

@Injectable({
  providedIn: 'root'
})
export class FeedService {
  private apiUrl = 'http://localhost:8080/api/user';

  private selectedFeedSubject = new BehaviorSubject<Feed | null>(null);
  selectedFeed$ = this.selectedFeedSubject.asObservable();

  constructor(private http: HttpClient) {
      console.log('FeedService instance created');
      this.selectedFeed$.subscribe(feed => {
          console.log('FeedService: selectedFeed$ emitted:', feed);
      });
  }

  selectFeed(feed: Feed | null) {
      console.log('FeedService: selectFeed() method called with:', feed);
      if (feed) {
          console.log('FeedService: Calling .next() with a valid feed object.');
          this.selectedFeedSubject.next(feed);
      } else {
          console.log('FeedService: Calling .next() with null.');
          this.selectedFeedSubject.next(null);
      }
  }

  getFeeds(): Observable<Feed[]> {
    return this.http.get<Feed[]>(`${this.apiUrl}/feeds`);
  }

  addFeed(link: string): Observable<Feed> {
    return this.http.put<Feed>(`${this.apiUrl}/addfeed`, null, { params: { link } });
  }

  deleteFeed(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/feeds/${id}`);
  }

  importOpmlFile(file: File): Observable<void> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<void>(`${this.apiUrl}/import`, formData);
  }
}
