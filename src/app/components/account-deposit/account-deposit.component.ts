import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-account-deposit',
  templateUrl: './account-deposit.component.html',
  styleUrls: ['./account-deposit.component.css']
})
export class AccountDepositComponent {
  depositForm: FormGroup;
  loading = false;
  success = false;
  error = '';

  constructor(
    private fb: FormBuilder,
    private apiService: ApiService,
    private router: Router
  ) {
    this.depositForm = this.fb.group({
      amount: ['', [Validators.required, Validators.min(1)]],
      reference: ['', [Validators.required]]
    });
  }

  onSubmit(): void {
    if (this.depositForm.valid) {
      this.loading = true;
      const { amount, reference } = this.depositForm.value;

      this.apiService.depositFunds(parseFloat(amount), reference).subscribe({
        next: (response) => {
          if (response.success) {
            this.success = true;
            this.error = '';
            this.loading = false;
            setTimeout(() => {
              this.success = false;
              this.router.navigate(['/dashboard']);
            }, 2000);
          }
        },
        error: (error) => {
          this.error = error.message || 'Deposit failed';
          this.success = false;
          this.loading = false;
        }
      });
    }
  }

  navigateTo(route: string): void {
    window.location.href = '/' + route;
  }

  logout(): void {
    this.authService.logout();
    window.location.href = '/login';
  }
}
