import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor, HttpHeaders
} from '@angular/common/http';
import {catchError, Observable, throwError} from 'rxjs';
import {AuthService} from '../services/auth-service';

@Injectable()
export class AppHttpInterceptor implements HttpInterceptor {

  constructor(private authService: AuthService) { }

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    console.log("url --> ",request.url);
    if (!request.url.includes("/auth/login")) {
      let newrequest = request.clone({
        headers: request.headers.set('Authorization', 'Bearer '+this.authService.accessToken)
      })
      return next.handle(newrequest).pipe(
          catchError((error) => {
            if(error.status === 401) {
              this.authService.logout()
            }
            return throwError(error);
          })
      );
    } else return next.handle(request);
  }
}
