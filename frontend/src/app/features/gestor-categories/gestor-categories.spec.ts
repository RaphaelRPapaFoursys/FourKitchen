import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { environment } from '../../../environments/environment';
import { GestorCategories } from './gestor-categories';

describe('GestorCategories', () => {
  const baseUrl = `${environment.apiUrl}/api/gestor/catalogo/categorias`;
  let fixture: ComponentFixture<GestorCategories>;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    localStorage.clear();
    await TestBed.configureTestingModule({
      imports: [GestorCategories],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(GestorCategories);
    fixture.detectChanges();

    httpMock.expectOne(baseUrl).flush([
      {
        id: 1,
        nome: 'Pratos principais',
        descricao: 'Refeições completas',
        imagem: null,
        ativo: true,
      },
    ]);
    await fixture.whenStable();
    fixture.detectChanges();
  });

  afterEach(() => httpMock.verify());

  it('creates the category management page', () => {
    expect(fixture.componentInstance).toBeTruthy();
    expect(fixture.nativeElement.textContent).toContain('Categorias');
    expect(fixture.nativeElement.textContent).toContain('Pratos principais');
  });

  it('opens the category form with the customized image field', () => {
    const createButton = fixture.nativeElement.querySelector('.page-heading .primary-button') as HTMLButtonElement;
    createButton.click();
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.category-dialog')).toBeTruthy();
    expect(fixture.nativeElement.textContent).toContain('Cadastrar categoria');
    expect(fixture.nativeElement.textContent).toContain('Imagem da categoria');
  });

  it('warns and deactivates an active category', () => {
    const confirmSpy = vi.spyOn(window, 'confirm').mockReturnValue(true);
    const statusButton = fixture.nativeElement.querySelector('.row-actions__status') as HTMLButtonElement;
    statusButton.click();

    expect(confirmSpy).toHaveBeenCalledWith(
      expect.stringContaining('produtos vinculados deixarão de aparecer'),
    );
    const request = httpMock.expectOne(`${baseUrl}/1/desativar`);
    expect(request.request.method).toBe('PATCH');
    request.flush({
      id: 1,
      nome: 'Pratos principais',
      descricao: 'Refeições completas',
      imagem: null,
      ativo: false,
    });
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Categoria desativada com sucesso.');
  });
});
