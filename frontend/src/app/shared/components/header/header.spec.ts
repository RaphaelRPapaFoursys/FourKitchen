import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { Topbar } from './header';

describe('Topbar', () => {
  let component: Topbar;
  let fixture: ComponentFixture<Topbar>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Topbar],
      providers: [provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(Topbar);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('renders the notification count', () => {
    fixture.componentRef.setInput('notificacoes', 3);
    fixture.detectChanges();

    const count = fixture.nativeElement.querySelector('.topbar__notificacao-contagem');
    expect(count?.textContent?.trim()).toBe('3');
  });

  it('oculta somente a busca quando configurado para uma tela sem pesquisa', () => {
    fixture.componentRef.setInput('mostrarBusca', false);
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.topbar__search')).toBeNull();
    expect(fixture.nativeElement.querySelector('.topbar__acoes')).not.toBeNull();
  });

  it('keeps only one popover open at a time', () => {
    const notifications: HTMLButtonElement = fixture.nativeElement.querySelector('.topbar__notificacao');
    const avatar: HTMLButtonElement = fixture.nativeElement.querySelector('.user-menu__trigger');

    notifications.click();
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.topbar__notificacoes-painel')).not.toBeNull();

    avatar.click();
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.topbar__notificacoes-painel')).toBeNull();
    expect(fixture.nativeElement.querySelector('.user-menu__popover')).not.toBeNull();
  });
});
