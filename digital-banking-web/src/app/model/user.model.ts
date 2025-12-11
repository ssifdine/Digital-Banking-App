export interface UserRequestDTO {
  id?: number;
  username?: string;
  password?: string;
  email?: string;
  roles?: string[];
}

export interface UserResponseDTO {
  id: number;
  username: string;
  email: string;
  roles: string[];
  createdBy?: string;
  createdDate?: string;
  lastModifiedBy?: string;
  lastModifiedDate?: string;
}

// reset-password-admin.dto.ts
export interface ResetPasswordAdminDTO {
  newPassword: string;
}

export interface ChangePasswordDTO {
  oldPassword: string;
  newPassword: string;
}

