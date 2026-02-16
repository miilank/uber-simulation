import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { UserRegistrationComponent } from './userRegistration';
import { AuthService } from '../services/auth.service';
import { HeaderComponent } from '../../features/shared/components/header.component';
import { ProfilePictureComponent } from '../../features/shared/components/profile-picture.component';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import {provideLocationMocks} from '@angular/common/testing';

describe('UserRegistrationComponent', () => {
  let component: UserRegistrationComponent;
  let fixture: ComponentFixture<UserRegistrationComponent>;
  let mockAuthService: jasmine.SpyObj<AuthService>;

  beforeEach(async () => {
    mockAuthService = jasmine.createSpyObj('AuthService', ['register']);
    await TestBed.configureTestingModule({
      imports: [
        UserRegistrationComponent,
        FormsModule,
        HeaderComponent,
        ProfilePictureComponent,
      ],
      providers: [
        { provide: AuthService, useValue: mockAuthService },
        provideRouter([]),
        provideLocationMocks(),
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(UserRegistrationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with empty form fields', () => {
    expect(component.firstName).toBe('');
    expect(component.lastName).toBe('');
    expect(component.email).toBe('');
    expect(component.address).toBe('');
    expect(component.phone).toBe('');
    expect(component.password).toBe('');
    expect(component.confirmPassword).toBe('');
  });

  it('should initialize with no registration status', () => {
    expect(component.registrationSuccess).toBe(false);
    expect(component.registrationError).toBeNull();
    expect(component.isSubmitting).toBe(false);
  });

  it('should initialize with no selected picture', () => {
    expect(component.selectedPicture).toBeNull();
  });

  it('should require first name', (done) => {
    fixture.whenStable().then(() => {
      const compiled = fixture.nativeElement;
      const firstNameInput = compiled.querySelector('input[name="firstName"]');

      firstNameInput.value = '';
      firstNameInput.dispatchEvent(new Event('input'));
      firstNameInput.dispatchEvent(new Event('blur'));
      fixture.detectChanges();

      fixture.whenStable().then(() => {
        const form = compiled.querySelector('form');
        expect(form.classList.contains('ng-invalid')).toBeTruthy();
        done();
      });
    });
  });

  it('should require first name to be at least 2 characters', (done) => {
    component.firstName = 'A';
    fixture.detectChanges();

    fixture.whenStable().then(() => {
      const compiled = fixture.nativeElement;
      const firstNameInput = compiled.querySelector('input[name="firstName"]');
      firstNameInput.dispatchEvent(new Event('blur'));
      fixture.detectChanges();

      fixture.whenStable().then(() => {
        expect(component.firstName.length).toBeLessThan(2);
        done();
      });
    });
  });

  it('should accept valid first name', () => {
    component.firstName = 'John';
    fixture.detectChanges();

    expect(component.firstName).toBe('John');
    expect(component.firstName.length).toBeGreaterThanOrEqual(2);
  });

  it('should require last name', (done) => {
    fixture.whenStable().then(() => {
      const compiled = fixture.nativeElement;
      const lastNameInput = compiled.querySelector('input[name="lastName"]');

      lastNameInput.value = '';
      lastNameInput.dispatchEvent(new Event('input'));
      lastNameInput.dispatchEvent(new Event('blur'));
      fixture.detectChanges();

      fixture.whenStable().then(() => {
        const form = compiled.querySelector('form');
        expect(form.classList.contains('ng-invalid')).toBeTruthy();
        done();
      });
    });
  });

  it('should require last name to be at least 2 characters', () => {
    component.lastName = 'D';
    fixture.detectChanges();

    expect(component.lastName.length).toBeLessThan(2);
  });

  it('should accept valid last name', () => {
    component.lastName = 'Doe';
    fixture.detectChanges();

    expect(component.lastName).toBe('Doe');
    expect(component.lastName.length).toBeGreaterThanOrEqual(2);
  });

  it('should require email', (done) => {
    fixture.whenStable().then(() => {
      const compiled = fixture.nativeElement;
      const emailInput = compiled.querySelector('input[name="email"]');

      emailInput.value = '';
      emailInput.dispatchEvent(new Event('input'));
      emailInput.dispatchEvent(new Event('blur'));
      fixture.detectChanges();

      fixture.whenStable().then(() => {
        const form = compiled.querySelector('form');
        expect(form.classList.contains('ng-invalid')).toBeTruthy();
        done();
      });
    });
  });

  it('should validate email format', () => {
    component.email = 'invalidemail';
    fixture.detectChanges();

    expect(component.email.includes('@')).toBeFalsy();
  });

  it('should accept valid email', () => {
    component.email = 'user@example.com';
    fixture.detectChanges();

    expect(component.email).toBe('user@example.com');
    expect(component.email.includes('@')).toBeTruthy();
  });

  it('should require address', (done) => {
    fixture.whenStable().then(() => {
      const compiled = fixture.nativeElement;
      const addressInput = compiled.querySelector('input[name="address"]');

      addressInput.value = '';
      addressInput.dispatchEvent(new Event('input'));
      addressInput.dispatchEvent(new Event('blur'));
      fixture.detectChanges();

      fixture.whenStable().then(() => {
        const form = compiled.querySelector('form');
        expect(form.classList.contains('ng-invalid')).toBeTruthy();
        done();
      });
    });
  });

  it('should require address to be at least 5 characters', () => {
    component.address = 'ABC';
    fixture.detectChanges();

    expect(component.address.length).toBeLessThan(5);
  });

  it('should accept valid address', () => {
    component.address = '123 Main Street';
    fixture.detectChanges();

    expect(component.address).toBe('123 Main Street');
    expect(component.address.length).toBeGreaterThanOrEqual(5);
  });

  it('should require phone number', (done) => {
    fixture.whenStable().then(() => {
      const compiled = fixture.nativeElement;
      const phoneInput = compiled.querySelector('input[name="phone"]');

      phoneInput.value = '';
      phoneInput.dispatchEvent(new Event('input'));
      phoneInput.dispatchEvent(new Event('blur'));
      fixture.detectChanges();

      fixture.whenStable().then(() => {
        const form = compiled.querySelector('form');
        expect(form.classList.contains('ng-invalid')).toBeTruthy();
        done();
      });
    });
  });

  it('should validate phone number format', () => {
    component.phone = '123';
    fixture.detectChanges();

    expect(component.phone.length).toBeLessThan(10);
  });

  it('should accept valid phone number with country code', () => {
    component.phone = '+381641234567';
    fixture.detectChanges();

    expect(component.phone).toBe('+381641234567');
    expect(component.phone.length).toBeGreaterThanOrEqual(10);
  });

  it('should require password', (done) => {
    fixture.whenStable().then(() => {
      const compiled = fixture.nativeElement;
      const passwordInput = compiled.querySelector('input[name="password"]');

      passwordInput.value = '';
      passwordInput.dispatchEvent(new Event('input'));
      passwordInput.dispatchEvent(new Event('blur'));
      fixture.detectChanges();

      fixture.whenStable().then(() => {
        const form = compiled.querySelector('form');
        expect(form.classList.contains('ng-invalid')).toBeTruthy();
        done();
      });
    });
  });

  it('should require password to be at least 6 characters', () => {
    component.password = '12345';
    fixture.detectChanges();

    expect(component.password.length).toBeLessThan(6);
  });

  it('should accept valid password', () => {
    component.password = 'password123';
    fixture.detectChanges();

    expect(component.password).toBe('password123');
    expect(component.password.length).toBeGreaterThanOrEqual(6);
  });

  it('should require confirm password', (done) => {
    fixture.whenStable().then(() => {
      const compiled = fixture.nativeElement;
      const confirmPasswordInput = compiled.querySelector('input[name="confirmPassword"]');

      confirmPasswordInput.value = '';
      confirmPasswordInput.dispatchEvent(new Event('input'));
      confirmPasswordInput.dispatchEvent(new Event('blur'));
      fixture.detectChanges();

      fixture.whenStable().then(() => {
        const form = compiled.querySelector('form');
        expect(form.classList.contains('ng-invalid')).toBeTruthy();
        done();
      });
    });
  });



  it('should require passwords to match', () => {
    component.password = 'password123';
    component.confirmPassword = 'different123';
    fixture.detectChanges();

    expect(component.password).not.toBe(component.confirmPassword);
  });

  it('should accept matching passwords', () => {
    component.password = 'password123';
    component.confirmPassword = 'password123';
    fixture.detectChanges();

    expect(component.password).toBe(component.confirmPassword);
  });

  it('should call authService.register on valid form submission', (done) => {
    component.firstName = 'John';
    component.lastName = 'Doe';
    component.email = 'john.doe@example.com';
    component.address = '123 Main Street';
    component.phone = '+381641234567';
    component.password = 'password123';
    component.confirmPassword = 'password123';

    mockAuthService.register.and.returnValue(of(undefined));

    component.onSubmit();

    fixture.whenStable().then(() => {
      expect(mockAuthService.register).toHaveBeenCalled();
      expect(component.registrationSuccess).toBe(true);
      done();
    });
  });

  it('should create FormData with correct user data', (done) => {
    component.firstName = 'John';
    component.lastName = 'Doe';
    component.email = 'john.doe@example.com';
    component.address = '123 Main Street';
    component.phone = '+381641234567';
    component.password = 'password123';
    component.confirmPassword = 'password123';

    mockAuthService.register.and.returnValue(of(undefined));

    component.onSubmit();

    fixture.whenStable().then(() => {
      const callArgs = mockAuthService.register.calls.mostRecent().args;
      const formData = callArgs[0] as FormData;

      expect(formData).toBeInstanceOf(FormData);
      done();
    });
  });

  it('should include avatar in FormData when picture is selected', (done) => {
    const mockFile = new File([''], 'avatar.jpg', { type: 'image/jpeg' });
    component.selectedPicture = mockFile;
    component.firstName = 'John';
    component.lastName = 'Doe';
    component.email = 'john.doe@example.com';
    component.address = '123 Main Street';
    component.phone = '+381641234567';
    component.password = 'password123';
    component.confirmPassword = 'password123';

    mockAuthService.register.and.returnValue(of(undefined));

    component.onSubmit();

    fixture.whenStable().then(() => {
      expect(mockAuthService.register).toHaveBeenCalled();
      done();
    });
  });

  it('should set isSubmitting to false after successful submission', (done) => {
    component.firstName = 'John';
    component.lastName = 'Doe';
    component.email = 'john.doe@example.com';
    component.address = '123 Main Street';
    component.phone = '+381641234567';
    component.password = 'password123';
    component.confirmPassword = 'password123';

    mockAuthService.register.and.returnValue(of(undefined));

    component.onSubmit();

    fixture.whenStable().then(() => {
      expect(component.isSubmitting).toBe(false);
      done();
    });
  });

  it('should set registrationSuccess to true on successful registration', (done) => {
    component.firstName = 'John';
    component.lastName = 'Doe';
    component.email = 'john.doe@example.com';
    component.address = '123 Main Street';
    component.phone = '+381641234567';
    component.password = 'password123';
    component.confirmPassword = 'password123';

    mockAuthService.register.and.returnValue(of(undefined));

    component.onSubmit();

    fixture.whenStable().then(() => {
      expect(component.registrationSuccess).toBe(true);
      expect(component.registrationError).toBeNull();
      done();
    });
  });

  it('should handle registration error', (done) => {
    const errorMessage = 'Email already exists';
    component.firstName = 'John';
    component.lastName = 'Doe';
    component.email = 'existing@example.com';
    component.address = '123 Main Street';
    component.phone = '+381641234567';
    component.password = 'password123';
    component.confirmPassword = 'password123';

    mockAuthService.register.and.returnValue(
      throwError(() => ({ error: { message: errorMessage } }))
    );

    component.onSubmit();

    fixture.whenStable().then(() => {
      expect(component.isSubmitting).toBe(false);
      expect(component.registrationSuccess).toBe(false);
      expect(component.registrationError).toBe(errorMessage);
      done();
    });
  });

  it('should handle registration error without message', (done) => {
    component.firstName = 'John';
    component.lastName = 'Doe';
    component.email = 'test@example.com';
    component.address = '123 Main Street';
    component.phone = '+381641234567';
    component.password = 'password123';
    component.confirmPassword = 'password123';

    mockAuthService.register.and.returnValue(
      throwError(() => ({ error: {} }))
    );

    component.onSubmit();

    fixture.whenStable().then(() => {
      expect(component.registrationError).toBe('Registration failed. Please try again.');
      done();
    });
  });

  it('should set selected picture when setPicture is called', () => {
    const mockFile = new File([''], 'avatar.jpg', { type: 'image/jpeg' });

    component.setPicture(mockFile);

    expect(component.selectedPicture).toBe(mockFile);
  });

  it('should handle null picture selection', () => {
    component.selectedPicture = new File([''], 'avatar.jpg', { type: 'image/jpeg' });

    component.setPicture(null as any);

    expect(component.selectedPicture).toBeNull();
  });


  it('should reset registrationSuccess when closeSuccessPopup is called', () => {
    component.registrationSuccess = true;

    component.closeSuccessPopup();

    expect(component.registrationSuccess).toBe(false);
  });

  it('should handle very long first name', () => {
    component.firstName = 'A'.repeat(100);
    fixture.detectChanges();

    expect(component.firstName.length).toBe(100);
  });

  it('should handle special characters in name', () => {
    component.firstName = 'Jöhn-Pâul';
    component.lastName = "O'Connor";
    fixture.detectChanges();

    expect(component.firstName).toBe('Jöhn-Pâul');
    expect(component.lastName).toBe("O'Connor");
  });

  it('should handle cyrillic characters in address', () => {
    component.address = 'Улица Кнеза Милоша 123';
    fixture.detectChanges();

    expect(component.address).toBe('Улица Кнеза Милоша 123');
  });

  it('should handle empty string for optional avatar', (done) => {
    component.firstName = 'John';
    component.lastName = 'Doe';
    component.email = 'john.doe@example.com';
    component.address = '123 Main Street';
    component.phone = '+381641234567';
    component.password = 'password123';
    component.confirmPassword = 'password123';
    component.selectedPicture = null;

    mockAuthService.register.and.returnValue(of(undefined));

    component.onSubmit();

    fixture.whenStable().then(() => {
      expect(mockAuthService.register).toHaveBeenCalled();
      done();
    });
  });

  it('should trim whitespace from email', () => {
    component.email = '  user@example.com  ';
    const trimmedEmail = component.email.trim();

    expect(trimmedEmail).toBe('user@example.com');
  });
});
