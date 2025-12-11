import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders, HttpParams} from '@angular/common/http';
import {environment} from '../../environments/environment';
import {jwtDecode} from 'jwt-decode';
import {Router} from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  isAuthenticated : boolean = false;
  roles : any;
  username : any;
  accessToken!: any;

  constructor(private http: HttpClient, private router: Router) { }

  public login(username : string, password : string ) {
    let options = {
      headers: new HttpHeaders().set('Content-Type', 'application/x-www-form-urlencoded '),
    }
    let params = new HttpParams().set('username', username).set('password', password);
    return this.http.post(environment.backendHost+"/auth/login",params,options)
  }

  loadProfile(data: any) {
    if (!data || typeof data['access-token'] !== 'string') {
      console.error('Access token invalide:', data);
      return;
    }

    this.isAuthenticated = true;
    this.accessToken = data['access-token'];

    try {
      let decodeJwt: any = jwtDecode(this.accessToken);
      console.log("decodeJwt : ",decodeJwt);
      this.username = decodeJwt.sub;
      this.roles = decodeJwt.scope;
      window.localStorage.setItem('jwt-token', this.accessToken);
    } catch (e) {
      console.error('Erreur lors du décodage du JWT:', e);
      this.isAuthenticated = false;
    }
  }

  logout() {
    this.isAuthenticated = false;
    this.accessToken = undefined;
    this.username = undefined;
    this.roles = undefined;
    window.localStorage.removeItem('jwt-token');
    this.router.navigateByUrl('/login');
  }

  loadJwtTokenFromLocalStorage() {
    let token = window.localStorage.getItem('jwt-token');
    if(token) {
      this.loadProfile({"access-token": token});
      this.router.navigateByUrl('/admin/customers');
    }
  }
}
