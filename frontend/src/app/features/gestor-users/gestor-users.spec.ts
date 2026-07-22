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
      {
        id: 2,
        nome: 'Bruno Lima',
        email: 'bruno@fourkitchen.com',
        perfilUsuario: 'GARCOM',
        idMesa: null,
        ativo: false,
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
    expect(fixture.nativeElement.textContent).toContain('Bruno Lima');
    expect(fixture.nativeElement.querySelector('.users-table thead').textContent).not.toContain('Mesa');
  });

  it('filters users by profile', () => {
    const buttons = [...fixture.nativeElement.querySelectorAll('.category-filters button')] as HTMLButtonElement[];
    buttons.find(button => button.textContent?.trim() === 'Garçom')?.click();
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Bruno Lima');
    expect(fixture.nativeElement.textContent).not.toContain('Maria Silva');
  });

  it('filters users by inactive status', () => {
    const buttons = [...fixture.nativeElement.querySelectorAll('.user-status-filters button')] as HTMLButtonElement[];
    buttons.find(button => button.textContent?.trim() === 'Inativos')?.click();
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Bruno Lima');
    expect(fixture.nativeElement.textContent).not.toContain('Maria Silva');
  });

  it('reactivates an inactive user', () => {
    const rows = [...fixture.nativeElement.querySelectorAll('.users-table tbody tr')] as HTMLTableRowElement[];
    const inactiveRow = rows.find(row => row.textContent?.includes('Bruno Lima'));
    const activateButton = inactiveRow?.querySelector('.row-actions__status') as HTMLButtonElement;
    expect(activateButton.textContent?.trim()).toBe('Ativar');

    activateButton.click();
    const request = httpMock.expectOne(`${baseUrl}/2/ativar`);
    expect(request.request.method).toBe('PATCH');
    request.flush({
      id: 2,
      nome: 'Bruno Lima',
      email: 'bruno@fourkitchen.com',
      perfilUsuario: 'GARCOM',
      idMesa: null,
      ativo: true,
    });
    fixture.detectChanges();

    expect(inactiveRow?.querySelector('.row-actions__status')?.textContent?.trim()).toBe('Inativar');
  });

  it('updates the password requirement checks while typing', () => {
    const createButton = fixture.nativeElement.querySelector('.page-heading .primary-button') as HTMLButtonElement;
    createButton.click();
    fixture.detectChanges();

    const passwordInput = fixture.nativeElement.querySelector('input[formControlName="senha"]') as HTMLInputElement;
    passwordInput.value = 'Senha123';
    passwordInput.dispatchEvent(new Event('input'));
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelectorAll('.password-requirements__item--met').length).toBe(4);
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
