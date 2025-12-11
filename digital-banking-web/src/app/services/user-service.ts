import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../environments/environment';
import {ChangePasswordDTO, ResetPasswordAdminDTO, UserRequestDTO, UserResponseDTO} from '../model/user.model';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class UserService {

  constructor(private http: HttpClient) { }

  createUser(user: any) {
    return this.http.post(`${environment.backendHost}/api/users/createUser`, user);
  }

  // GET BY ID
  getUserById(id: number): Observable<UserResponseDTO> {
    return this.http.get<UserResponseDTO>(`${environment.backendHost}/api/users/${id}`);
  }

  // GET BY USERNAME
  getUserByUsername(username: string): Observable<UserResponseDTO> {
    return this.http.get<UserResponseDTO>(`${environment.backendHost}/api/users/username/${username}`);
  }

  // GET ALL USERS
  getAllUsers(): Observable<UserResponseDTO[]> {
    return this.http.get<UserResponseDTO[]>(`${environment.backendHost}/api/users/all`);
  }

  // UPDATE
  updateUser(id: number, user: UserRequestDTO): Observable<UserResponseDTO> {
    return this.http.put<UserResponseDTO>(`${environment.backendHost}/api/users/${id}`, user);
  }

  // DELETE
  deleteUser(id: number): Observable<void> {
    return this.http.delete<void>(`${environment.backendHost}/api/users/${id}`);
  }

  // resetPasswordAdmin
  resetPasswordAdmin(id: number, resetPasswordAdminDTO: ResetPasswordAdminDTO) {
    return this.http.patch(
      `${environment.backendHost}/api/users/admin/${id}/reset-password`,
        resetPasswordAdminDTO,
      { responseType: 'text' }
    );
  }

  changePassword(data : ChangePasswordDTO) {
    return this.http.patch(
      `${environment.backendHost}/api/users/change-password`,
      data,
      {
        responseType: 'text',
      },
    );
  }


}
