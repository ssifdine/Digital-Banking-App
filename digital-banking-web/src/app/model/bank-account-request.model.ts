export interface BankAccountRequest {
  type: 'CURRENT' | 'SAVING';
  initialBalance: number;
  overdraft?: number;
  interestRate?: number;
  customerId: number;
}
