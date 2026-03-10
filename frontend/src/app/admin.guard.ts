import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService } from './auth.service';
import { jwtDecode } from 'jwt-decode';

interface DecodedToken {
  roles?: string[];
  [key: string]: any;
}

export const adminGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const token = authService.getToken();

  if (token) {
    try {
      const decodedToken: DecodedToken = jwtDecode(token);
      const roles = decodedToken.roles || [];

      if (roles.includes('ROLE_ADMIN')) {
        return true; // User is an admin, allow access
      }
    } catch (error) {
      console.error('Error decoding token for admin guard:', error);
    }
  }

  // If no token, or token is invalid, or user is not an admin
  console.warn('AdminGuard: Access denied. User is not an admin.');
  router.navigate(['/']); // Redirect to home page
  return false;
};
