import { Injectable } from '@angular/core';
import { CanActivate, CanActivateChild, ActivatedRouteSnapshot, RouterStateSnapshot, Router, UrlTree } from '@angular/router';
import { Observable, of } from 'rxjs';
import { take, switchMap, catchError, map } from 'rxjs/operators';
import { CurrentUserService } from './current-user.service';

@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanActivate, CanActivateChild {

  private roleHierarchy: Record<string, string[]> = {
    'ADMIN': ['DRIVER', 'PASSENGER'],
    'DRIVER': ['PASSENGER'],
    'PASSENGER': []
  };

  constructor(private userService: CurrentUserService, private router: Router) {}

  private roleSatisfies(userRole: string | undefined, requiredRole: string): boolean {
    if (!userRole) return false;
    if (userRole === requiredRole) return true;
    const included: string[] = this.roleHierarchy[userRole] || [];
    return included.includes(requiredRole);
  }


  private anyRoleSatisfied(userRole: string | undefined, requiredRoles: string[] | undefined): boolean {
    if (!requiredRoles || requiredRoles.length === 0) return true;
        return requiredRoles.some(req => this.roleSatisfies(userRole, req));
    }


  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot):
    Observable<boolean | UrlTree> {

    const requiredRoles = route.data?.['roles'] as string[] | undefined;

    return this.userService.currentUser$.pipe(
      take(1),
      switchMap(current => {
        if (current) return of(current);
        return this.userService.fetchMe().pipe(
            catchError(() => of(null))
        );
      }),
      map(user => {
        // Error from fetchMe, so not logged in
        if (!user) {
          this.userService.clearCurrentUser();          
          return this.router.createUrlTree(['/signIn'], { queryParams: { returnUrl: state.url }});
        }

        const role = (user as any).role as string | undefined;

        if (!requiredRoles || requiredRoles.length === 0) {
          return true;
        }

        if (this.anyRoleSatisfied(role, requiredRoles)) {
          return true;
        }

        // authenticated but not authorized
        switch(role) {
            case "ADMIN":
                return this.router.createUrlTree(["/admin/dashboard"]); break;
            case "DRIVER":
                return this.router.createUrlTree(["/driver/dashboard"]); break;
            case "PASSENGER":
                return this.router.createUrlTree(["/user/dashboard"]); break;
            default:
                return this.router.createUrlTree(["/signIn"]);
        }

      })
    );
  }

  canActivateChild(childRoute: ActivatedRouteSnapshot, state: RouterStateSnapshot) {
    return this.canActivate(childRoute, state);
  }
}
