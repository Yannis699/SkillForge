import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';
import { HeaderComponent } from './core/header/header.component';
import { SidebarComponent } from './core/sidebar/sidebar.component';
import { FooterComponent } from './core/footer/footer.component';

@Component({
selector: 'app-root',
standalone: true,
templateUrl: './app.component.html',
styleUrls: ['./app.component.scss'],
imports: [RouterModule, HeaderComponent, SidebarComponent, FooterComponent]
})
export class AppComponent { }
