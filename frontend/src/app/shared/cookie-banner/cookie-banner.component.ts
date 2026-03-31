import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-cookie-banner',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './cookie-banner.component.html',
  styleUrl: './cookie-banner.component.css'
})
export class CookieBannerComponent implements OnInit {
  isVisible = signal<boolean>(false);

  ngOnInit() {
    const consent = localStorage.getItem('cookieConsent');
    if (!consent) {
      this.isVisible.set(true);
    }
  }

  dismiss() {
    localStorage.setItem('cookieConsent', 'true');
    this.isVisible.set(false);
  }
}
