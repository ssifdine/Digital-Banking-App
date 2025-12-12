import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { AuthService } from '../services/auth-service';
import { Router } from '@angular/router';
import { NgOptimizedImage, CommonModule } from '@angular/common';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    CommonModule,
  ],
  templateUrl: './login.html',
  styleUrls: ['./login.css']
})
export class Login implements OnInit {

  formLogin!: FormGroup;
  loginError: string | null = null;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.formLogin = this.fb.group({
      username: ["", [Validators.required, Validators.minLength(4)]],
      password: ["", [Validators.required, Validators.minLength(5)]]
    });
  }

  handleLogin() {
    if (this.formLogin.invalid) return;

    const { username, password } = this.formLogin.value;
    this.loginError = null;

    this.authService.login(username, password).subscribe({
      next: (data) => {
        this.authService.loadProfile(data);
        this.router.navigateByUrl("/admin/dashboard");
      },
      error: (err) => {
        console.error("Login error", err);

        if (err.status === 401) {
          this.loginError = "Invalid username or password.";

          // Appliquer l'erreur 'unauthorized' sur les deux champs
          this.formLogin.get('username')?.setErrors({ unauthorized: true });
          this.formLogin.get('password')?.setErrors({ unauthorized: true });
        } else {
          this.loginError = "An unexpected error occurred. Please try again.";
        }

        // Après 5 secondes, enlever l'erreur 'unauthorized' pour réinitialiser l'état visuel
        setTimeout(() => {
          this.loginError = null;
          this.formLogin.get('username')?.updateValueAndValidity();
          this.formLogin.get('password')?.updateValueAndValidity();
        }, 5000);
      }
    });
  }
}
