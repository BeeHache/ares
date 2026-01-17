import { Routes } from '@angular/router';
import { HomeComponent } from './home/home.component';
import { LoginComponent } from './login/login.component';
import { RegisterComponent } from './register/register.component';
import { UserComponent } from './user/user.component';
import { VerifyEmailComponent } from './verify-email/verify-email.component';
import { LicensesComponent } from './about/licenses/licenses.component';
import { authGuard } from './auth.guard';

export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'verify/:code', component: VerifyEmailComponent },
  { path: 'about/licenses', component: LicensesComponent },
  { path: 'user', component: UserComponent, canActivate: [authGuard] },
  { path: '**', redirectTo: '' }
];
