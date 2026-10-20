import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { from, switchMap } from 'rxjs';
import { environment } from '../../environments/environment';
import { Auth } from './auth';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  if (!req.url.startsWith(`${environment.apiUrl}/`)) return next(req);
  return from(inject(Auth).idToken()).pipe(
    switchMap((token) =>
      next(token ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : req),
    ),
  );
};
