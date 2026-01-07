import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="container">
      <h1>Welcome to Ares</h1>
      <p>Your personal RSS reader.</p>
    </div>
  `,
  styles: [`
    .container { text-align: center; padding: 50px; }
  `]
})
export class HomeComponent {}
