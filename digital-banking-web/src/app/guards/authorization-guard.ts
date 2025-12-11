import { Injectable } from '@angular/core';
import {
  ActivatedRouteSnapshot,
  CanActivate,
  GuardResult,
  MaybeAsync,
  Router,
  RouterStateSnapshot
} from '@angular/router';
import {AuthService} from '../services/auth-service';

@Injectable({
  providedIn: 'root'
})
export class AuthorizationGuard implements CanActivate {

  constructor(private router: Router, private authService: AuthService) { }

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot): MaybeAsync<GuardResult> {
    const expectedRole = route.data['role'];
    console.log("expectedRole : ", expectedRole);
    if (this.authService.roles.includes("ADMIN")) {
      return true;
    }
    else {
      this.router.navigateByUrl("/admin/notAuthorized");
      return false;
    }
  }

}
