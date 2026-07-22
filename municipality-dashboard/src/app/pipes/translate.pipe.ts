import { Pipe, PipeTransform } from '@angular/core';
import { LanguageService } from '../services/language.service';

@Pipe({ name: 'T', pure: false })
export class TranslatePipe implements PipeTransform {
  constructor(private ls: LanguageService) {}
  transform(en: string, bn: string): string {
    return this.ls.current === 'bn' ? bn : en;
  }
}
