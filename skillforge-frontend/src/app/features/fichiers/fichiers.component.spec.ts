import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FichiersComponent } from './fichiers.component';

describe('FichiersComponent', () => {
  let component: FichiersComponent;
  let fixture: ComponentFixture<FichiersComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FichiersComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FichiersComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
