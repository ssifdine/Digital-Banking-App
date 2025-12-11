import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators} from '@angular/forms';
import {AccountsService} from '../services/accounts-service';
import {catchError, Observable, of, tap, throwError} from 'rxjs';
import {AccountDetails} from '../model/account.model';
import {
  AsyncPipe,
  DatePipe,
  DecimalPipe,
  JsonPipe,
  NgClass,
  NgFor,
  NgIf,
  SlicePipe,
  TitleCasePipe, UpperCasePipe
} from '@angular/common';
import {AuthService} from '../services/auth-service';
import {ToastService} from '../services/toast-service';
import {BeneficaireDTO} from '../model/account.model';

@Component({
  selector: 'app-accounts',
  imports: [
    ReactiveFormsModule,
    NgIf, NgFor,
    AsyncPipe,
    DecimalPipe, DatePipe, NgClass, FormsModule, TitleCasePipe,
  ],
  templateUrl: './accounts.html',
  styleUrl: './accounts.css'
})
export class Accounts implements OnInit {
  newOverdraftLimit: number = 0;
  newInterestRate: number = 0;

  beneficiaryForm!: FormGroup;
  showAddBeneficiaryModal: boolean = false;
  showManageBeneficiaryModal: boolean = false;

  accountFormGroup!: FormGroup;
  currentPage: number = 0;
  pageSize: number = 5;
  accountObservable!: Observable<AccountDetails>;
  opertaionFromGroup!: FormGroup;
  errorMessage!: string;
  statuses: string[] = ['ACTIVE', 'INACTIVE', 'BLOCKED', 'CLOSED', 'PENDING'];
  selectedStatus!: string;
  filterFormGroup!: FormGroup;
  accountSnapshot!: AccountDetails;

  beneficiaries: BeneficaireDTO[] = [];

  constructor(
    private fb: FormBuilder,
    private accountService: AccountsService,
    public authService: AuthService,
    private toast: ToastService,
  ) {}

  ngOnInit(): void {
    this.filterFormGroup = this.fb.group({
      startDate: [''],
      endDate: [''],
      minAmount: [''],
      maxAmount: [''],
    });

    this.accountFormGroup = this.fb.group({
      accountId: this.fb.control(''),
    });

    this.opertaionFromGroup = this.fb.group({
      operationType: this.fb.control(null),
      amount: this.fb.control(0),
      description: this.fb.control(null),
      accountDestination: this.fb.control(null),
    });

    this.beneficiaryForm = this.fb.group({
      nom: ['', Validators.required],
      compteDestinataire: ['', Validators.required],
    });

    const formValueId = this.accountFormGroup?.value?.accountId;
    const routerId = history.state?.id;

    if (routerId) {
      console.log("routerId :", routerId);
      this.accountFormGroup.patchValue({ accountId: routerId });
      console.log("FormGroup après patch :", this.accountFormGroup.value);
      this.handleSearchAccount();
    } else if (!formValueId) {
      this.accountService.getLatestAccount().subscribe({
        next: (latest) => {
          console.log("Latest account",latest.status);
          this.accountFormGroup.patchValue({ accountId: latest.id });
          this.handleSearchAccount();
        },
        error: (err) => {
          this.toast.showError(err?.error || 'Failed to load latest account');
        },
      });
    }
  }

  loadLatestAccount() {
    console.log('loadLatestAccount');
    this.accountService.getLatestAccount().subscribe({
      next: (latestAccount) => {
        this.accountFormGroup.patchValue({
          accountId: latestAccount.id
        });
        console.log("latestAccount", latestAccount.id);
        this.handleSearchAccount();
      },
      error: (err) => {
        console.error("Erreur lors du chargement du dernier compte :", err);
        this.errorMessage = "Impossible de charger le dernier compte";
      }
    });
  }

  handleSearchAccount() {
    let accountId: string = this.accountFormGroup.value.accountId;
    console.log("accountId :", accountId);

    if (!accountId || accountId.trim() === '') {
      this.errorMessage = "Veuillez saisir un ID de compte";
      return;
    }

    this.errorMessage = '';
    this.accountObservable = this.accountService.getAccount(accountId, this.currentPage, this.pageSize).pipe(
      tap(data => {
        console.log("Données du compte :", data);
        this.beneficiaries = data.beneficiaries;
      }),
      catchError(err => {
        this.errorMessage = err.message || "Erreur lors de la récupération du compte";
        console.error("Erreur :", err);
        return throwError(err);
      })
    );
  }


  gotoPage(page: number) {
    this.currentPage = page;

    // Vérifier s'il y a des filtres actifs
    const hasFilters = this.hasActiveFilters();

    if (hasFilters) {
      this.searchOperations(); // Utiliser la recherche filtrée
    } else {
      this.handleSearchAccount(); // Utiliser la recherche normale
    }
  }

  handleAccountOperation() {
    let accountId: string = this.accountFormGroup.value.accountId;
    let operationType = this.opertaionFromGroup.value.operationType;
    let amount: number = this.opertaionFromGroup.value.amount;
    let description: string = this.opertaionFromGroup.value.description;
    let accountDestination: string = this.opertaionFromGroup.value.accountDestination;

    if (operationType == 'DEBIT') {
      this.accountService.debit(accountId, amount, description).subscribe({
        next: () => {
          this.toast.showSuccess("Successfully debit");
          this.opertaionFromGroup.reset();
          this.handleSearchAccount();
        },
        error: (error) => this.handleHttpError(error)
      });
    } else if (operationType == 'CREDIT') {
      this.accountService.credit(accountId, amount, description).subscribe({
        next: () => {
          this.toast.showSuccess("Successfully credit");
          this.opertaionFromGroup.reset();
          this.handleSearchAccount();
        },
        error: (error) => this.handleHttpError(error)
      });
    } else if (operationType == 'TRANSFER') {
      this.accountService.transfer(accountId, accountDestination, amount, description).subscribe({
        next: () => {
          this.toast.showSuccess("Successfully transfer");
          this.opertaionFromGroup.reset();
          this.handleSearchAccount();
        },
        error: (error) => this.handleHttpError(error)
      });
    }
  }

  private handleHttpError(error: any) {
    console.log("Erreur HttpErrorResponse:", error);
    let errorMessage = "Une erreur est survenue";

    if (error.error && error.error.message) {
      errorMessage = error.error.message;
    } else if (error.message) {
      errorMessage = error.message;
    }

    console.log("Message d'erreur:", errorMessage);
    this.toast.showError(errorMessage);
  }

  updateInterestRate() {
    const id = this.accountSnapshot.accountId;
    const rate = this.newInterestRate;

    console.log("accountSnapshot:", this.accountSnapshot);
    console.log("id:", id);
    console.log("rate:", rate);

    this.accountService.updateInterestRate(id, rate!).subscribe({
      next: (message: string) => {
        console.log("Server response:", message);
        this.toast.showSuccess(message);
        this.handleSearchAccount();
      },
      error: (err) => {
        console.log("Error:", err);
        this.toast.showError(err?.error || 'Failed to update interest rate.');
      },
    });
  }

  updateOverdraftLimit() {
    const id = this.accountSnapshot.accountId;
    const limit = this.newOverdraftLimit;

    this.accountService.updateOverdraftLimit(id, limit!).subscribe({
      next: (message: string) => {
        this.toast.showSuccess(message);
        this.handleSearchAccount();
      },
      error: (err) => {
        this.toast.showError(err?.error || 'Failed to update overdraft limit.');
      },
    });
  }

  searchOperations() {
    const { startDate, endDate, minAmount, maxAmount } = this.filterFormGroup.value;
    const accountId = this.accountFormGroup.value.accountId;

    // Validation
    if (!accountId || accountId.trim() === '') {
      this.toast.showError('Veuillez sélectionner un compte');
      return;
    }

    // Validation des dates
    if (startDate && endDate && new Date(startDate) > new Date(endDate)) {
      this.toast.showError('La date de début doit être antérieure à la date de fin');
      return;
    }

    // Validation des montants
    if (minAmount && maxAmount && parseFloat(minAmount) > parseFloat(maxAmount)) {
      this.toast.showError('Le montant minimum doit être inférieur au montant maximum');
      return;
    }

    this.accountService
      .searchOperations(accountId, {
        startDate: startDate || undefined,
        endDate: endDate || undefined,
        minAmount: minAmount ? parseFloat(minAmount) : undefined,
        maxAmount: maxAmount ? parseFloat(maxAmount) : undefined,
        page: this.currentPage,
        size: this.pageSize,
      })
      .subscribe({
        next: (data) => {
          this.accountObservable = of(data);
          this.accountSnapshot = data;
          this.currentPage = data.currentPage;
          this.toast.showSuccess('Recherche effectuée avec succès');
        },
        error: (err) => {
          console.error('Search error:', err);
          this.toast.showError(err?.error?.message || 'La recherche a échoué. Veuillez vérifier vos critères.');
        },
      });
  }

  clearFilters() {
    this.filterFormGroup.reset();
    this.currentPage = 0;
    this.handleSearchAccount();
    this.toast.showSuccess('Filtres réinitialisés');
  }

  hasActiveFilters(): boolean {
    const filters = this.filterFormGroup.value;
    return !!(filters.startDate || filters.endDate || filters.minAmount || filters.maxAmount);
  }

  updateStatus() {
    console.log("updateStatus");
    const id = this.accountFormGroup.value.accountId;
    console.log("id:", id);
    console.log("selectedStatus:",this.selectedStatus);
    if (!id || !this.selectedStatus) return;

    this.accountService.updateStatus(id, this.selectedStatus).subscribe({
      next: () => {
        this.toast.showInfo('Status updated!');
        this.handleSearchAccount();
      },
      error: (err) => {
        this.toast.showError(err?.error || 'Failed to update account status.');
      },
    });
  }

  openAddBeneficiary() {
    this.showAddBeneficiaryModal = true;
  }

  closeAddBeneficiary() {
    this.showAddBeneficiaryModal = false;
  }

  saveBeneficiary() {
    const dto: BeneficaireDTO = {
      nom: this.beneficiaryForm.value.nom,
      compteDestinataire: this.beneficiaryForm.value.compteDestinataire
    };

    const accountId = this.accountFormGroup.get('accountId')?.value;

    this.accountService.addBeneficaire(accountId, dto).subscribe({
      next: () => {
        alert("Bénéficiaire ajouté avec succès !");
        this.closeAddBeneficiary();
        this.handleSearchAccount();
        this.beneficiaryForm = this.fb.group({
          nom: ['', Validators.required],
          compteDestinataire: ['', Validators.required],
        });
      },
      error: err => {
        console.error(err);
        alert("Erreur lors de l'ajout du bénéficiaire");
      }
    });
  }

  // Ouvrir / fermer le modal
  openManageBeneficiary() {
    this.showManageBeneficiaryModal = true;
  }

  closeManageBeneficiary() {
    this.showManageBeneficiaryModal = false;
  }

// Supprimer un bénéficiaire via le backend
  removeBeneficiary(index: number) {
    const accountId = this.accountFormGroup.get('accountId')?.value;
    const benef = this.beneficiaries[index];

    if (confirm(`Supprimer le bénéficiaire ${benef.nom} ?`)) {
      this.accountService.deleteBeneficiaire(accountId, benef.id!).subscribe({
        next: () => {
          this.toast.showSuccess(`Bénéficiaire ${benef.nom} supprimé`);
          this.beneficiaries.splice(index, 1);
        },
        error: (err) => {
          console.error(err);
          this.toast.showError('Erreur lors de la suppression');
        }
      });
    }
  }

  updateBeneficiary(index: number) {
    const accountId = this.accountFormGroup.get('accountId')?.value;
    const benef = this.beneficiaries[index];

    this.accountService.updateBeneficaire(accountId, benef).subscribe({
      next: (updated) => {
        console.log("updated" , updated);
        this.toast.showSuccess(`Bénéficiaire ${updated.nom} mis à jour`);
        this.beneficiaries[index] = { ...updated };
      },
      error: (err) => {
        console.error(err);
        this.toast.showError('Erreur lors de la mise à jour');
      }
    });
  }

}
