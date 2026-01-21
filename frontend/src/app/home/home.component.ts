import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../auth.service';
import { FeedListComponent } from '../feed-list/feed-list.component';
import { FeedItemsComponent } from '../feed-items/feed-items.component';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, FeedListComponent, FeedItemsComponent],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent {
  constructor(public authService: AuthService) {}
}
