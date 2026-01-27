import { Component, signal, OnInit } from '@angular/core';
import { Router, RouterOutlet, NavigationEnd } from '@angular/router';
import { HeaderComponent } from './header/header.component';
import { filter } from 'rxjs/operators';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, HeaderComponent, CommonModule],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit {
  protected readonly title = signal('ares-ui');
  showHeader = true;

  constructor(private router: Router) {
      console.log('AppComponent: Constructor called. URL:', this.router.url);
  }

  ngOnInit(): void {
    console.log('AppComponent: ngOnInit called');
    this.router.events.subscribe(event => {
        if (event instanceof NavigationEnd) {
            console.log('AppComponent: NavigationEnd:', event.urlAfterRedirects);
            this.showHeader = !event.urlAfterRedirects.startsWith('/feed-items/');
            console.log('AppComponent: showHeader set to:', this.showHeader);
        }
    });
  }
}
