import { Injectable, PLATFORM_ID, inject } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { Router } from '@angular/router';
import { jwtDecode } from 'jwt-decode';
import { BehaviorSubject, Observable } from 'rxjs';

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
  private currentUserSubject = new BehaviorSubject<string | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  private currentNameSubject = new BehaviorSubject<string | null>(null);
  public currentName$ = this.currentNameSubject.asObservable();

  private currentRoles: string[] = [];

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
      this.currentUserSubject.next(decodedToken.sub);
      this.currentNameSubject.next(decodedToken.name || null);
      this.currentRoles = decodedToken.roles || [];

      console.log('User Logged In:', decodedToken.sub);
      console.log('User Name:', decodedToken.name);
      console.log('User Roles:', this.currentRoles);

    } catch (error) {
      console.error('Error decoding token:', error);
      this.currentUserSubject.next(null);
      this.currentNameSubject.next(null);
      this.currentRoles = [];
    }
  }

  isLoggedIn(): boolean {
    return !!this.currentUserSubject.value;
  }

  getUsername(): string | null {
    return this.currentUserSubject.value;
  }

  getName(): string | null {
    return this.currentNameSubject.value;
  }

  hasRole(role: string): boolean {
    return this.currentRoles.includes(role);
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
      this.currentUserSubject.next(null);
      this.currentNameSubject.next(null);
      this.currentRoles = [];
      this.router.navigate(['/login']);
    }
  }
}
