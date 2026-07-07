import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Garcom } from './garcom';

describe('Garcom', () => {
  let component: Garcom;
  let fixture: ComponentFixture<Garcom>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Garcom],
    }).compileComponents();

    fixture = TestBed.createComponent(Garcom);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
