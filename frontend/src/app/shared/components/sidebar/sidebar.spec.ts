import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Sidebar } from './sidebar';

describe('Sidebar', () => {
  let component: Sidebar;
  let fixture: ComponentFixture<Sidebar>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Sidebar],
    }).compileComponents();

    fixture = TestBed.createComponent(Sidebar);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('emite sair ao clicar no botão de logout', () => {
    const emitido = vi.fn();
    component.sair.subscribe(emitido);

    const botao: HTMLButtonElement = fixture.nativeElement.querySelector('.sidebar__sair');
    botao.click();

    expect(emitido).toHaveBeenCalledOnce();
  });
});
