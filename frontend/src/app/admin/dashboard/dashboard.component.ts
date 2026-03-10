import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

interface AdminStats {
  totalUsers: number;
  totalFeeds: number;
  totalArticles: number;
}

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  message = '';
  stats: AdminStats | null = null;
  features: { name: string, enabled: boolean }[] = [];

  constructor(
    private http: HttpClient,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadMessage();
    this.loadStats();
    this.loadFeatures();
  }

  loadMessage() {
    this.http.get<{message: string}>(`${environment.apiUrl}/admin/hello`)
      .subscribe({
        next: (data) => {
          this.message = data.message;
          this.cdr.detectChanges();
        },
        error: (err) => this.message = 'Error loading admin data: ' + err.message
      });
  }

  loadStats() {
    this.http.get<AdminStats>(`${environment.apiUrl}/admin/stats`)
      .subscribe({
        next: (data) => {
          this.stats = data;
          this.cdr.detectChanges();
        },
        error: (err) => console.error('Error loading stats', err)
      });
  }

  loadFeatures() {
    this.http.get<{ [key: string]: boolean }>(`${environment.apiUrl}/features`)
      .subscribe({
        next: (data) => {
          this.features = Object.keys(data).map(key => ({
            name: key,
            enabled: data[key]
          })).sort((a, b) => a.name.localeCompare(b.name));
          this.cdr.detectChanges();
        },
        error: (err) => console.error('Error loading features', err)
      });
  }

  toggleFeature(feature: { name: string, enabled: boolean }) {
    const newState = !feature.enabled;
    this.http.post(`${environment.apiUrl}/features/${feature.name}?enabled=${newState}`, {})
      .subscribe({
        next: () => {
          feature.enabled = newState;
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error('Error toggling feature', err);
          alert('Failed to update feature flag.');
          feature.enabled = !newState;
          this.cdr.detectChanges();
        }
      });
  }
}
