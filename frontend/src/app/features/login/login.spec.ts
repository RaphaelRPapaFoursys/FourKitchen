import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { provideRouter, Router } from '@angular/router';
import { Observable, Subject, of, throwError } from 'rxjs';

import { LoginFormValue, LoginResponse } from '../../core/models/auth.models';
import { AuthService } from '../../core/services/auth';
import { Login } from './login';

class AuthServiceMock {
  login = vi.fn<(credentials: LoginFormValue) => Observable<LoginResponse>>();
}

describe('Login', () => {
  let component: Login;
  let fixture: ComponentFixture<Login>;
  let authService: AuthServiceMock;
  let router: Router;

  const loginResponse: LoginResponse = {
    accessToken: 'access-token',
    tokenType: 'Bearer',
    usuario: {
      id: 1,
      nome: 'Usuario Gestor',
      email: 'gestor@fourkitchen.com',
      perfil: 'GESTOR',
    },
  };

  beforeEach(async () => {
    authService = new AuthServiceMock();

    await TestBed.configureTestingModule({
      imports: [Login],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: authService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Login);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigateByUrl').mockResolvedValue(true);
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should not call login when form is invalid', () => {
    submitForm();

    expect(authService.login).not.toHaveBeenCalled();
    expect(queryText('#email-error')).toContain('Informe um e-mail valido.');
    expect(queryText('#password-error')).toContain('Informe sua senha.');
  });

  it('should send form credentials and redirect to profile route on success', () => {
    authService.login.mockReturnValue(of(loginResponse));

    fillForm();
    submitForm();

    expect(authService.login).toHaveBeenCalledWith({
      email: 'gestor@fourkitchen.com',
      password: '123456',
    });
    expect(router.navigateByUrl).toHaveBeenCalledWith('/gestor');
  });

  it('should keep loading while login request is pending', () => {
    const loginRequest = new Subject<LoginResponse>();
    authService.login.mockReturnValue(loginRequest.asObservable());

    fillForm();
    submitForm();
    fixture.detectChanges();

    expect(getSubmitButton().disabled).toBe(true);
    expect(getSubmitButton().textContent).toContain('Entrando...');

    loginRequest.next(loginResponse);
    loginRequest.complete();
    fixture.detectChanges();

    expect(getSubmitButton().disabled).toBe(false);
    expect(getSubmitButton().textContent).toContain('Entrar no Sistema');
  });

  it('should show invalid credentials message when login returns 401', () => {
    authService.login.mockReturnValue(
      throwLoginError(new HttpErrorResponse({ status: 401 })),
    );

    fillForm();
    submitForm();
    fixture.detectChanges();

    expect(queryText('[role="alert"]')).toContain('E-mail ou senha invalidos.');
  });

  it('should show api error message when available', () => {
    authService.login.mockReturnValue(
      throwLoginError(
        new HttpErrorResponse({
          status: 400,
          error: {
            codError: 'AUTH_001',
            msgError: 'Usuario bloqueado.',
          },
        }),
      ),
    );

    fillForm();
    submitForm();
    fixture.detectChanges();

    expect(queryText('[role="alert"]')).toContain('Usuario bloqueado.');
  });

  it('should toggle password visibility', () => {
    fixture.detectChanges();

    const passwordInput = getPasswordInput();
    const toggleButton = fixture.debugElement.query(By.css('.login-form__icon-button'));

    expect(passwordInput.type).toBe('password');

    toggleButton.triggerEventHandler('click');
    fixture.detectChanges();

    expect(getPasswordInput().type).toBe('text');
  });

  function fillForm(): void {
    component['loginForm'].setValue({
      email: 'gestor@fourkitchen.com',
      password: '123456',
    });
    fixture.detectChanges();
  }

  function submitForm(): void {
    fixture.debugElement.query(By.css('form')).triggerEventHandler('ngSubmit');
    fixture.detectChanges();
  }

  function getSubmitButton(): HTMLButtonElement {
    return fixture.nativeElement.querySelector('button[type="submit"]');
  }

  function getPasswordInput(): HTMLInputElement {
    return fixture.nativeElement.querySelector('#password');
  }

  function queryText(selector: string): string {
    return fixture.nativeElement.querySelector(selector)?.textContent.trim() ?? '';
  }

  function throwLoginError(error: HttpErrorResponse): Observable<never> {
    return throwError(() => error);
  }
});
