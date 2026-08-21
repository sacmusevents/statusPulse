import { Component, OnInit, OnDestroy, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FirebaseService } from '../../services/firebase.service';
import { SignalColor } from '../../models/session.model';

/**
 * Angular Component: LightDisplayComponent
 * ----------------------------------------
 * Learning Point:
 * Demonstrates:
 * 1. Angular Router parameters (`ActivatedRoute`).
 * 2. Component Lifecycle Hooks: `ngOnInit` (on component load) & `ngOnDestroy` (on component tear-down).
 * 3. Cleaning up subscriptions to avoid memory leaks.
 */
@Component({
  selector: 'app-light-display',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './light-display.component.html',
  styleUrl: './light-display.component.css'
})
export class LightDisplayComponent implements OnInit, OnDestroy {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private firebaseService = inject(FirebaseService);

  // Reactive state signals
  sessionId = signal<string>('');
  sessionTitle = signal<string>('Connected to Session');
  currentColor = signal<SignalColor>('green');

  private unsubscribeSignal?: () => void;

  ngOnInit(): void {
    // Read route parameter (:id from /light/:id)
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.sessionId.set(id);

      // Read optional state passed via router
      const state = history.state;
      if (state?.title) {
        this.sessionTitle.set(state.title);
      }

      // TODO (Step 4): Connect to real-time updates via FirebaseService
      this.unsubscribeSignal = this.firebaseService.subscribeToSignal(id, (newColor) => {
        this.currentColor.set(newColor);
      });
    }
  }

  ngOnDestroy(): void {
    // Clean up Firebase listener when navigating away
    if (this.unsubscribeSignal) {
      this.unsubscribeSignal();
    }
  }

  goBack(): void {
    this.router.navigate(['/']);
  }
}
