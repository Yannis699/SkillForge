import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { AccueilComponent } from './features/accueil/accueil.component';
import { FichiersComponent } from './features/fichiers/fichiers.component';
import { AuthentificationComponent } from './features/authentification/authentification.component';
import { SupervisionComponent } from './features/supervision/supervision.component';

const routes: Routes = [
{ path: '', component: AccueilComponent }, // Page d'accueil
{ path: 'fichiers', component: FichiersComponent },
{ path: 'auth', component: AuthentificationComponent },
{ path: 'supervision', component: SupervisionComponent }
];

@NgModule({
imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
