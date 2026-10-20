import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { environment } from '../../environments/environment';

export interface ColumnMapping {
  csvColumn: string;
  appColumn: string;
}

export interface ImportResult {
  deleted: number;
  inserted: number;
}

@Service()
export class OperationImport {
  readonly #http = inject(HttpClient);

  import(file: File, mapping: ColumnMapping[]): Observable<ImportResult> {
    const body = new FormData();
    body.append('file', file);
    body.append(
      'mapping',
      JSON.stringify(
        mapping
          .map((column, index) => ({ csv_index: index, app_column: column.appColumn }))
          .filter((column) => column.app_column),
      ),
    );
    return this.#http.post<ImportResult>(`${environment.apiUrl}/operations`, body);
  }
}
