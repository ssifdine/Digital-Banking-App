import { Routes } from '@angular/router';
import {Customers} from './customers/customers';
import {Accounts} from './accounts/accounts';
import {NewCustomer} from './new-customer/new-customer';
import {CustomerAccounts} from './customer-accounts/customer-accounts';
import {Login} from './login/login';
import {AdminTemplate} from './admin-template/admin-template';
import {AuthenticationGuard} from './guards/authentication-guard';
import {NotAuthorized} from './not-authorized/not-authorized';
import {AuthorizationGuard} from './guards/authorization-guard';
import {UpdateCustomer} from './update-customer/update-customer';
import {NewAccount} from './new-account/new-account';
import {NewUser} from './new-user/new-user';
import {ManageUser} from './manage-user/manage-user';
import {ChangePassword} from './change-password/change-password';
import {Dashboard} from './dashboard/dashboard';

export const routes: Routes = [
  { path: 'login',component : Login },
  { path: "",redirectTo: "login",pathMatch: "full" },
  { path: "admin",component : AdminTemplate, canActivate : [AuthenticationGuard],
    children: [
      { path: "customers",component : Customers },
      { path: "accounts",component : Accounts },
      { path: "dashboard", component : Dashboard },
      { path: "new-customer",component : NewCustomer, canActivate: [AuthorizationGuard], data : {role : "ADMIN"}},
      { path: "new-account/:id" ,component : NewAccount , canActivate: [AuthorizationGuard], data : {role : "ADMIN"} },
      { path: "update-customer",component : UpdateCustomer, canActivate: [AuthorizationGuard], data : {role : "ADMIN"} },
      { path: "new-user",component: NewUser, canActivate: [AuthorizationGuard], data : {role : "ADMIN"}},
      { path: "manage-users", component: ManageUser, canActivate: [AuthorizationGuard], data : {role : "ADMIN"}},
      { path: "change-password", component: ChangePassword},
      { path: "customer-accounts/:id",component: CustomerAccounts },
      { path: "notAuthorized",component: NotAuthorized },
    ]
  },
];
