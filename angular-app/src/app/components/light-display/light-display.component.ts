import { Component, OnInit, OnDestroy, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FirebaseService } from '../../services/firebase.service';
import { SignalColor } from '../../models/session.model';

/**
 * ============================================================================
 * ANGULAR ROUTING & LIFECYCLE HOOKS EXPLAINED
 * ============================================================================
 * 
 * 1. Router Route Parameters (`ActivatedRoute`):
 *    - Routes can define dynamic URL segments: `{ path: 'light/:id', component: LightDisplayComponent }`
 *    - Access parameter values in TypeScript using `ActivatedRoute`:
 *      `const id = this.route.snapshot.paramMap.get('id');`
 * 
 * 2. Component Lifecycle Hooks (`OnInit`, `OnDestroy`):
 *    Angular components go through a lifecycle managed by Angular's engine:
 *    - `ngOnInit()`: Called once when the component is created and inputs are set.
 *       Ideal for starting subscriptions, fetching API data, reading route params.
 *    - `ngOnDestroy()`: Called once right before Angular destroys the component 
 *      (e.g., when the user navigates away to another page).
 * 
 * 3. Memory Leak Prevention:
 *    Long-lived subscriptions (WebSockets, Realtime DB listeners, RxJS Observables, setIntervals)
 *    WILL stay alive in memory if not explicitly closed! Always clean them up inside `ngOnDestroy()`.
 * 
 * ============================================================================
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

  // Stores the teardown function for real-time listener
  private unsubscribeSignal?: () => void;

  ngOnInit(): void {
    // Read route parameter (:id from /light/:id)
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.sessionId.set(id);

      // Read optional state object passed during navigation
      const state = history.state;
      if (state?.title) {
        this.sessionTitle.set(state.title);
      }

      // Subscribe to real-time database changes
      this.unsubscribeSignal = this.firebaseService.subscribeToSignal(id, (newColor) => {
        this.currentColor.set(newColor);
      });
    }
  }

  ngOnDestroy(): void {
    // Crucial cleanup: Unsubscribe real-time listener when leaving page to prevent memory leaks
    if (this.unsubscribeSignal) {
      this.unsubscribeSignal();
    }
  }

  goBack(): void {
    this.router.navigate(['/']);
  }
}
