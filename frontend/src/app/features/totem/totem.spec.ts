import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Totem } from './totem';

describe('Totem', () => {
  let component: Totem;
  let fixture: ComponentFixture<Totem>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Totem],
    }).compileComponents();

    fixture = TestBed.createComponent(Totem);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
