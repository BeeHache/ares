import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { environment } from '../../../environments/environment';

interface Role {
  id: number;
  name: string;
  parentId?: number;
  parentName?: string;
  subRoles?: Role[];
}

@Component({
  selector: 'app-role-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './role-management.component.html',
  styleUrl: './role-management.component.css'
})
export class RoleManagementComponent implements OnInit {
  roles = signal<Role[]>([]);
  flatRoles = signal<Role[]>([]);
  loading = signal<boolean>(false);

  // Form state
  selectedRole = signal<Role | null>(null);
  isEditing = signal<boolean>(false);

  formData = {
    name: '',
    parentId: undefined as number | undefined
  };

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.loadRoles();
  }

  loadRoles() {
    this.loading.set(true);
    // Load hierarchy
    this.http.get<Role[]>(`${environment.apiUrl}/admin/roles/hierarchy`).subscribe({
      next: (data) => {
        this.roles.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error loading role hierarchy', err);
        this.loading.set(false);
      }
    });

    // Load flat list for parent selection
    this.http.get<Role[]>(`${environment.apiUrl}/admin/roles`).subscribe({
      next: (data) => this.flatRoles.set(data),
      error: (err) => console.error('Error loading flat roles', err)
    });
  }

  selectRole(role: Role) {
    this.selectedRole.set(role);
    this.isEditing.set(true);
    this.formData = {
      name: role.name,
      parentId: role.parentId
    };
  }

  resetForm() {
    this.selectedRole.set(null);
    this.isEditing.set(false);
    this.formData = {
      name: '',
      parentId: undefined
    };
  }

  onSubmit() {
    const payload = {
      name: this.formData.name,
      parentId: this.formData.parentId || null
    };

    if (this.isEditing() && this.selectedRole()) {
      this.http.put(`${environment.apiUrl}/admin/roles/${this.selectedRole()?.id}`, payload).subscribe({
        next: () => {
          this.loadRoles();
          this.resetForm();
        },
        error: (err) => alert(err.error?.message || 'Update failed')
      });
    } else {
      this.http.post(`${environment.apiUrl}/admin/roles`, payload).subscribe({
        next: () => {
          this.loadRoles();
          this.resetForm();
        },
        error: (err) => alert(err.error?.message || 'Creation failed')
      });
    }
  }

  deleteRole(id: number) {
    if (confirm('Are you sure you want to delete this role? It must not be assigned to any accounts or have sub-roles.')) {
      this.http.delete(`${environment.apiUrl}/admin/roles/${id}`).subscribe({
        next: () => this.loadRoles(),
        error: (err) => alert(err.error?.message || 'Deletion failed')
      });
    }
  }
}
