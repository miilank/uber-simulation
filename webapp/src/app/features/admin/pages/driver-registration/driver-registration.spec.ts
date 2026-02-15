import { ComponentFixture, fakeAsync, flushMicrotasks, TestBed, tick } from '@angular/core/testing';

import { DriverRegistration } from './driver-registration';
import { FormsModule, NgModel } from '@angular/forms';
import { ChangeDetectorRef, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { DriverService } from '../../../shared/services/driver.service';
import { Router } from '@angular/router';
import { HttpErrorResponse, provideHttpClient } from '@angular/common/http'
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing'
import { VehicleType } from '../../../shared/models/vehicle';
import { ConfigService } from '../../../../core/services/config.service';
import { By } from '@angular/platform-browser';
import { of, throwError } from 'rxjs';

describe('DriverRegistration', () => {
  let component: DriverRegistration;
  let fixture: ComponentFixture<DriverRegistration>;

  let mockDriverService: jasmine.SpyObj<DriverService>;
  let mockRouter: { navigate: jasmine.Spy };
  let compiled: HTMLElement;

  beforeEach(async () => {
    mockDriverService = jasmine.createSpyObj('DriverService', ['createDriver']);
    mockRouter = { navigate: jasmine.createSpy('navigate') }

    TestBed.configureTestingModule({
      imports: [FormsModule, DriverRegistration],
      declarations: [],
      providers: [
        { provide: DriverService, useValue: mockDriverService },
        { provide: Router, useValue: mockRouter }   
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(DriverRegistration);
    component = fixture.componentInstance;

    fixture.detectChanges();
    compiled = fixture.nativeElement;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize vehicles from types', () => {
    component.ngOnInit();
    expect(component.vehicleTypes).toEqual(Object.values(VehicleType));
  })

  it('should disable button when input is empty', () => {
    fixture.detectChanges();

    const btn : HTMLButtonElement | null = compiled.querySelector("button[type='submit']");
    expect(btn).toBeTruthy();
    expect(btn!.disabled).toBeTrue();
  })

  // Valid form
  it('should enable submit button when required inputs are filled', async () => {

    const setInput = (name: string, value: string) => {
        const input = compiled.querySelector(`input[name='${name}']`) as HTMLInputElement;
        expect(input).toBeTruthy();
        input.value = value;
        input.dispatchEvent(new Event('input'));
        input.dispatchEvent(new Event('blur'));
      };

      setInput('firstName', 'Driver');
      setInput('lastName', 'Driveric');
      setInput('email', 'driver@mail.com');
      setInput('address', 'Street');
      setInput('phone', '1111111111');
      setInput('vehicleModel', 'Car');
      setInput('licensePlate', 'LICENSEPLATE');

      const select = compiled.querySelector(`select[name='vehicleType']`) as HTMLSelectElement;
      expect(select).toBeTruthy();

      select.value = select.options[0].value;
      select.dispatchEvent(new Event('change'));
      select.dispatchEvent(new Event('blur'));

      const passengersInput = compiled.querySelector(`input[name='passengers']`) as HTMLInputElement;
      expect(passengersInput).toBeTruthy();
      passengersInput.value = '2';
      passengersInput.dispatchEvent(new Event('input'));
      passengersInput.dispatchEvent(new Event('blur'));

      fixture.detectChanges();
      await fixture.whenStable();

      const submitBtn = compiled.querySelector("button[type='submit']") as HTMLButtonElement;
      expect(submitBtn.disabled).toBeFalse();
    });

  // Invalid form
  it('should mark firstName input invalid when shorter than minlength (2)', () => {
    const inputDebugEl = fixture.debugElement.query(By.css('input[name="firstName"]'));
    const input = inputDebugEl.nativeElement as HTMLInputElement;
    const ngModel = inputDebugEl.injector.get(NgModel);
    
    expect(input).toBeTruthy();
    input.value = 'A';
    input.dispatchEvent(new Event('input'));
    fixture.detectChanges();
    
    expect(ngModel.invalid).toBeTrue();
    expect(ngModel.errors?.['minlength']).toBeTruthy();
  });

  it('should mark phone input invalid if pattern does not match', () => {
    const inputDebugEl = fixture.debugElement.query(By.css('input[name="phone"]'));
    const input = inputDebugEl.nativeElement as HTMLInputElement;
    const ngModel = inputDebugEl.injector.get(NgModel);
    
    expect(input).toBeTruthy();
    input.value = 'abc';
    input.dispatchEvent(new Event('input'));
    fixture.detectChanges();

    expect(ngModel.invalid).toBeTrue();
    expect(ngModel.errors?.['pattern']).toBeTruthy();
  });

  it('should mark passengers input invalid when less than min', () => {
    const input = compiled.querySelector(`input[name='passengers']`) as HTMLInputElement;
    expect(input).toBeTruthy();

    input.value = '0';
    input.dispatchEvent(new Event('input'));
    input.dispatchEvent(new Event('blur'));
    fixture.detectChanges();

    expect(input.validity.valid).toBeFalse();
    expect(input.validity.rangeUnderflow).toBeTrue();
  });

  it('setPicture should set selectedPicture', () => {
    const f = new File(['a'], 'a.png', { type: 'image/png' });
    component.setPicture(f);
    expect(component['selectedPicture']).toBe(f);
  });

  // Full flow test
  it('should register driver', async () => {
    component.firstName = 'Driver';
    component.lastName = 'Driveric';
    component.email = 'driver@mail.com';
    component.address = 'Street';
    component.phone = '1111111111';
    component.vehicleModel = 'Car';
    component.vehicleType = 'Standard';
    component.licensePlate = 'LICENSEPLATE';
    component.passengers = 3;
    component.pets = true;
    component.infants = true;

    const f = new File(['a'], 'a.png', { type: 'image/png' });
    component.setPicture(f);

    fixture.detectChanges();

    let spy = mockDriverService.createDriver.and.returnValue(of(void 0));

    component.onSubmit();
    await fixture.whenStable();

    expect(mockDriverService.createDriver).toHaveBeenCalledTimes(1);

    const sentFormData: FormData = mockDriverService.createDriver.calls.mostRecent().args[0] as FormData;
    expect(sentFormData).toBeTruthy();

    const userBlob = sentFormData.get('user') as Blob | null;
    expect(userBlob).toBeInstanceOf(Blob);

    let jsonText = await userBlob!.text();

    const parsed = JSON.parse(jsonText);
    expect(parsed.email).toBe(component.email);
    expect(parsed.firstName).toBe(component.firstName);
    expect(parsed.lastName).toBe(component.lastName);
    expect(parsed.address).toBe(component.address);
    expect(parsed.phoneNumber).toBe(component.phone);

    console.log('spy call count', mockDriverService.createDriver.calls.count());
    console.log('injected driverService === mock?', component['driverService'] === mockDriverService);
    console.log('isSubmitting after submit:', component.isSubmitting);
    console.log('registrationSuccess after submit:', component.registrationSuccess);
    console.log('registrationError after submit:', component.registrationError);

    const avatar = sentFormData.get('avatar') as File | null;
    expect(avatar).toBeTruthy();
    expect((avatar as File).name).toBe(f.name);

    expect(component.isSubmitting).toBeFalse();
    expect(component.registrationSuccess).toBeTrue();
  });

  it('should set registrationError and clear isSubmitting on error response', () => {
    component.firstName = 'Driver';
    component.lastName = 'Driveric';
    component.email = 'driver@mail.com';
    component.address = 'Street';
    component.phone = '1111111111';
    component.vehicleModel = 'Car';
    component.vehicleType = 'Standard';
    component.licensePlate = 'LICENSEPLATE';
    component.passengers = 3;

    const serverErr = { error: { message: 'Server failure' } };
    mockDriverService.createDriver.and.returnValue(throwError(() => serverErr));

    component.onSubmit();

    expect(component.isSubmitting).toBeFalse();
    expect(component.registrationSuccess).toBeFalse();
    expect(component.registrationError).toBe('Server failure');
  });

  it('should fall back to generic error message if error has no message', () => {
    component.firstName = 'Driver';
    component.lastName = 'Driveric';
    component.email = 'driver@mail.com';
    component.address = 'Street';
    component.phone = '1111111111';
    component.vehicleModel = 'Car';
    component.vehicleType = 'Standard';
    component.licensePlate = 'LICENSEPLATE';
    component.passengers = 1;

    mockDriverService.createDriver.and.returnValue(throwError(() => new HttpErrorResponse({ error: null })));

    component.onSubmit();

    expect(component.isSubmitting).toBeFalse();
    expect(component.registrationSuccess).toBeFalse();
    expect(component.registrationError).toBe('Registration failed. Please try again.');
  });

  it('closeSuccessPopup should hide popup and navigate to dashboard', () => {
    component.registrationSuccess = true;
    component.email = 'driver@mail.com';

    component.closeSuccessPopup();

    expect(component.registrationSuccess).toBeFalse();
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/admin/dashboard']);
  });

});
