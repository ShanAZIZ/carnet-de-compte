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
  template: `
    <div class="card bg-base-100 border-base-300 mx-auto max-w-sm border">
      <form class="card-body" novalidate (submit)="signIn($event)">
        <h1 class="card-title text-2xl">Connexion</h1>

        <label class="label" for="login-email">Email</label>
        <input
          id="login-email"
          class="input w-full"
          type="email"
          autocomplete="username"
          [formField]="loginForm.email"
        />

        <label class="label" for="login-password">Mot de passe</label>
        <input
          id="login-password"
          class="input w-full"
          type="password"
          autocomplete="current-password"
          [formField]="loginForm.password"
        />

        @if (error(); as message) {
          <p role="alert" class="text-error">{{ message }}</p>
        }
        @if (isResetSent()) {
          <p role="status" class="text-success">
            Si ce compte existe, un email de réinitialisation vient d'être envoyé.
          </p>
        }

        <button
          type="submit"
          class="btn btn-primary mt-2"
          [disabled]="loginForm().invalid() || loginForm().submitting()"
        >
          Se connecter
        </button>
        <button
          type="button"
          class="btn btn-link"
          [disabled]="loginForm.email().invalid()"
          (click)="sendPasswordReset()"
        >
          Mot de passe oublié
        </button>
      </form>
    </div>
  `,
})
export class Login {
  readonly #auth = inject(Auth);
  readonly #router = inject(Router);
  protected readonly credentials = signal<Credentials>({ email: '', password: '' });
  protected readonly loginForm = form(this.credentials, credentialsSchema);
  protected readonly error = signal<string | null>(null);
  protected readonly isResetSent = signal(false);

  protected async signIn(event: Event): Promise<void> {
    event.preventDefault();
    await submit(this.loginForm, async (field) => {
      const { email, password } = field().value();
      try {
        await this.#auth.signIn(email, password);
        await this.#router.navigateByUrl('/');
      } catch {
        this.error.set('Email ou mot de passe incorrect.');
      }
      return undefined;
    });
  }

  protected async sendPasswordReset(): Promise<void> {
    await this.#auth.sendPasswordReset(this.loginForm.email().value());
    this.isResetSent.set(true);
  }
}
