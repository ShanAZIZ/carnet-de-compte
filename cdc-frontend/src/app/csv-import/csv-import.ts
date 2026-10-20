import { firstValueFrom } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { form, FormField, schema, submit, validate } from '@angular/forms/signals';
import { ColumnMapping, ImportResult, OperationImport } from './operation-import';

const APP_FIELDS = [
  { key: 'operationDate', label: "Date d'opération", isRequired: true },
  { key: 'debitDate', label: 'Date de débit', isRequired: true },
  { key: 'label', label: 'Libellé', isRequired: true },
  { key: 'suggestedLabel', label: 'Libellé suggéré', isRequired: false },
  { key: 'amount', label: 'Montant', isRequired: true },
  { key: 'accountId', label: 'Numéro de compte', isRequired: true },
  { key: 'accountLabel', label: 'Libellé du compte', isRequired: true },
  { key: 'balance', label: 'Solde', isRequired: true },
];

const mappingSchema = schema<ColumnMapping[]>((path) => {
  validate(path, ({ value }) => {
    const mapped = value().map((column) => column.appColumn);
    return APP_FIELDS.flatMap((field) => {
      const count = mapped.filter((key) => key === field.key).length;
      if (field.isRequired && count === 0) {
        return [{ kind: 'missing', message: `« ${field.label} » n'est associé à aucune colonne` }];
      }
      if (count > 1) {
        return [
          { kind: 'duplicated', message: `« ${field.label} » est associé à plusieurs colonnes` },
        ];
      }
      return [];
    });
  });
});

@Component({
  imports: [FormField],
  selector: 'app-csv-import',
  templateUrl: './csv-import.html',
})
export class CsvImport {
  readonly #operationImport = inject(OperationImport);
  readonly #file = signal<File | null>(null);
  protected readonly mapping = signal<ColumnMapping[]>([]);
  protected readonly mappingForm = form(this.mapping, mappingSchema);
  protected readonly result = signal<ImportResult | null>(null);
  protected readonly appFields = APP_FIELDS;

  protected async import(event: Event): Promise<void> {
    event.preventDefault();
    const file = this.#file();
    if (!file) return;
    await submit(this.mappingForm, async (field) => {
      try {
        this.result.set(await firstValueFrom(this.#operationImport.import(file, field().value())));
        return undefined;
      } catch (failure) {
        return { kind: 'server', message: serverMessage(failure) };
      }
    });
  }

  protected async readHeaders(event: Event): Promise<void> {
    const file = (event.target as HTMLInputElement).files?.[0] ?? null;
    const firstLine = file ? (await file.text()).replace(/^\uFEFF/, '').split(/\r?\n/, 1)[0] : '';
    // ponytail: naive `;` split, a quoted header containing `;` breaks; parse properly if a bank exports one.
    const headers = firstLine.split(';').map((header) => header.replace(/^"(.*)"$/, '$1'));
    this.#file.set(file);
    this.result.set(null);
    this.mapping.set(headers.map((csvColumn) => ({ csvColumn, appColumn: '' })));
  }
}

function serverMessage(failure: unknown): string {
  const isReadable = failure instanceof HttpErrorResponse && typeof failure.error === 'string';
  return isReadable && failure.error ? failure.error : "L'import a échoué, réessayez.";
}
