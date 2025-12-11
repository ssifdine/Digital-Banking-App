import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import {ToastMessage, ToastService} from '../services/toast-service';

@Component({
  selector: 'app-toast',
  imports: [CommonModule],
  templateUrl: './toast.html',
  styleUrl: './toast.css',
})
export class Toast implements OnInit {
  toasts: ToastMessage[] = [];

  constructor(private toastService: ToastService) {}

  ngOnInit(): void {
    this.toastService.toast$.subscribe((msg) => {
      this.toasts.push(msg);
      setTimeout(() => {
        this.toasts.shift();
      }, 3000);
    });
  }
}
