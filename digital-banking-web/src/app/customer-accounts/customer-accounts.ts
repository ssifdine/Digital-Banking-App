import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Customer } from '../model/customer.model';
import { AccountDetails } from '../model/account.model';
import { CustomerService } from '../services/customer.service';
import { DatePipe, DecimalPipe, NgClass, NgIf, NgFor, AsyncPipe } from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule} from '@angular/forms';
import {ToastService} from '../services/toast-service';
import {AuthService} from '../services/auth-service';

@Component({
  selector: 'app-customer-accounts',
  standalone: true,
  imports: [
    NgFor,
    DecimalPipe,
    ReactiveFormsModule,
    NgIf,
  ],
  templateUrl: './customer-accounts.html',
  styleUrls: ['./customer-accounts.css']
})
export class CustomerAccounts implements OnInit {

  customerId!: number;
  customer!: Customer;
  accounts: any[] = [];
  userForm!: FormGroup;

  constructor(
    public authService: AuthService,
    private route: ActivatedRoute,
    private customerService: CustomerService,
    private fb: FormBuilder,
    private router: Router,
    private toast: ToastService,
  ) {}

  navigateToNewAccount() {
    this.router.navigate([
      '/admin/new-account',
      this.customerId
    ]);
  }

  ngOnInit(): void {
    this.customerId = this.route.snapshot.params['id'];

    this.userForm = this.fb.group({
      name: [''],
      email: [''],
    });

    this.loadCustomer();
    this.loadAccounts();
  }

  loadCustomer(): void {
    this.customerService.getCustomer(this.customerId).subscribe({
      next: (cust) => {
        this.customer = cust;
        this.userForm.patchValue({
          name: cust.name,
          email: cust.email,
        });
      },
      error: (err) => {
        console.error('Error loading customer:', err);
        this.toast.showError(err.error || 'Failed to load customer data');
      },
    });
  }

  loadAccounts(): void {
    this.customerService.getCustomerAccounts(this.customerId).subscribe({
      next: (accs) =>{
        (this.accounts = accs)
        console.log("account loaded :",this.accounts);
      },
      error: (err) => {
        console.error('Error loading accounts:', err);
        this.toast.showError(err.error || 'Failed to load customer accounts');
      },
    });
  }

  handleUpdateUser(): void {
    const updatedCustomer: Customer = {
      id: this.customerId,
      name: this.userForm.value.name,
      email: this.userForm.value.email,
    };

    this.customerService.updateCustomers(updatedCustomer.id,updatedCustomer).subscribe({
      next: () => {
        this.toast.showInfo('Customer updated successfully.');
        this.loadCustomer();
      },
      error: (err) => {
        console.error('Update failed:', err);
        this.toast.showError(err.error || 'Failed to update customer.');
      },
    });
  }

  goToAccount(id: string) {
    console.log("goToAccount :",id);
    this.router.navigate(['/admin/accounts'], {
      state: { id },
    });
  }
}
