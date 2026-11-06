import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface Transaction {
  id: number;
  sourceAccount: string;
  targetAccount: string;
  amount: number;
  type: string;
  status: string;
  timestamp: string;
}

export interface Account {
  id: number;
  accountNumber: string;
  accountType: string;
  balance: number;
  branch: string;
  ifscCode: string;
  pinCreated: boolean;
}

export interface User {
  id: number;
  name: string;
  email: string;
  phoneNumber: string;
  countryCode: string;
  address: string;
  account: Account;
}

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private baseUrl = environment.apiUrl;

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': token ? `Bearer ${token}` : ''
    });
  }

  checkPinCreated(accountId: number): Observable<any> {
    return this.http.post(`${this.baseUrl}/api/account/check-pin-created`, { accountId }, { headers: this.getHeaders() });
  }

  createPin(accountId: number, pin: string): Observable<any> {
    return this.http.post(`${this.baseUrl}/api/account/create-pin`, { accountId, pin }, { headers: this.getHeaders() });
  }

  updatePin(accountId: number, oldPin: string, newPin: string): Observable<any> {
    return this.http.post(`${this.baseUrl}/api/account/update-pin`, { accountId, oldPin, newPin }, { headers: this.getHeaders() });
  }

  withdraw(accountId: number, amount: number): Observable<any> {
    return this.http.post(`${this.baseUrl}/api/account/withdraw`, { accountId, amount }, { headers: this.getHeaders() });
  }

  deposit(accountId: number, amount: number): Observable<any> {
    return this.http.post(`${this.baseUrl}/api/account/deposit`, { accountId, amount }, { headers: this.getHeaders() });
  }

  fundTransfer(sourceAccountId: number, targetAccountId: number, amount: number): Observable<any> {
    return this.http.post(`${this.baseUrl}/api/account/fund-transfer`, { sourceAccountId, targetAccountId, amount }, { headers: this.getHeaders() });
  }

  getTransactions(accountId: number): Observable<any> {
    return this.http.get(`${this.baseUrl}/api/account/transactions`, { headers: this.getHeaders() });
  }

  getAccountDetails(accountId: number): Observable<any> {
    return this.http.get(`${this.baseUrl}/api/account/details`, { headers: this.getHeaders() });
  }

  validatePin(accountId: number, pin: string): Observable<any> {
    return this.http.post(`${this.baseUrl}/api/account/validate-pin`, { accountId, pin }, { headers: this.getHeaders() });
  }

  validateAmount(amount: number): Observable<any> {
    return this.http.post(`${this.baseUrl}/api/account/validate-amount`, { amount }, { headers: this.getHeaders() });
  }

  sendStatement(accountId: number): Observable<any> {
    return this.http.post(`${this.baseUrl}/api/account/send-statement`, { accountId }, { headers: this.getHeaders() });
  }
}
