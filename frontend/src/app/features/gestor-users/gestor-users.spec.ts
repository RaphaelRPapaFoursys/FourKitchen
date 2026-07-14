import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { environment } from '../../../environments/environment';
import { GestorUsers } from './gestor-users';

describe('GestorUsers', () => {
  const baseUrl = `${environment.apiUrl}/api/gestor/usuarios`;
  let fixture: ComponentFixture<GestorUsers>;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    localStorage.clear();
    await TestBed.configureTestingModule({
      imports: [GestorUsers],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(GestorUsers);
    fixture.detectChanges();

    httpMock.expectOne(baseUrl).flush([
      {
        id: 1,
        nome: 'Maria Silva',
        email: 'maria@fourkitchen.com',
        perfilUsuario: 'GESTOR',
        idMesa: null,
        ativo: true,
      },
    ]);
    await fixture.whenStable();
    fixture.detectChanges();
  });

  afterEach(() => httpMock.verify());

  it('creates the user management page', () => {
    expect(fixture.componentInstance).toBeTruthy();
    expect(fixture.nativeElement.textContent).toContain('Usuários');
    expect(fixture.nativeElement.textContent).toContain('Maria Silva');
  });

  it('opens the form to create a user', () => {
    const createButton = fixture.nativeElement.querySelector('.page-heading .primary-button') as HTMLButtonElement;
    createButton.click();
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.user-dialog')).toBeTruthy();
    expect(fixture.nativeElement.textContent).toContain('Cadastrar usuário');
  });

  it('requires a table id for the MESA profile', () => {
    const createButton = fixture.nativeElement.querySelector('.page-heading .primary-button') as HTMLButtonElement;
    createButton.click();
    fixture.detectChanges();

    const profileSelect = fixture.nativeElement.querySelector(
      'select[formControlName="perfilUsuario"]',
    ) as HTMLSelectElement;
    const mesaOption = Array.from(profileSelect.options).find(option => option.text === 'Mesa');
    expect(mesaOption).toBeTruthy();

    profileSelect.value = mesaOption!.value;
    profileSelect.dispatchEvent(new Event('change'));
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('input[formControlName="idMesa"]')).toBeTruthy();
  });
});
