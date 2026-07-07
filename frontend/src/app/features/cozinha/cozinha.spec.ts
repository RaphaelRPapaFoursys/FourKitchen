import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { Cozinha } from './cozinha';
import { environment } from '../../../environments/environment';

describe('Cozinha', () => {
  let component: Cozinha;
  let fixture: ComponentFixture<Cozinha>;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Cozinha],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(Cozinha);
    component = fixture.componentInstance;

    httpMock.expectOne(`${environment.apiUrl}/api/cozinha/fila`).flush([]);
    fixture.detectChanges();
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
