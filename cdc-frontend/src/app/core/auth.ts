import { Service, signal } from '@angular/core';
import { initializeApp } from 'firebase/app';
import {
  getAuth,
  onAuthStateChanged,
  signInWithEmailAndPassword,
  signOut,
  User,
} from 'firebase/auth';
import { FIREBASE_CONFIG } from './firebase-config';

@Service()
export class Auth {
  readonly #auth = getAuth(initializeApp(FIREBASE_CONFIG));
  readonly #user = signal<User | null>(null);
  readonly user = this.#user.asReadonly();

  constructor() {
    onAuthStateChanged(this.#auth, (user) => this.#user.set(user));
  }

  async isSignedIn(): Promise<boolean> {
    await this.#auth.authStateReady();
    return this.#auth.currentUser !== null;
  }

  async idToken(): Promise<string | null> {
    return (await this.#auth.currentUser?.getIdToken()) ?? null;
  }

  async signIn(email: string, password: string): Promise<void> {
    await signInWithEmailAndPassword(this.#auth, email, password);
  }

  signOut(): Promise<void> {
    return signOut(this.#auth);
  }
}
