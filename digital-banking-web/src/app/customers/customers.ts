import {Component, OnInit} from '@angular/core';
import {HttpClient, HttpClientModule} from '@angular/common/http';
import {AsyncPipe, JsonPipe, NgFor, NgIf} from '@angular/common';
import {CustomerService} from '../services/customer.service';
import {catchError, map, Observable, throwError} from 'rxjs';
import {Customer} from '../model/customer.model';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {Router} from '@angular/router';
import {AuthService} from '../services/auth-service';
import {Page} from '../model/page.mode';

@Component({
  selector: 'app-customers',
  imports: [
    NgIf,
    NgFor,
    HttpClientModule,
    AsyncPipe,
    ReactiveFormsModule
  ],
  templateUrl: './customers.html',
  styleUrl: './customers.css',
})
export class Customers implements OnInit {

  lastKeyword: string = '';

  customers!: Observable<Array<Customer>>;
  errorMessage!: string;
  searchFormGroup: FormGroup | undefined;

  // Propriétés pour la pagination
  isLoading: boolean = false;
  selectedCustomerId: number | null = null;
  currentPage: number = 0; // Spring Boot utilise 0 comme première page
  pageSize: number = 10;
  totalCustomers: number = 0;
  totalPages: number = 0;
  sortColumn: string = '';
  sortDirection: 'asc' | 'desc' = 'asc';

  // Données de la page actuelle
  customerPage: Page<Customer> | null = null;
  customersData: Customer[] = [];

  constructor(
    public authService: AuthService,
    private customerservice: CustomerService,
    private fb: FormBuilder,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.searchFormGroup = this.fb.group({
      keyword: this.fb.control(""),
    });
    this.handleSearchCustomers();
  }

  handleSearchCustomers() {
    this.isLoading = true;
    this.errorMessage = '';

    let kw = '';
    if (this.searchFormGroup) {
      const keywordControl = this.searchFormGroup.get('keyword');
      kw = keywordControl ? keywordControl.value || '' : '';
    }

    // Remettre à zéro la page si le mot clé a changé
    if (this.lastKeyword !== kw) {
      this.currentPage = 0;
      this.lastKeyword = kw;
    }

    this.customerservice.searchCustomersPage(kw, this.currentPage, this.pageSize)
      .pipe(
        map((pageData: Page<Customer>) => {
          // Si la page est trop haute, on revient à 0
          if (pageData.content.length === 0 && pageData.totalElements > 0 && this.currentPage > 0) {
            this.currentPage = 0;
            this.handleSearchCustomers();
            return [];
          }
          this.customerPage = pageData;
          this.totalCustomers = pageData.totalElements;
          this.totalPages = pageData.totalPages;
          this.customersData = this.applySorting(pageData.content);
          this.isLoading = false;
          return this.customersData;
        }),
        catchError(err => {
          this.errorMessage = err.message || 'Erreur lors du chargement des clients';
          this.isLoading = false;
          return throwError(err);
        })
      )
      .subscribe(data => {
        this.customers = new Observable(observer => {
          observer.next(data);
          observer.complete();
        });
      });
  }

  handleDeleteCustomer(c: Customer) {
    const confirmMessage = `Êtes-vous sûr de vouloir supprimer le client ${c.name} ?`;
    let conf = confirm(confirmMessage);
    if (!conf) return;

    this.customerservice.deleteCustomer(c.id).subscribe({
      next: (resp) => {
        // Recharger la page actuelle après suppression
        this.handleSearchCustomers();
        this.showSuccessMessage(`Client ${c.name} supprimé avec succès`);
      },
      error: err => {
        console.error('Erreur lors de la suppression:', err);
        this.errorMessage = 'Erreur lors de la suppression du client';
      }
    });
  }

  handleCustomerAccounts(customer: Customer) {
    this.router.navigateByUrl("/admin/customer-accounts/" + customer.id, {state: customer});
  }

  handleUpdateCustomer(c: Customer) {
    this.router.navigateByUrl("/admin/update-customer", {state: c});
  }

  handleAddCustomer() {
    this.router.navigateByUrl("/admin/new-customer");
  }

  handleExportCustomers() {
    console.log('Export des clients...');
    // Logique d'export
  }

  handleClearSearch() {
    if (this.searchFormGroup) {
      const keywordControl = this.searchFormGroup.get('keyword');
      if (keywordControl) {
        keywordControl.setValue('');
      }
    }
    this.currentPage = 0; // Retour à la première page
    this.handleSearchCustomers();
  }

  handleRetryLoad() {
    this.errorMessage = '';
    this.handleSearchCustomers();
  }

  handleSort(column: string) {
    if (this.sortColumn === column) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortColumn = column;
      this.sortDirection = 'asc';
    }
    this.currentPage = 0; // Retour à la première page lors du tri
    this.handleSearchCustomers();
  }

  handleViewCustomerDetails(customer: Customer) {
    this.selectedCustomerId = customer.id;
    this.router.navigateByUrl("/admin/customer-details/" + customer.id, {state: customer});
  }

  handleCustomerHistory(customer: Customer) {
    this.router.navigateByUrl("/admin/customer-history/" + customer.id, {state: customer});
  }

  // Méthodes utilitaires

  getInitials(name: string): string {
    if (!name) return 'N/A';
    const names = name.split(' ');
    return names.length > 1
      ? (names[0][0] + names[1][0]).toUpperCase()
      : name.substring(0, 2).toUpperCase();
  }

  getActiveCustomersCount(): number {
    return Math.floor(this.totalCustomers * 0.8);
  }

  getStatusBadgeClass(status?: string): string {
    switch (status?.toLowerCase()) {
      case 'active':
      case 'actif':
        return 'bg-success';
      case 'inactive':
      case 'inactif':
        return 'bg-secondary';
      case 'pending':
      case 'en_attente':
        return 'bg-warning';
      case 'blocked':
      case 'bloque':
        return 'bg-danger';
      default:
        return 'bg-primary';
    }
  }

  getStatusLabel(status?: string): string {
    switch (status?.toLowerCase()) {
      case 'active':
        return 'Actif';
      case 'inactive':
        return 'Inactif';
      case 'pending':
        return 'En attente';
      case 'blocked':
        return 'Bloqué';
      default:
        return status || 'Actif';
    }
  }

  getDisplayRange(): {start: number, end: number} {
    const start = this.currentPage * this.pageSize + 1;
    const end = Math.min(start + this.customersData.length - 1, this.totalCustomers);
    return {start, end};
  }

  trackByCustomerId(index: number, customer: Customer): number {
    return customer.id;
  }

  getTotalPages(): number {
    return this.totalPages;
  }

  goToPage(page: number): void {
    if (page >= 0 && page < this.totalPages && page !== this.currentPage) {
      this.currentPage = page;
      this.handleSearchCustomers();
    }
  }

  goToFirstPage(): void {
    this.goToPage(0);
  }

  goToLastPage(): void {
    this.goToPage(this.totalPages - 1);
  }

  goToPreviousPage(): void {
    if (this.currentPage > 0) {
      this.goToPage(this.currentPage - 1);
    }
  }

  goToNextPage(): void {
    if (this.currentPage < this.totalPages - 1) {
      this.goToPage(this.currentPage + 1);
    }
  }

  getVisiblePages(): number[] {
    const maxVisible = 5;
    let startPage = Math.max(0, this.currentPage - Math.floor(maxVisible / 2));
    let endPage = Math.min(this.totalPages - 1, startPage + maxVisible - 1);

    if (endPage - startPage + 1 < maxVisible) {
      startPage = Math.max(0, endPage - maxVisible + 1);
    }

    const pages: number[] = [];
    for (let i = startPage; i <= endPage; i++) {
      pages.push(i);
    }
    return pages;
  }

  private applySorting(data: Customer[]): Customer[] {
    if (!this.sortColumn) return data;

    return [...data].sort((a, b) => {
      let aValue: any = a[this.sortColumn as keyof Customer];
      let bValue: any = b[this.sortColumn as keyof Customer];

      if (typeof aValue === 'string') aValue = aValue.toLowerCase();
      if (typeof bValue === 'string') bValue = bValue.toLowerCase();

      if (aValue < bValue) return this.sortDirection === 'asc' ? -1 : 1;
      if (aValue > bValue) return this.sortDirection === 'asc' ? 1 : -1;
      return 0;
    });
  }

  private showSuccessMessage(message: string) {
    console.log('Succès:', message);
  }

  isAdminWithoutSearch() {
    return this.authService.roles?.includes('ADMIN') && !this.searchFormGroup?.get('keyword')?.value;
  }

  // Méthodes pour changer la taille de page
  changePageSize(newSize: number) {
    this.pageSize = newSize;
    this.currentPage = 0; // Retour à la première page
    this.handleSearchCustomers();
  }
}
