import { Component, OnInit, ChangeDetectorRef, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

interface AdminStats {
  totalUsers: number;
  totalFeeds: number;
  totalArticles: number;
}

interface FeatureFlag {
  name: string;
  enabled: boolean;
  isChild?: boolean;
  parentName?: string;
}

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  message = signal<string>('');
  stats = signal<AdminStats | null>(null);
  features = signal<FeatureFlag[]>([]);

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
        next: (data) => this.message.set(data.message),
        error: (err) => this.message.set('Error loading admin data: ' + err.message)
      });
  }

  loadStats() {
    this.http.get<AdminStats>(`${environment.apiUrl}/admin/stats`)
      .subscribe({
        next: (data) => {
          this.stats.set(data);
        },
        error: (err) => console.error('Error loading stats', err)
      });
  }

  loadFeatures() {
    this.http.get<{ [key: string]: boolean }>(`${environment.apiUrl}/features`)
      .subscribe({
        next: (data) => {
          const socialChildren = ['GITHUB_LOGIN', 'GOOGLE_LOGIN', 'FACEBOOK_LOGIN', 'APPLE_LOGIN', 'MICROSOFT_LOGIN'];

          const processedFeatures = Object.keys(data).map(key => ({
            name: key,
            enabled: data[key],
            isChild: socialChildren.includes(key),
            parentName: socialChildren.includes(key) ? 'SOCIAL_LOGIN' : undefined
          }));

          processedFeatures.sort((a, b) => {
            const familyA = a.parentName || a.name;
            const familyB = b.parentName || b.name;
            if (familyA !== familyB) return familyA.localeCompare(familyB);
            if (!a.parentName) return -1;
            if (!b.parentName) return 1;
            return a.name.localeCompare(b.name);
          });

          this.features.set(processedFeatures);
        },
        error: (err) => console.error('Error loading features', err)
      });
  }

  isParentDisabled(feature: FeatureFlag): boolean {
    if (!feature.isChild || !feature.parentName) return false;
    const parent = this.features().find(f => f.name === feature.parentName);
    return parent ? !parent.enabled : false;
  }

  toggleFeature(feature: FeatureFlag) {
    if (this.isParentDisabled(feature)) return;

    const newState = !feature.enabled;
    this.http.post(`${environment.apiUrl}/features/${feature.name}?enabled=${newState}`, {})
      .subscribe({
        next: () => {
          // Update the signal value immutably
          this.features.update(fs => fs.map(f =>
            f.name === feature.name ? { ...f, enabled: newState } : f
          ));
        },
        error: (err) => {
          console.error('Error toggling feature', err);
          alert('Failed to update feature flag.');
        }
      });
  }
}
