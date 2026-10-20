import { of, throwError } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CsvImport } from './csv-import';
import { OperationImport } from './operation-import';

const HEADER = 'Date;Date de valeur;Libellé;Montant;Compte;Nom du compte;Solde';
const REQUIRED_KEYS = [
  'operationDate',
  'debitDate',
  'label',
  'amount',
  'accountId',
  'accountLabel',
  'balance',
];

describe('CsvImport', () => {
  let fixture: ComponentFixture<CsvImport>;
  let importOperations: ReturnType<typeof vi.fn>;

  beforeEach(async () => {
    importOperations = vi.fn(() => of({ deleted: 2, inserted: 5 }));
    TestBed.configureTestingModule({
      imports: [CsvImport],
      providers: [{ provide: OperationImport, useValue: { import: importOperations } }],
    });
    fixture = TestBed.createComponent(CsvImport);
    await fixture.whenStable();
  });

  it('lists the CSV headers to map with their position', async () => {
    await upload(`\uFEFF"Date";Date;Montant\r\n01/01/2026;02/01/2026;12,50`);

    expect(labelsOf('select')).toEqual(['1. Date', '2. Date', '3. Montant']);
  });

  it('disables import until every required field is mapped', async () => {
    await upload(HEADER);

    expect(importButton().disabled).toBe(true);
  });

  it('enables import once every required field is mapped', async () => {
    await upload(HEADER);

    await mapAll(REQUIRED_KEYS);

    expect(importButton().disabled).toBe(false);
  });

  it('reports a field mapped to several columns', async () => {
    await upload(HEADER);

    await mapAll([
      'operationDate',
      'operationDate',
      'label',
      'amount',
      'accountId',
      'accountLabel',
      'balance',
    ]);

    expect(alert()).toContain("« Date d'opération » est associé à plusieurs colonnes");
  });

  it('sends the file and the mapping, then shows the result', async () => {
    await upload(HEADER);
    await mapAll(REQUIRED_KEYS);

    importButton().click();
    await fixture.whenStable();

    expect(fixture.nativeElement.querySelector('[role="status"]')?.textContent).toContain(
      '5 opérations importées, 2 anciennes supprimées',
    );
  });

  it('shows the message the API rejects the import with', async () => {
    importOperations.mockReturnValue(
      throwError(() => new HttpErrorResponse({ status: 400, error: 'Ligne 3, champ amount' })),
    );
    await upload(HEADER);
    await mapAll(REQUIRED_KEYS);

    importButton().click();
    await fixture.whenStable();

    expect(alert()).toContain('Ligne 3, champ amount');
  });

  async function upload(content: string): Promise<void> {
    const input: HTMLInputElement = fixture.nativeElement.querySelector('input[type="file"]');
    Object.defineProperty(input, 'files', { value: [new File([content], 'releve.csv')] });
    input.dispatchEvent(new Event('change'));
    await vi.waitFor(() => expect(selects().length).toBeGreaterThan(0));
    await fixture.whenStable();
  }

  async function mapAll(keys: string[]): Promise<void> {
    selects().forEach((select, index) => {
      select.value = keys[index];
      select.dispatchEvent(new Event('input'));
    });
    await fixture.whenStable();
  }

  function selects(): HTMLSelectElement[] {
    return [...fixture.nativeElement.querySelectorAll('select')];
  }

  function labelsOf(selector: string): string[] {
    return [...fixture.nativeElement.querySelectorAll(selector)].map((element: HTMLElement) =>
      fixture.nativeElement.querySelector(`label[for="${element.id}"]`).textContent.trim(),
    );
  }

  function importButton(): HTMLButtonElement {
    return fixture.nativeElement.querySelector('button[type="submit"]');
  }

  function alert(): string {
    return fixture.nativeElement.querySelector('[role="alert"]')?.textContent ?? '';
  }
});
