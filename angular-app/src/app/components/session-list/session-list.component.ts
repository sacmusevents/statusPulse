import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FirebaseService } from '../../services/firebase.service';
import { Session } from '../../models/session.model';
import { RelativeTimePipe } from '../../pipes/relative-time.pipe';

/**
 * ============================================================================
 * ANGULAR STANDALONE COMPONENTS & SIGNALS EXPLAINED
 * ============================================================================
 * 
 * 1. What is a Standalone Component? (`standalone: true`)
 *    Introduced in modern Angular (v14+), standalone components do NOT require legacy 
 *    `@NgModule` wrapper files. Components explicitly list their own direct dependencies 
 *    in the `imports` array (e.g. `imports: [CommonModule, RelativeTimePipe]`).
 * 
 * 2. What are Angular Signals? (`signal()`)
 *    Signals are Angular's modern reactive state primitive:
 *    - `mySignal = signal(initialValue)`: Creates a reactive signal container.
 *    - Read value in TypeScript: `this.mySignal()` (call it like a function).
 *    - Read value in HTML: `{{ mySignal() }}`
 *    - Update value: `this.mySignal.set(newValue)` or `this.mySignal.update(val => val + 1)`
 * 
 *    Why Signals outshine traditional change detection:
 *    When a signal changes, Angular precisely targets and updates ONLY the specific DOM 
 *    nodes that read that signal, eliminating unnecessary component tree diffing!
 * 
 * 3. Component Lifecycle:
 *    - `ngOnInit()`: Called automatically by Angular ONCE after component inputs/DI are ready.
 *      Ideal location for initial API calls and data fetching.
 * 
 * ============================================================================
 */
@Component({
  selector: 'app-session-list',
  standalone: true,
  imports: [CommonModule, RelativeTimePipe],
  templateUrl: './session-list.component.html',
  styleUrl: './session-list.component.css'
})
export class SessionListComponent implements OnInit {
  // Dependency Injection using inject() function
  private firebaseService = inject(FirebaseService);
  private router = inject(Router);

  // Angular Signals holding reactive component state
  sessions = signal<Session[]>([]);
  isLoading = signal<boolean>(true);
  connectionStatus = signal<string>('Connecting...');

  async ngOnInit(): Promise<void> {
    try {
      const data = await this.firebaseService.getActiveSessions();
      // Update Signal state
      this.sessions.set(data);
      this.connectionStatus.set('Connected');
    } catch (error) {
      console.error('Failed to load sessions', error);
      this.connectionStatus.set('Connection Failed');
    } finally {
      this.isLoading.set(false);
    }
  }

  selectSession(session: Session): void {
    // Angular Router programmatic navigation
    this.router.navigate(['/light', session.id], { state: { title: session.title } });
  }
}
