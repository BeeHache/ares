import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

interface Account {
  id: number;
  username: string;
  type: string;
  accountEnabledAt: string;
  accountLockedUntil: string;
  accountExpiresAt: string;
}

interface Page<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  number: number;
  size: number;
}

@Component({
  selector: 'app-user-management',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './user-management.component.html',
  styleUrl: './user-management.component.css'
})
export class UserManagementComponent implements OnInit {
  users: Account[] = [];
  currentPage = 0;
  totalPages = 0;
  pageSize = 20;
  loading = false;

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.loadUsers(0);
  }

  loadUsers(page: number) {
    this.loading = true;
    this.http.get<Page<Account>>(`${environment.apiUrl}/admin/users?page=${page}&size=${this.pageSize}`)
      .subscribe({
        next: (data) => {
          this.users = data.content;
          this.currentPage = data.number;
          this.totalPages = data.totalPages;
          this.loading = false;
        },
        error: (err) => {
          console.error('Error loading users', err);
          this.loading = false;
        }
      });
  }

  lockUser(id: number) {
    if (confirm('Are you sure you want to lock this user?')) {
      this.http.post(`${environment.apiUrl}/admin/users/${id}/lock`, {})
        .subscribe({
          next: () => this.loadUsers(this.currentPage),
          error: (err) => alert('Failed to lock user')
        });
    }
  }

  unlockUser(id: number) {
    this.http.post(`${environment.apiUrl}/admin/users/${id}/unlock`, {})
      .subscribe({
        next: () => this.loadUsers(this.currentPage),
        error: (err) => alert('Failed to unlock user')
      });
  }

  isLocked(user: Account): boolean {
    if (!user.accountLockedUntil) return false;
    return new Date(user.accountLockedUntil) > new Date();
  }
}
