import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { ReactiveFormsModule } from '@angular/forms';
import { HTTP_INTERCEPTORS } from '@angular/common/http';

import { AppComponent } from './app.component';
import { AppRoutingModule } from './app-routing.module';
import { AuthInterceptor } from './services/auth.interceptor';
import { LoginComponent } from './components/login/login.component';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { AccountPinComponent } from './components/account-pin/account-pin.component';
import { AccountDetailsComponent } from './components/account-details/account-details.component';
import { FundTransferComponent } from './components/fund-transfer/fund-transfer.component';
import { AccountTransactionsComponent } from './components/account-transactions/account-transactions.component';
import { AccountDepositComponent } from './components/account-deposit/account-deposit.component';
import { AccountWithdrawComponent } from './components/account-withdraw/account-withdraw.component';

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    DashboardComponent,
    AccountPinComponent,
    AccountDetailsComponent,
    FundTransferComponent,
    AccountTransactionsComponent,
    AccountDepositComponent,
    AccountWithdrawComponent
  ],
  imports: [
    BrowserModule,
    ReactiveFormsModule,
    AppRoutingModule
  ],
  providers: [
    { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true }
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
