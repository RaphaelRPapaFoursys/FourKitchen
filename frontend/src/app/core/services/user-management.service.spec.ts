import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { environment } from '../../../environments/environment';
import { CriarUsuarioGestorRequest } from '../models/user-management.models';
import { UserManagementService } from './user-management.service';

describe('UserManagementService', () => {
  const baseUrl = `${environment.apiUrl}/api/gestor/usuarios`;
  let service: UserManagementService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(UserManagementService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('lists users through the BFF', () => {
    service.listUsers().subscribe(users => expect(users).toEqual([]));

    const request = httpMock.expectOne(baseUrl);
    expect(request.request.method).toBe('GET');
    request.flush([]);
  });

  it('creates a user through the BFF', () => {
    const body: CriarUsuarioGestorRequest = {
      nome: 'Maria Silva',
      email: 'maria@fourkitchen.com',
      senha: 'Senha123',
      perfilUsuario: 'GESTOR',
      idMesa: null,
    };

    service.createUser(body).subscribe();

    const request = httpMock.expectOne(baseUrl);
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(body);
    request.flush({});
  });

  it('updates a user without changing the password', () => {
    const body = {
      nome: 'Maria Atualizada',
      email: 'maria@fourkitchen.com',
      senha: null,
      perfilUsuario: 'GESTOR' as const,
      idMesa: null,
    };

    service.updateUser(7, body).subscribe();

    const request = httpMock.expectOne(`${baseUrl}/7`);
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual(body);
    request.flush({});
  });

  it('deactivates a user through the BFF', () => {
    service.deactivateUser(7).subscribe();

    const request = httpMock.expectOne(`${baseUrl}/7`);
    expect(request.request.method).toBe('DELETE');
    request.flush(null);
  });
});
