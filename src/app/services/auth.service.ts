import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { environment } from '../../environments/environment';

export interface User {
  id: number;
  name: string;
  email: string;
  phoneNumber: string;
  countryCode: string;
  address: string;
  account: any;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private baseUrl = environment.apiUrl;
  private currentUserSubject: BehaviorSubject<User | null>;
  public currentUser: User | null;

  constructor(private http: HttpClient) {
    this.currentUserSubject = new BehaviorSubject<User | null>(JSON.parse(localStorage.getItem('currentUser') || 'null'));
    this.currentUser = this.currentUserSubject.value;
  }

  private getHeaders(): HttpHeaders {
    return new HttpHeaders({
      'Content-Type': 'application/json'
    });
  }

  registerUser(user: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/api/auth/register`, user, { headers: this.getHeaders() });
  }

  login(email: string, password: string): Observable<any> {
    return this.http.post(`${this.baseUrl}/api/auth/login`, { email, password }, { headers: this.getHeaders() });
  }

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('currentUser');
    this.currentUserSubject.next(null);
    this.currentUser = null;
  }

  isLoggedIn(): boolean {
    return !!localStorage.getItem('token');
  }

  getCurrentUser(): User | null {
    return this.currentUser;
  }

  getUserDetails(): Observable<any> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    return this.http.get(`${this.baseUrl}/api/user/profile`, { headers });
  }

  updateUserProfile(user: any): Observable<any> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
    return this.http.put(`${this.baseUrl}/api/user/profile`, user, { headers });
  }

  generateOTP(phoneNumber: string, email: string): Observable<any> {
    return this.http.post(`${this.baseUrl}/api/auth/send-otp`, { phoneNumber, email }, { headers: this.getHeaders() });
  }

  verifyOTP(phoneNumber: string, otp: string, email: string): Observable<any> {
    return this.http.post(`${this.baseUrl}/api/auth/verify-otp`, { phoneNumber, otp, email }, { headers: this.getHeaders() });
  }

  resetPassword(token: string, newPassword: string): Observable<any> {
    return this.http.post(`${this.baseUrl}/api/auth/reset-password`, { token, newPassword }, { headers: this.getHeaders() });
  }

  sendLoginNotification(phoneNumber: string): Observable<any> {
    return this.http.post(`${this.baseUrl}/api/auth/send-login-notification`, { phoneNumber }, { headers: this.getHeaders() });
  }

  generateOtp(phoneNumber: string): Observable<any> {
    return this.http.post(`${this.baseUrl}/api/auth/generate-otp`, { phoneNumber }, { headers: this.getHeaders() });
  }

  verifyOtpAndLogin(phoneNumber: string, otp: string): Observable<any> {
    return this.http.post(`${this.baseUrl}/api/auth/verify-otp-and-login`, { phoneNumber, otp }, { headers: this.getHeaders() });
  }
}
