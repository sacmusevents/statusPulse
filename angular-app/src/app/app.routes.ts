import { Routes } from '@angular/router';
import { SessionListComponent } from './components/session-list/session-list.component';
import { LightDisplayComponent } from './components/light-display/light-display.component';

export const routes: Routes = [
  { path: '', component: SessionListComponent },
  { path: 'light/:id', component: LightDisplayComponent },
  { path: '**', redirectTo: '' }
];
