import { Component } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { UserService} from '../services/user-service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-change-password',
  imports: [ReactiveFormsModule, CommonModule],
  templateUrl: './change-password.html',
  styleUrl: './change-password.css',
})
export class ChangePassword {
  passwordForm!: FormGroup;
  message = '';
  error = '';

  constructor(
    private fb: FormBuilder,
    private userService: UserService,
  ) {
    this.passwordForm = this.fb.group({
      oldPassword: ['', Validators.required],
      newPassword: ['', [Validators.required, Validators.minLength(6)]],
    });
  }

  submit() {
    if (this.passwordForm.invalid) return;

    this.userService.changePassword(this.passwordForm.value).subscribe({
      next: (res) => {
        this.message = res;
        this.error = '';
        this.passwordForm.reset();
      },
      error: (err) => {
        this.error = err.error;
        this.message = '';
      },
    });
  }

  getPasswordStrength(): number {
    const password = this.passwordForm.get('newPassword')?.value || '';
    if (!password) return 0;

    let strength = 0;

    // Longueur (max 40 points)
    if (password.length >= 8) strength += 20;
    if (password.length >= 12) strength += 10;
    if (password.length >= 16) strength += 10;

    // Contient des minuscules (15 points)
    if (/[a-z]/.test(password)) strength += 15;

    // Contient des majuscules (15 points)
    if (/[A-Z]/.test(password)) strength += 15;

    // Contient des chiffres (15 points)
    if (/[0-9]/.test(password)) strength += 15;

    // Contient des caractères spéciaux (15 points)
    if (/[^a-zA-Z0-9]/.test(password)) strength += 15;

    return Math.min(strength, 100);
  }

  getPasswordStrengthClass(): string {
    const strength = this.getPasswordStrength();

    if (strength < 40) return 'strength-weak';
    if (strength < 70) return 'strength-medium';
    return 'strength-strong';
  }

  getPasswordStrengthText(): string {
    const strength = this.getPasswordStrength();

    if (strength < 40) return 'Weak';
    if (strength < 70) return 'Medium';
    return 'Strong';
  }
}
