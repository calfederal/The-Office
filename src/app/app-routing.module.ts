import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { AccountPinComponent } from './components/account-pin/account-pin.component';
import { AccountDetailsComponent } from './components/account-details/account-details.component';
import { FundTransferComponent } from './components/fund-transfer/fund-transfer.component';
import { AccountTransactionsComponent } from './components/account-transactions/account-transactions.component';
import { AccountDepositComponent } from './components/account-deposit/account-deposit.component';
import { AccountWithdrawComponent } from './components/account-withdraw/account-withdraw.component';

const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'dashboard', component: DashboardComponent },
  { path: 'account-pin', component: AccountPinComponent },
  { path: 'account-details', component: AccountDetailsComponent },
  { path: 'fund-transfer', component: FundTransferComponent },
  { path: 'account-transactions', component: AccountTransactionsComponent },
  { path: 'account-deposit', component: AccountDepositComponent },
  { path: 'account-withdraw', component: AccountWithdrawComponent },
  { path: '**', redirectTo: '/login' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
