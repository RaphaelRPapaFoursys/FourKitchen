import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Topbar } from './header';

describe('Topbar', () => {
  let component: Topbar;
  let fixture: ComponentFixture<Topbar>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Topbar],
    }).compileComponents();

    fixture = TestBed.createComponent(Topbar);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('reflete a contagem de notificações no template', () => {
    fixture.componentRef.setInput('notificacoes', 3);
    fixture.detectChanges();

    const contagem = fixture.nativeElement.querySelector('.topbar__notificacao-contagem');
    expect(contagem?.textContent?.trim()).toBe('3');
  });
});
