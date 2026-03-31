import { Component, OnInit, signal, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { AuthService } from '../../auth.service';
import { FormsModule } from '@angular/forms';

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
  imports: [CommonModule, FormsModule],
  templateUrl: './user-management.component.html',
  styleUrl: './user-management.component.css'
})
export class UserManagementComponent implements OnInit {
  // Signals
  users = signal<Account[]>([]);
  loading = signal<boolean>(false);
  currentPage = signal<number>(0);
  totalPages = signal<number>(0);
  pageSize = signal<number>(20);

  // Filter Signals
  typeFilter = signal<string>(''); // empty, ADMIN, USER
  statusFilter = signal<string>(''); // empty, LOCKED, ACTIVE

  constructor(
    private http: HttpClient,
    public authService: AuthService
  ) {
    // Reload users when filters change
    effect(() => {
      this.typeFilter();
      this.statusFilter();
      this.loadUsers(0);
    });
  }

  ngOnInit(): void {
    // Initial load happens via effect
  }

  loadUsers(page: number) {
    this.loading.set(true);

    let url = `${environment.apiUrl}/admin/accounts?page=${page}&size=${this.pageSize()}`;

    if (this.typeFilter()) {
      url += `&type=${this.typeFilter()}`;
    }

    if (this.statusFilter()) {
      const isLocked = this.statusFilter() === 'LOCKED';
      url += `&locked=${isLocked}`;
    }

    this.http.get<Page<Account>>(url)
      .subscribe({
        next: (data) => {
          this.users.set(data.content);
          this.currentPage.set(data.number);
          this.totalPages.set(data.totalPages);
          this.loading.set(false);
        },
        error: (err) => {
          console.error('Error loading users', err);
          this.loading.set(false);
        }
      });
  }

  lockUser(id: number) {
    if (confirm('Are you sure you want to lock this user?')) {
      this.http.post(`${environment.apiUrl}/admin/users/${id}/lock`, {})
        .subscribe({
          next: () => this.loadUsers(this.currentPage()),
          error: (err) => alert('Failed to lock user')
        });
    }
  }

  unlockUser(id: number) {
    this.http.post(`${environment.apiUrl}/admin/users/${id}/unlock`, {})
      .subscribe({
        next: () => this.loadUsers(this.currentPage()),
        error: (err) => alert('Failed to unlock user')
      });
  }

  isLocked(user: Account): boolean {
    if (!user.accountLockedUntil) return false;
    return new Date(user.accountLockedUntil) > new Date();
  }

  isCurrentUser(username: string): boolean {
    return this.authService.currentUser() === username;
  }
}
