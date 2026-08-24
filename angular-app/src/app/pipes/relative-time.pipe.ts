import { Pipe, PipeTransform } from '@angular/core';

/**
 * ============================================================================
 * ANGULAR PIPES EXPLAINED (@Pipe & PipeTransform)
 * ============================================================================
 * 
 * 1. What is an Angular Pipe?
 *    A Pipe is a class adorned with the `@Pipe` decorator that transforms input data
 *    into a formatted output directly inside HTML templates without modifying the underlying data.
 * 
 * 2. Template Syntax:
 *    - Basic: `{{ value | pipeName }}`
 *    - Parameterized: `{{ value | pipeName:arg1:arg2 }}`
 *    - Chaining: `{{ value | date:'yyyy-MM-dd' | uppercase }}`
 * 
 * 3. Built-in Angular Pipes to know:
 *    - `DatePipe`: {{ dateObj | date:'short' }}
 *    - `CurrencyPipe`: {{ price | currency:'USD' }}
 *    - `DecimalPipe` / `PercentPipe`: {{ ratio | percent }}
 *    - `JsonPipe`: {{ object | json }} (Invaluable for debugging state in templates!)
 *    - `AsyncPipe`: {{ observable$ | async }} (Automatically subscribes/unsubscribes from RxJS Observables)
 * 
 * 4. Pure vs. Impure Pipes (Performance Optimization):
 *    - By default, Angular pipes are "Pure" (`pure: true`). Angular executes a pure pipe 
 *      ONLY when it detects a change to the primitive input value or object reference (by memory address).
 *    - Pure pipes are cached and memoized for high performance during change detection.
 *    - Impure pipes (`pure: false`) run on every change detection cycle (use sparingly!).
 * 
 * ============================================================================
 */
@Pipe({
  name: 'relativeTime',
  standalone: true // Standalone pipe: Can be imported directly by any component without @NgModule
})
export class RelativeTimePipe implements PipeTransform {
  /**
   * The `transform` method is required by the `PipeTransform` interface.
   * Angular calls this method automatically whenever the template evaluates `{{ data | relativeTime }}`.
   * 
   * @param value - Raw input value passed before the pipe operator `|`
   * @returns Formatted user-friendly string
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
