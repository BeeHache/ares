import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../environments/environment';

export interface Enclosure {
  id: string;
  url: string;
  length?: number;
  type?: string;
}

export interface FeedItem {
  id: string;
  title: string;
  description?: string;
  link: string;
  date?: string;
  enclosures?: Enclosure[];
}

export interface FeedTitle {
  id: string;
  title: string;
}

export interface Feed {
  id: string;
  title: string;
  description?: string;
  link: string;
  isPodcast: boolean;
  lastModified?: string;
  image?: any;
  items?: FeedItem[];
  date?: string;
}

@Injectable({
  providedIn: 'root'
})
export class FeedService {
  private apiUrl = `${environment.apiUrl}/feed`;

  private selectedFeedSubject = new BehaviorSubject<FeedTitle | null>(null);
  selectedFeed$ = this.selectedFeedSubject.asObservable();

  constructor(private http: HttpClient) {
      this.selectedFeed$.subscribe(feed => {
          // Keep subscription active
      });
  }

  selectFeed(feed: FeedTitle | null) {
      this.selectedFeedSubject.next(feed);
  }

  getFeedTitles(): Observable<FeedTitle[]> {
      return this.http.get<FeedTitle[]>(`${this.apiUrl}/titles`);
  }

  getFeeds(): Observable<Feed[]> {
    return this.http.get<Feed[]>(`${this.apiUrl}`);
  }

  getFeedById(id: string): Observable<Feed> {
    return this.http.get<Feed>(`${this.apiUrl}/${id}`);
  }

  addFeed(link: string): Observable<Feed> {
    return this.http.put<Feed>(`${this.apiUrl}`, null, { params: { link } });
  }

  deleteFeed(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  importOpmlFile(file: File): Observable<void> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<void>(`${this.apiUrl}/import`, formData);
  }

  importOpmlUrl(url: string): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/import`, null, { params: { url } });
  }
}
