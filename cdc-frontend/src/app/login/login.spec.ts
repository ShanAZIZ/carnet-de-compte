import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { Auth } from '../core/auth';
import { Login } from './login';

describe('Login', () => {
  let fixture: ComponentFixture<Login>;
  let signIn: ReturnType<typeof vi.fn>;

  beforeEach(async () => {
    signIn = vi.fn(() => Promise.resolve());
    TestBed.configureTestingModule({
      imports: [Login],
      providers: [provideRouter([]), { provide: Auth, useValue: { signIn } }],
    });
    fixture = TestBed.createComponent(Login);
    await fixture.whenStable();
  });

  it('disables submit until email and password are valid', async () => {
    await fill('pas-un-email', 'secret');

    expect(submitButton().disabled).toBe(true);
  });

  it('signs in and goes to the home page', async () => {
    const navigate = vi.spyOn(TestBed.inject(Router), 'navigateByUrl').mockResolvedValue(true);
    await fill('alice@example.com', 'secret');

    submitButton().click();
    await fixture.whenStable();

    expect(navigate).toHaveBeenCalledWith('/');
  });

  it('shows an error when the credentials are rejected', async () => {
    signIn.mockRejectedValue(new Error('auth/invalid-credential'));
    await fill('alice@example.com', 'wrong');

    submitButton().click();
    await fixture.whenStable();

    expect(fixture.nativeElement.querySelector('[role="alert"]')?.textContent).toContain(
      'Email ou mot de passe incorrect',
    );
  });

  async function fill(email: string, password: string): Promise<void> {
    input('Email').value = email;
    input('Email').dispatchEvent(new Event('input'));
    input('Mot de passe').value = password;
    input('Mot de passe').dispatchEvent(new Event('input'));
    await fixture.whenStable();
  }

  function input(label: string): HTMLInputElement {
    const labelElement = Array.from<HTMLLabelElement>(
      fixture.nativeElement.querySelectorAll('label'),
    ).find((element) => element.textContent?.trim() === label);
    return fixture.nativeElement.querySelector(`#${labelElement?.htmlFor}`);
  }

  function submitButton(): HTMLButtonElement {
    return fixture.nativeElement.querySelector('button[type="submit"]');
  }
});
