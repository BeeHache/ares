import { Component, OnInit, signal, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { AuthService } from '../../auth.service';
import { FormsModule } from '@angular/forms';
import { PasswordInputComponent } from '../../shared/password-input/password-input.component';

interface Role {
  id: number;
  name: string;
  subRoles?: Role[];
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
  imports: [CommonModule, FormsModule, PasswordInputComponent],
  templateUrl: './user-management.component.html',
  styleUrl: './user-management.component.css'
})
export class UserManagementComponent implements OnInit {
  // Signals
  users = signal<Account[]>([]);
  allRoles = signal<Role[]>([]); // Flat list for selection
  roleHierarchy = signal<Role[]>([]); // Hierarchy for effective calculation
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

  // Add Account state
  showAddUserModal = signal<boolean>(false);
  showAddAdminModal = signal<boolean>(false);

  newAccount = {
    username: '',
    email: '',
    name: '',
    password: '',
    type: 'USER',
    roleIds: [] as number[]
  };

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
    this.loadRoles();
  }

  loadRoles() {
    this.http.get<Role[]>(`${environment.apiUrl}/admin/roles`).subscribe({
      next: (data) => this.allRoles.set(data),
      error: (err) => console.error('Error loading roles', err)
    });

    this.http.get<Role[]>(`${environment.apiUrl}/admin/roles/hierarchy`).subscribe({
      next: (data) => this.roleHierarchy.set(data),
      error: (err) => console.error('Error loading hierarchy', err)
    });
  }

  getEffectiveRoles(account: Account): string[] {
    if (!account.roles || account.roles.length === 0) return [];
    const effectiveSet = new Set<string>();
    account.roles.forEach(role => {
      this.collectRolesRecursive(role.name, effectiveSet);
    });
    return Array.from(effectiveSet);
  }

  private collectRolesRecursive(roleName: string, set: Set<string>) {
    if (set.has(roleName)) return;
    set.add(roleName);
    const roleObj = this.findRoleInHierarchy(roleName, this.roleHierarchy());
    if (roleObj && roleObj.subRoles) {
      roleObj.subRoles.forEach(sub => this.collectRolesRecursive(sub.name, set));
    }
  }

  private findRoleInHierarchy(name: string, roles: Role[]): Role | null {
    for (const r of roles) {
      if (r.name === name) return r;
      if (r.subRoles) {
        const found = this.findRoleInHierarchy(name, r.subRoles);
        if (found) return found;
      }
    }
    return null;
  }

  isExplicitRole(account: Account, roleName: string): boolean {
    return !!account.roles?.some(r => r.name === roleName);
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

  openAddUserModal() {
    this.newAccount = { username: '', email: '', name: '', password: '', type: 'USER', roleIds: [] };
    this.showAddUserModal.set(true);
  }

  openAddAdminModal() {
    this.newAccount = { username: '', email: '', name: '', password: '', type: 'ADMIN', roleIds: [] };
    this.showAddAdminModal.set(true);
  }

  closeModals() {
    this.showAddUserModal.set(false);
    this.showAddAdminModal.set(false);
  }

  toggleNewAccountRole(roleId: number) {
    const index = this.newAccount.roleIds.indexOf(roleId);
    if (index > -1) {
      this.newAccount.roleIds.splice(index, 1);
    } else {
      this.newAccount.roleIds.push(roleId);
    }
  }

  createAccount() {
    const payload = {
      username: this.newAccount.username || this.newAccount.email,
      name: this.newAccount.type === 'ADMIN' ? this.newAccount.name : undefined,
      password: this.newAccount.password,
      type: this.newAccount.type,
      roles: this.newAccount.type === 'ADMIN' ? this.newAccount.roleIds.map(id => ({ id })) : undefined
    };
    this.http.post(`${environment.apiUrl}/admin/accounts`, payload).subscribe({
      next: () => {
        alert(`${this.newAccount.type} account created successfully`);
        this.loadUsers(0);
        this.closeModals();
      },
      error: (err) => alert('Failed to create account: ' + (err.error?.message || err.message))
    });
  }

  deleteAccount(id: number, username: string) {
    if (confirm(`Are you sure you want to delete account "${username}"? This will permanently remove all associated data.`)) {
      this.http.delete(`${environment.apiUrl}/admin/accounts/${id}`).subscribe({
        next: () => {
          alert('Account deleted successfully');
          this.loadUsers(this.currentPage());
        },
        error: (err) => alert('Failed to delete account: ' + (err.error?.message || err.message))
      });
    }
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
