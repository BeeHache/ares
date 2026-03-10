import { Component, OnInit, Inject, PLATFORM_ID } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { isPlatformBrowser } from '@angular/common';
import { AuthService } from '../../auth.service';
import { jwtDecode } from 'jwt-decode';

interface DecodedToken {
  roles?: string[];
  [key: string]: any;
}

@Component({
  selector: 'app-login-success',
  standalone: true,
  template: '<p>Logging you in...</p>'
})
export class LoginSuccessComponent implements OnInit {

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {}

  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      console.log('LoginSuccessComponent: Initialized (Browser)');

      this.route.queryParams.subscribe(params => {
        const token = params['token'];
        console.log('LoginSuccessComponent: Token from query params:', token);

        if (token) {
          console.log('LoginSuccessComponent: Token found, logging in...');
          this.authService.login(token);

          // Determine redirect based on role
          setTimeout(() => this.redirectUser(token), 50);
        } else {
          console.error('LoginSuccessComponent: No token found in login-success redirect');
          console.log('LoginSuccessComponent: Full URL:', this.router.url);
          this.router.navigate(['/login']);
        }
      });
    } else {
        console.log('LoginSuccessComponent: Running on Server (SSR), skipping logic.');
    }
  }

  redirectUser(token: string) {
    try {
      const decodedToken: DecodedToken = jwtDecode(token);
      const roles = decodedToken.roles || [];

      if (roles.includes('ROLE_ADMIN')) {
        this.router.navigate(['/admin']);
      } else {
        this.router.navigate(['/feeds']);
      }
    } catch (error) {
      console.error('Error decoding token for redirect:', error);
      this.router.navigate(['/feeds']);
    }
  }
}
