import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="container">
      <h1>Welcome to Ares</h1>
      <p>Your personal RSS reader.</p>
      <nav>
        <a routerLink="/login" class="btn">Login</a>
        <a routerLink="/register" class="btn">Register</a>
      </nav>
    </div>
  `,
  styles: [`
    .container { text-align: center; padding: 50px; }
    .btn { margin: 10px; padding: 10px 20px; background-color: #007bff; color: white; text-decoration: none; border-radius: 5px; }
    .btn:hover { background-color: #0056b3; }
  `]
})
export class HomeComponent {}
