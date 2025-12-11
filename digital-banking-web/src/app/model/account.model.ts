export interface AccountDetails {
  accountId: string;
  balance: number;
  dateCreated: string;
  customerId: string;
  status: string;
  type: string;
  interestRate?: number;
  overdraftLimit?: number;
  accountOperationDTOS: Operation[];
  currentPage: number;
  pageSize: number;
  totalPages: number;
  beneficiaries: BeneficaireDTO[];
}

export interface Operation {
  id: number;
  operationDate: Date;
  amount: number;
  type: string;
  description: string;
  cancelled?: boolean;
}

export interface BeneficaireDTO {
  id?: number;
  nom: string;
  compteDestinataire: string;
}

export interface BeneficaireResponseDTO {
  id: number;
  nom: string;
  compteDestinataire: string;
}

export interface BeneficiareHistoryDTO {
  accountId: string;
  status: string; // enum AccountStatus → côté Angular on utilise string
  beneficiaries: BeneficaireResponseDTO[];
}




