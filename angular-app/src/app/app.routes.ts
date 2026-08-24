import { Routes } from '@angular/router';
import { SessionListComponent } from './components/session-list/session-list.component';
import { LightDisplayComponent } from './components/light-display/light-display.component';

/**
 * ============================================================================
 * ANGULAR ROUTER EXPLAINED (`app.routes.ts`)
 * ============================================================================
 * 
 * 1. What is Angular Router?
 *    Angular Router is the official client-side routing library for Angular.
 *    It maps browser URL paths to Angular components without reloading the browser page.
 * 
 * 2. Key Route Definitions:
 *    - `{ path: '', component: MyComponent }`: Default root URL path.
 *    - `{ path: 'users/:id', component: UserComponent }`: Dynamic URL parameter (`:id`).
 *    - `{ path: '**', redirectTo: '' }`: Wildcard route (catches 404 / unknown URLs and redirects).
 * 
 * 3. Advanced Features to Know:
 *    - Lazy Loading Components (`loadComponent`):
 *      `{ path: 'admin', loadComponent: () => import('./admin/admin.component').then(m => m.AdminComponent) }`
 *      Splits JavaScript bundles so users only download code for the page they visit!
 *    - Route Guards (`canActivate`):
 *      Functions that prevent unauthorized users from entering specific routes (e.g. Auth guards).
 * 
 * ============================================================================
 */
export const routes: Routes = [
  { path: '', component: SessionListComponent },
  { path: 'light/:id', component: LightDisplayComponent },
  { path: '**', redirectTo: '' }
];
