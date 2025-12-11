import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../environments/environment';
import {DashboardStats} from '../model/dashboard.model';

@Injectable({
  providedIn: 'root'
})
export class DashboardService {

  constructor(private http: HttpClient) { }

  getAccountsStats(): Observable<DashboardStats> {
    return this.http.get<DashboardStats>(
      environment.backendHost + '/api/dashboard',
    );
  }

  getOperationsByType() {
    return this.http.get(
      environment.backendHost + '/api/dashboard/operationsByType',
    );
  }

  getMostActiveCustomers() {
    return this.http.get<{ [key: string]: number }>(
      environment.backendHost + '/api/dashboard/most-active-customers',
    );
  }
}
