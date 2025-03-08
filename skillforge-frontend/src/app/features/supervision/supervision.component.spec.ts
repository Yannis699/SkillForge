import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SupervisionComponent } from './supervision.component';

describe('SupervisionComponent', () => {
  let component: SupervisionComponent;
  let fixture: ComponentFixture<SupervisionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SupervisionComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SupervisionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
