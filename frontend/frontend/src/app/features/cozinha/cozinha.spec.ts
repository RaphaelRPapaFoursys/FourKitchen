import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Cozinha } from './cozinha';

describe('Cozinha', () => {
  let component: Cozinha;
  let fixture: ComponentFixture<Cozinha>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Cozinha],
    }).compileComponents();

    fixture = TestBed.createComponent(Cozinha);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
