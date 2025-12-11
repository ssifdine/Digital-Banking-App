import { Injectable } from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../environments/environment';
import {AccountDetails, BeneficaireDTO, BeneficaireResponseDTO, BeneficiareHistoryDTO} from '../model/account.model';
import {BankAccountRequest} from '../model/bank-account-request.model';
import {BankAccountResponse} from '../model/bank-account-response.model';

@Injectable({
  providedIn: 'root'
})
export class AccountsService {

  constructor(private http: HttpClient)  { }

  public getAccount(accountId: string, page:number, size:number): Observable<AccountDetails> {
    return this.http.get<AccountDetails>(environment.backendHost+"/accounts/"+accountId+"/pageOperations?page="+page+"&size="+size);
  }

  public debit(accountId : string, amount : number, description:string){
    let data={accountId : accountId, amount : amount, description : description}
    return this.http.post(environment.backendHost+"/accounts/debit",data);
  }
  public credit(accountId : string, amount : number, description:string){
    let data={accountId : accountId, amount : amount, description : description}
    return this.http.post(environment.backendHost+"/accounts/credit",data);
  }
  public transfer(accountSource: string,accountDestination: string, amount : number, description:string){
    let data={accountSource, accountDestination, amount, description }
    return this.http.post(environment.backendHost+"/accounts/transfer",data);
  }

  public createAccount(request: BankAccountRequest): Observable<BankAccountResponse> {
    return this.http.post<BankAccountResponse>(environment.backendHost+"/accounts/createAccount", request);
  }

  public getLatestAccount(){
    return this.http.get<BankAccountResponse>(environment.backendHost + "/accounts/latest");
  }

  updateInterestRate(id: string, newRate: number): Observable<string> {
    return this.http.put(
      `${environment.backendHost}/accounts/${id}/interest-rate`,
      newRate,
      { responseType: 'text' }
    );
  }

  updateOverdraftLimit(id: string, newLimit: number): Observable<string> {
    return this.http.put(
      `${environment.backendHost}/accounts/${id}/overdraft-limit`,
      newLimit,
      { responseType: 'text' } // important pour recevoir le message String
    );
  }

  public searchOperations(
    accountId: string,
    params: {
      startDate?: string;
      endDate?: string;
      minAmount?: number;
      maxAmount?: number;
      page: number;
      size: number;
    }
  ): Observable<AccountDetails> {
    let queryParams = new HttpParams()
      .set('page', params.page.toString())
      .set('size', params.size.toString());

    if (params.startDate) {
      queryParams = queryParams.set('startDate', params.startDate);
    }
    if (params.endDate) {
      queryParams = queryParams.set('endDate', params.endDate);
    }
    if (params.minAmount !== null && params.minAmount !== undefined) {
      queryParams = queryParams.set('minAmount', params.minAmount.toString());
    }
    if (params.maxAmount !== null && params.maxAmount !== undefined) {
      queryParams = queryParams.set('maxAmount', params.maxAmount.toString());
    }

    return this.http.get<AccountDetails>(
      `${environment.backendHost}/accounts/${accountId}/operations/search`,
      { params: queryParams }
    );
  }

  public updateStatus(accountId: string, newStatus: string): Observable<any> {
    return this.http.patch(
      `${environment.backendHost}/accounts/${accountId}/status`,
      { status: newStatus },
      { responseType: 'text' as 'json' },
    );
  }

  getBeneficiairesByAccountId(accountId: string): Observable<BeneficiareHistoryDTO> {
    return this.http.get<BeneficiareHistoryDTO>(`${environment.backendHost}/accounts/${accountId}/beneficaires`);
  }

  addBeneficaire(accountId: string, dto: BeneficaireDTO): Observable<any> {
    return this.http.post<any>(`${environment.backendHost}/accounts/${accountId}/addBeneficaire`, dto);
  }

  updateBeneficaire(accountId: string, beneficaireDTO: BeneficaireDTO): Observable<BeneficaireResponseDTO> {
    return this.http.put<BeneficaireResponseDTO>(
      `${environment.backendHost}/accounts/${accountId}/updateBeneficaire`,
      beneficaireDTO
    );
  }


  deleteBeneficiaire(accountId: string, benefId: number) {
    return this.http.delete(
      `${environment.backendHost}/accounts/${accountId}/beneficiaires/${benefId}`,
      { responseType: 'text' }
    );
  }








}
