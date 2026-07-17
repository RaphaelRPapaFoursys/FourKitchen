import { ChangeDetectionStrategy, Component, output } from '@angular/core';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
  selector: 'app-customer-hero',
  templateUrl: './customer-hero.html',
  styleUrl: './customer-hero.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [TranslatePipe],
})
export class CustomerHeroComponent {
  readonly sectionSelected = output<{ sectionId: string; event: Event }>();

  protected selectSection(sectionId: string, event: Event): void {
    this.sectionSelected.emit({ sectionId, event });
  }
}
