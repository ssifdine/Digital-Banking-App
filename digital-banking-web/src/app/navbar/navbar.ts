import {Component, OnInit} from '@angular/core';
import {Router, RouterLink} from '@angular/router';
import {AuthenticationGuard} from '../guards/authentication-guard';
import {AuthService} from '../services/auth-service';
import {NgIf} from '@angular/common';

@Component({
  selector: 'app-navbar',
  imports: [
    RouterLink,
    NgIf
  ],
  templateUrl: './navbar.html',
  styleUrl: './navbar.css'
})
export class Navbar implements OnInit {

  constructor(public authService: AuthService , private router : Router) { }

  handleLogout() {
    this.authService.logout();
  }

  ngOnInit(): void {
  }
}
