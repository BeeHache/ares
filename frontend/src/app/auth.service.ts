import { Injectable, PLATFORM_ID, inject } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { Router } from '@angular/router';
import { jwtDecode } from 'jwt-decode';
import { BehaviorSubject, Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private platformId = inject(PLATFORM_ID);
  private router = inject(Router);
  private currentUserSubject = new BehaviorSubject<string | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

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
      const decodedToken: { sub: string } = jwtDecode(token);
      this.currentUserSubject.next(decodedToken.sub);
    } catch (error) {
      console.error('Error decoding token:', error);
      this.currentUserSubject.next(null);
    }
  }

  isLoggedIn(): boolean {
    return !!this.currentUserSubject.value;
  }

  getUsername(): string | null {
    return this.currentUserSubject.value;
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
      this.router.navigate(['/login']);
    }
  }
}
