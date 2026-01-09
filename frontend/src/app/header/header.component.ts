import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../auth.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <header>
      <nav>
        <a class="brand" routerLink="/">Ares</a>
        <div class="nav-links">
          <ng-container *ngIf="authService.currentUser$ | async as username; else loggedOut">
            <span class="username">{{ username }}</span>
            <a routerLink="/user" class="nav-link">Profile</a>
            <button (click)="authService.logout()" class="logout-btn">Logout</button>
          </ng-container>
          <ng-template #loggedOut>
            <a routerLink="/login" class="nav-link">Login</a>
            <a routerLink="/register" class="nav-link">Register</a>
          </ng-template>
        </div>
      </nav>
    </header>
  `,
  styles: [`
    header { background-color: #343a40; padding: 10px 20px; }
    nav { display: flex; justify-content: space-between; align-items: center; }
    .brand { color: white; font-weight: bold; text-decoration: none; font-size: 1.5rem; }
    .nav-links { display: flex; align-items: center; gap: 15px; }
    .nav-link { color: #f8f9fa; text-decoration: none; }
    .nav-link:hover { color: #adb5bd; }
    .username { color: #f8f9fa; }
    .logout-btn { background: #dc3545; color: white; border: none; padding: 8px 12px; border-radius: 5px; cursor: pointer; }
    .logout-btn:hover { background: #c82333; }
  `]
})
export class HeaderComponent {
  constructor(public authService: AuthService) {}
}
