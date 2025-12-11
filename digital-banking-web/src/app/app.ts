import {Component, OnInit} from '@angular/core';
import { RouterOutlet } from '@angular/router';
import {Navbar} from './navbar/navbar';
import {FormBuilder} from '@angular/forms';
import {AuthService} from './services/auth-service';
import {Toast} from './toast/toast';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, Toast],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit{
  protected title = 'digital-banking-web';

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    this.authService.loadJwtTokenFromLocalStorage();
  }
}
