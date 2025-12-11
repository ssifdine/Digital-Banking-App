import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators} from '@angular/forms';
import {NgIf} from '@angular/common';
import {Router} from '@angular/router';
import {CustomerService} from '../services/customer.service';
import {Customer} from '../model/customer.model';

@Component({
  selector: 'app-update-customer',
  imports: [
    FormsModule,
    NgIf,
    ReactiveFormsModule
  ],
  templateUrl: './update-customer.html',
  styleUrl: './update-customer.css'
})
export class UpdateCustomer implements OnInit {

  customer: Customer;

  updateCustomerFormGroup! : FormGroup;

  constructor(private fb: FormBuilder, private router: Router, private customerService: CustomerService) {
    const navigation = this.router.getCurrentNavigation();
    this.customer = navigation?.extras?.state as Customer;
  }

  ngOnInit(): void {
    this.updateCustomerFormGroup=this.fb.group({
      name : this.fb.control(null,[Validators.required, Validators.minLength(4)]),
      email : this.fb.control(null,[Validators.required, Validators.email]),
    })

    // 3. Si l'objet existe, le charger dans le formulaire
    if (this.customer) {
      this.updateCustomerFormGroup.patchValue({
        name: this.customer.name,
        email: this.customer.email
      });
    } else {
      // 🔁 Optionnel : rediriger ou recharger depuis l'API avec l'ID
      console.warn("Aucun client passé via navigation");
    }
  }


  handleUpdateCustomer() {
    if (this.updateCustomerFormGroup.valid) {
      const updatedCustomer = {
        ...this.customer,
        ...this.updateCustomerFormGroup.value
      };
      // appel au service pour update
      console.log("Customer à mettre à jour :", updatedCustomer);
      this.customerService.updateCustomers(updatedCustomer.id, updatedCustomer).subscribe({
        next: (result) => {
          alert("Customer has been successfully updated!");
          // this.newCustomerFormGroup.reset();
          this.router.navigateByUrl("/admin/customers");
        },
        error: (error) => {
          console.log(error);
        }
      })
    }
  }
}
