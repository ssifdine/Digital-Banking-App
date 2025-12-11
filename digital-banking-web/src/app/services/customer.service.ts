import { Injectable } from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Customer} from '../model/customer.model';
import {environment} from '../../environments/environment';
import {AccountDetails} from '../model/account.model';
import {Page} from '../model/page.mode';

@Injectable({
  providedIn: 'root'
})
export class CustomerService {


  constructor(private http: HttpClient) { }

  public getCustomers(page: number = 0, size: number = 10): Observable<Page<Customer>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<Page<Customer>>(`${environment.backendHost}/customers`, { params });
  }

  searchCustomersPage(keyword: string, page: number, size: number): Observable<Page<Customer>> {
    let params = new HttpParams()
      .set('keyword', keyword || '')
      .set('page', page.toString())
      .set('size', size.toString());
    console.log("keyword", keyword);

    return this.http.get<Page<Customer>>(environment.backendHost+"/customers/searchPage", { params });
  }

  public searchCustomers(keyword : string): Observable<Array<Customer>> {
    return this.http.get<Array<Customer>>(environment.backendHost+"/customers/search?keyword=" + keyword);
  }

  public saveCustomers(customer : Customer): Observable<Customer> {
    return this.http.post<Customer>(environment.backendHost+"/customers" , customer);
  }

  public deleteCustomer(id : number){
    return this.http.delete(environment.backendHost+"/customers/"+id);
  }

  public updateCustomers(id: number,customer : Customer) : Observable<Customer> {
    return this.http.put<Customer>(environment.backendHost+"/customers/"+id,customer);
  }

  public getCustomer(id: number): Observable<Customer> {
    return this.http.get<Customer>(
      `${environment.backendHost}/customers/${id}`,
    );
  }

  public getCustomerAccounts(id: number): Observable<AccountDetails[]> {
    return this.http.get<AccountDetails[]>(`${environment.backendHost}/customers/${id}/accounts`);
  }
}
