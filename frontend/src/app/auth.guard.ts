import { inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { Router, CanActivateFn, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';

export const authGuard: CanActivateFn = (route: ActivatedRouteSnapshot, state: RouterStateSnapshot) => {
  const router = inject(Router);
  const platformId = inject(PLATFORM_ID);

  console.log('AuthGuard: Running for URL:', state.url);

  if (isPlatformBrowser(platformId)) {
    const token = localStorage.getItem('token');
    console.log('AuthGuard: Token from localStorage:', token);

    if (token) {
      console.log('AuthGuard: Access granted.');
      return true;
    } else {
      console.log('AuthGuard: No token found. Access denied.');
    }
  } else {
    console.log('AuthGuard: Not in browser. Access denied.');
  }

  // On the server, or if no token in browser, deny access and redirect
  router.navigate(['/login'], { queryParams: { returnUrl: state.url } });
  return false;
};
