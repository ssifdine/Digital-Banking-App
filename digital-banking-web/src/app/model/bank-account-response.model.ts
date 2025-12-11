export interface BankAccountResponse {
  type: string;
  id: string;
  balance: number;
  createdAt: string;
  status: string;
  customerDTO: {
    id: number;
    name: string;
    email: string;
  };
}

export interface CurrentBankAccountResponse extends BankAccountResponse {
  overdraft: number;
}

export interface SavingBankAccountResponse extends BankAccountResponse {
  interestRate: number;
}
