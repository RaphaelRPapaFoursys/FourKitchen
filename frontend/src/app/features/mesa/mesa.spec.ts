import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Mesa } from './mesa';

describe('Mesa', () => {
  let component: Mesa;
  let fixture: ComponentFixture<Mesa>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Mesa],
    }).compileComponents();

    fixture = TestBed.createComponent(Mesa);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
