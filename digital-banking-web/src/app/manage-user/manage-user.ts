import {Component, OnInit} from '@angular/core';
import {UserRequestDTO, UserResponseDTO} from '../model/user.model';
import {UserService} from '../services/user-service';
import {FormsModule} from '@angular/forms';
import {CommonModule, NgClass} from '@angular/common';
import {ToastService} from '../services/toast-service';

@Component({
  selector: 'app-manage-user',
  imports: [
    FormsModule,
    CommonModule,
  ],
  templateUrl: './manage-user.html',
  styleUrl: './manage-user.css'
})
export class ManageUser implements OnInit {

  users: UserResponseDTO[] = [];
  editMode: { [key: number]: boolean } = {};
  selectedUser: UserResponseDTO | null = null;
  resetPasswordValue: string = '';

  constructor(private userService: UserService, private toast: ToastService,
  ) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.userService.getAllUsers().subscribe((res) => {
      this.users = res;
    });
  }

  enableEdit(userId: number): void {
    this.editMode[userId] = true;
  }

  saveUser(user: UserResponseDTO): void {
    this.userService.updateUser(user.id, user).subscribe(() => {
      this.editMode[user.id] = false;
      this.loadUsers();
    });
  }

  openResetPasswordModal(user: UserResponseDTO): void {
    this.selectedUser = user;
    this.resetPasswordValue = '';
  }

  submitPasswordReset(): void {
    if (!this.selectedUser || !this.resetPasswordValue) {
      return; // sécurité
    }

    this.userService
      .resetPasswordAdmin(this.selectedUser.id, { newPassword: this.resetPasswordValue })
      .subscribe({
        next: () => {
          // Optionnel : message de succès
          console.log("Password reset successfully");
          this.toast.showSuccess('Password reset successfully');

          // Reset UI
          this.selectedUser = null;
          this.resetPasswordValue = '';
        },
        error: (err) => {
          console.error(err);
        }
      });
  }


  toggleRole(user: UserResponseDTO, role: string): void {
    const index = user.roles.indexOf(role);
    if (index > -1) {
      user.roles.splice(index, 1);
    } else {
      user.roles.push(role);
    }
  }

}
