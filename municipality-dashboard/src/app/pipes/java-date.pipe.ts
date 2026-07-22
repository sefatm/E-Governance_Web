import { Pipe, PipeTransform } from '@angular/core';

@Pipe({ name: 'javaDate' })
export class JavaDatePipe implements PipeTransform {

  private months = ['Jan','Feb','Mar','Apr','May','Jun',
                    'Jul','Aug','Sep','Oct','Nov','Dec'];

  transform(value: any, format: 'date' | 'datetime' = 'date'): string {
    if (!value) return '—';

    let d: Date;

    // Case 1: Java LocalDateTime array [year, month, day, hour, min, sec]
    if (Array.isArray(value)) {
      const [y, m, day, h = 0, min = 0] = value;
      d = new Date(y, m - 1, day, h, min);
    }
    // Case 2: ISO string "2026-04-29T14:51:00" or "2026-04-29"
    else if (typeof value === 'string') {
      d = new Date(value);
    }
    // Case 3: Already a Date object
    else if (value instanceof Date) {
      d = value;
    } else {
      return '—';
    }

    if (isNaN(d.getTime())) return '—';

    const day2  = String(d.getDate()).padStart(2, '0');
    const month = this.months[d.getMonth()];
    const year  = d.getFullYear();

    if (format === 'datetime') {
      const h   = String(d.getHours()).padStart(2, '0');
      const min = String(d.getMinutes()).padStart(2, '0');
      return `${day2} ${month} ${year}, ${h}:${min}`;
    }

    return `${day2} ${month} ${year}`;
  }
}
