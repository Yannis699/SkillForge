import { Routes } from '@angular/router';
import { AccueilComponent } from '@features/accueil/accueil.component';
import { FichiersComponent } from '@features/fichiers/fichiers.component';
import { AuthentificationComponent } from '@features/authentification/authentification.component';
import { SupervisionComponent } from '@features/supervision/supervision.component';

export const routes: Routes = [
{ path: '', component: AccueilComponent },
{ path: 'fichiers', component: FichiersComponent },
{ path: 'auth', component: AuthentificationComponent },
{ path: 'supervision', component: SupervisionComponent },
{ path: '**', redirectTo: '' }
];
