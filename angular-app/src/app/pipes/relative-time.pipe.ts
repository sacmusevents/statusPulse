import { Pipe, PipeTransform } from '@angular/core';

/**
 * Custom Angular Pipe: RelativeTimePipe
 * ------------------------------------
 * Learning Point:
 * Pipes transform raw data into user-friendly strings directly inside templates.
 * Usage in HTML: {{ session.lastUpdated | relativeTime }}
 */
@Pipe({
  name: 'relativeTime',
  standalone: true
})
export class RelativeTimePipe implements PipeTransform {
  /**
   * TODO (Step 3): Customize or expand the relative time calculation.
   */
  transform(value: string | Date | undefined): string {
    if (!value) return 'never';

    const date = typeof value === 'string' ? new Date(value) : value;
    const now = new Date();
    const secondsAgo = Math.floor((now.getTime() - date.getTime()) / 1000);

    if (secondsAgo < 10) return 'just now';
    if (secondsAgo < 60) return `${secondsAgo}s ago`;

    const minutesAgo = Math.floor(secondsAgo / 60);
    if (minutesAgo < 60) return `${minutesAgo} mins ago`;

    const hoursAgo = Math.floor(minutesAgo / 60);
    if (hoursAgo < 24) return `${hoursAgo} hours ago`;

    const daysAgo = Math.floor(hoursAgo / 24);
    return `${daysAgo} days ago`;
  }
}
