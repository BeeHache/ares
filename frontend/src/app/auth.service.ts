import { Injectable, PLATFORM_ID, inject, signal } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { Router } from '@angular/router';
import { jwtDecode } from 'jwt-decode';

interface DecodedToken {
  sub: string;
  roles?: string[];
  name?: string;
  [key: string]: any;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private platformId = inject(PLATFORM_ID);
  private router = inject(Router);

  // Signals for state management
  public currentUser = signal<string | null>(null);
  public currentName = signal<string | null>(null);
  public currentRoles = signal<string[]>([]);

  constructor() {
    if (isPlatformBrowser(this.platformId)) {
      const token = localStorage.getItem('token');
      if (token) {
        this.updateUserFromToken(token);
      }
    }
  }

  private updateUserFromToken(token: string) {
    try {
      const decodedToken: DecodedToken = jwtDecode(token);
      this.currentUser.set(decodedToken.sub);
      this.currentName.set(decodedToken.name || null);
      this.currentRoles.set(decodedToken.roles || []);

      console.log('User Logged In (Signal):', this.currentUser());
      console.log('User Name (Signal):', this.currentName());
      console.log('User Roles (Signal):', this.currentRoles());

    } catch (error) {
      console.error('Error decoding token:', error);
      this.clearState();
    }
  }

  private clearState() {
    this.currentUser.set(null);
    this.currentName.set(null);
    this.currentRoles.set([]);
  }

  isLoggedIn(): boolean {
    return !!this.currentUser();
  }

  getUsername(): string | null {
    return this.currentUser();
  }

  getName(): string | null {
    return this.currentName();
  }

  hasRole(role: string): boolean {
    return this.currentRoles().includes(role);
  }

  getToken(): string | null {
    if (isPlatformBrowser(this.platformId)) {
      return localStorage.getItem('token');
    }
    return null;
  }

  login(token: string) {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.setItem('token', token);
      this.updateUserFromToken(token);
    }
  }

  logout(): void {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.removeItem('token');
      this.clearState();
      this.router.navigate(['/login']);
    }
  }
}
