import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';

import { AuthService } from '../../../core/services/auth';
import { normalizePerfil } from '../../../core/utils/profile-redirect';

@Component({
  selector: 'app-device-indicator',
  templateUrl: './device-indicator.html',
  styleUrl: './device-indicator.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [TranslatePipe],
})
export class DeviceIndicatorComponent {
  private readonly authService = inject(AuthService);
  private readonly translate = inject(TranslateService);

  private readonly usuario = toSignal(this.authService.usuario$, {
    initialValue: this.authService.getCurrentUser(),
  });

  protected readonly label = computed(() => {
    const usuario = this.usuario();
    const perfil = usuario ? normalizePerfil(usuario.perfil) : '';

    if (perfil === 'MESA' && usuario?.idMesa) {
      return this.translate.instant('COMMON.TABLE', { number: String(usuario.idMesa).padStart(2, '0') });
    }

    const nome = usuario?.nome.trim();

    if (perfil === 'TOTEM' && nome) {
      return nome.replace(/\d+/g, value => {
        const number = Number(value);
        return number < 10 ? String(number).padStart(2, '0') : value;
      });
    }

    return null;
  });
}
