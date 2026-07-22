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

    httpMock.expectOne(req => req.url === baseUrl).flush({
      content: [{
        id: 1,
        nome: 'Pratos principais',
        descricao: 'Refeições completas',
        imagemUrl: null,
        ativo: true,
      }],
      page: 0, size: 10, totalElements: 1, totalPages: 1, first: true, last: true,
    });
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

  it('filters inactive categories through the backend', () => {
    const buttons = [...fixture.nativeElement.querySelectorAll('.category-filters button')] as HTMLButtonElement[];
    buttons.find(button => button.textContent?.trim() === 'Inativas')?.click();

    const request = httpMock.expectOne(req => req.url === baseUrl && req.params.get('ativo') === 'false');
    expect(request.request.params.get('page')).toBe('0');
    request.flush({
      content: [], page: 0, size: 10, totalElements: 0, totalPages: 0, first: true, last: true,
    });
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
      imagemUrl: null,
      ativo: false,
    });
    httpMock.expectOne(req => req.url === baseUrl).flush({
      content: [], page: 0, size: 10, totalElements: 0, totalPages: 0, first: true, last: true,
    });
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Categoria desativada com sucesso.');
  });
});
