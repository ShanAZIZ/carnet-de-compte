import { Component, inject, signal } from '@angular/core';
import { email, form, FormField, required, schema, submit } from '@angular/forms/signals';
import { Router } from '@angular/router';
import { Auth } from '../core/auth';

interface Credentials {
  email: string;
  password: string;
}

const credentialsSchema = schema<Credentials>((path) => {
  required(path.email, { message: 'Email requis' });
  email(path.email, { message: 'Email invalide' });
  required(path.password, { message: 'Mot de passe requis' });
});

@Component({
  imports: [FormField],
  selector: 'app-login',
  templateUrl: './login.html',
})
export class Login {
  readonly #auth = inject(Auth);
  readonly #router = inject(Router);
  protected readonly credentials = signal<Credentials>({ email: '', password: '' });
  protected readonly loginForm = form(this.credentials, credentialsSchema);
  protected readonly error = signal<string | null>(null);

  protected async signIn(event: Event): Promise<void> {
    event.preventDefault();
    await submit(this.loginForm, async (field) => {
      const { email, password } = field().value();
      try {
        await this.#auth.signIn(email, password);
        await this.#router.navigateByUrl('/', { replaceUrl: true });
      } catch {
        this.error.set('Email ou mot de passe incorrect.');
      }
      return undefined;
    });
  }
}
