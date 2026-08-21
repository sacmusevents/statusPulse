import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FirebaseService } from '../../services/firebase.service';
import { Session } from '../../models/session.model';
import { RelativeTimePipe } from '../../pipes/relative-time.pipe';

/**
 * Angular Component: SessionListComponent
 * ---------------------------------------
 * Learning Point:
 * Modern Angular components use `standalone: true` and `signals` for reactive state.
 * - `signal()` creates reactive state variable that updates the DOM automatically when changed.
 * - `inject(FirebaseService)` is the modern syntax for Dependency Injection in Angular.
 */
@Component({
  selector: 'app-session-list',
  standalone: true,
  imports: [CommonModule, RelativeTimePipe],
  templateUrl: './session-list.component.html',
  styleUrl: './session-list.component.css'
})
export class SessionListComponent implements OnInit {
  private firebaseService = inject(FirebaseService);
  private router = inject(Router);

  // Angular Signals for reactive UI state
  sessions = signal<Session[]>([]);
  isLoading = signal<boolean>(true);
  connectionStatus = signal<string>('Connecting...');

  async ngOnInit(): Promise<void> {
    try {
      // TODO: Call firebaseService to fetch real sessions
      const data = await this.firebaseService.getActiveSessions();
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
    // Navigate to the light display screen for this session using Angular Router
    this.router.navigate(['/light', session.id], { state: { title: session.title } });
  }
}
