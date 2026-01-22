import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { PLATFORM_ID, inject } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { catchError, throwError } from 'rxjs';
import { Router } from '@angular/router';
import { AuthService } from './auth.service'; // Assuming AuthService is available

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const platformId = inject(PLATFORM_ID);
  const router = inject(Router);
  const authService = inject(AuthService); // Inject AuthService

  let clonedReq = req;

  if (isPlatformBrowser(platformId)) {
    const token = localStorage.getItem('token');
    if (token) {
      clonedReq = req.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
    }
  }

  return next(clonedReq).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        console.warn('AuthInterceptor: 401 Unauthorized response received. Logging out user.');
        authService.logout(); // Clear token and navigate to login
      }
      return throwError(() => error); // Re-throw the error for other handlers
    })
  );
};
