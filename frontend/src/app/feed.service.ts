import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Feed {
  id?: number;
  title: string;
  description?: string;
  link: string;
  isPodcast: boolean;
  lastModified?: string;
  image?: any; // Add type if needed
  items?: any[]; // Add type if needed
  enclosures?: any[];
  date: string;

}

@Injectable({
  providedIn: 'root'
})
export class FeedService {
  private apiUrl = 'http://localhost:8080/api/user';

  constructor(private http: HttpClient) {}

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
