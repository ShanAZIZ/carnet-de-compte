import { firstValueFrom } from 'rxjs';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../environments/environment';
import { OperationImport } from './operation-import';

const URL = `${environment.apiUrl}/operations`;

describe('OperationImport', () => {
  let service: OperationImport;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(OperationImport);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('posts the file with the index of each mapped column only', () => {
    const file = new File(['Date;Note'], 'releve.csv');

    service
      .import(file, [
        { csvColumn: 'Note', appColumn: '' },
        { csvColumn: 'Date', appColumn: 'operationDate' },
      ])
      .subscribe();

    const body: FormData = http.expectOne({ method: 'POST', url: URL }).request.body;
    expect([body.get('file'), body.get('mapping')]).toEqual([
      expect.any(File),
      '[{"csv_index":1,"app_column":"operationDate"}]',
    ]);
  });

  it('returns the import result', async () => {
    const result = firstValueFrom(service.import(new File([''], 'releve.csv'), []));

    http.expectOne(URL).flush({ deleted: 2, inserted: 5 });

    expect(await result).toEqual({ deleted: 2, inserted: 5 });
  });
});
