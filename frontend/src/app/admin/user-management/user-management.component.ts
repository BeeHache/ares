import { Component, OnInit, signal, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { AuthService } from '../../auth.service';
import { FormsModule } from '@angular/forms';

interface Role {
  id: number;
  name: string;
}

interface Account {
  id: number;
  username: string;
  type: string;
  accountEnabledAt: string;
  accountLockedUntil: string;
  accountExpiresAt: string;
  roles?: Role[];
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
  allRoles = signal<Role[]>([]);
  loading = signal<boolean>(false);
  currentPage = signal<number>(0);
  totalPages = signal<number>(0);
  pageSize = signal<number>(20);

  // Filter Signals
  typeFilter = signal<string>('');
  statusFilter = signal<string>('');

  // Role Edit state
  editingRolesFor = signal<Account | null>(null);
  selectedRoleIds = new Set<number>();

  constructor(
    private http: HttpClient,
    public authService: AuthService
  ) {
    effect(() => {
      this.typeFilter();
      this.statusFilter();
      this.loadUsers(0);
    });
  }

  ngOnInit(): void {
    this.loadAvailableRoles();
  }

  loadAvailableRoles() {
    this.http.get<Role[]>(`${environment.apiUrl}/admin/roles`).subscribe({
      next: (data) => this.allRoles.set(data),
      error: (err) => console.error('Error loading roles', err)
    });
  }

  loadUsers(page: number) {
    this.loading.set(true);
    let url = `${environment.apiUrl}/admin/accounts?page=${page}&size=${this.pageSize()}`;
    if (this.typeFilter()) url += `&type=${this.typeFilter()}`;
    if (this.statusFilter()) url += `&locked=${this.statusFilter() === 'LOCKED'}`;

    this.http.get<Page<Account>>(url).subscribe({
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

  openRoleEditor(account: Account) {
    this.editingRolesFor.set(account);
    this.selectedRoleIds.clear();
    if (account.roles) {
      account.roles.forEach(r => this.selectedRoleIds.add(r.id));
    }
  }

  closeRoleEditor() {
    this.editingRolesFor.set(null);
    this.selectedRoleIds.clear();
  }

  toggleRole(roleId: number) {
    if (this.selectedRoleIds.has(roleId)) {
      this.selectedRoleIds.delete(roleId);
    } else {
      this.selectedRoleIds.add(roleId);
    }
  }

  saveRoles() {
    const account = this.editingRolesFor();
    if (!account) return;

    const roleIds = Array.from(this.selectedRoleIds);
    this.http.put(`${environment.apiUrl}/admin/accounts/${account.id}/roles`, roleIds).subscribe({
      next: () => {
        this.loadUsers(this.currentPage());
        this.closeRoleEditor();
      },
      error: (err) => alert('Failed to update roles: ' + (err.error?.message || err.message))
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
