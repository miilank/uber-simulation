import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RatingModalComponent } from './rating-modal.component';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

describe('RatingModalComponent', () => {
  let component: RatingModalComponent;
  let fixture: ComponentFixture<RatingModalComponent>;
  let compiled: HTMLElement;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RatingModalComponent, CommonModule, FormsModule]
    }).compileComponents();

    fixture = TestBed.createComponent(RatingModalComponent);
    component = fixture.componentInstance;
    compiled = fixture.nativeElement as HTMLElement;
  });

  it('should create the component', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  // POZITIVNI TESTOVI

  describe('Pozitivni testovi - Osnovna funkcionalnost', () => {

    it('should not display modal when isOpen is false', () => {
      component.isOpen = false;
      fixture.detectChanges();

      const modal = compiled.querySelector('.fixed.inset-0');
      expect(modal).toBeNull();
    });

    it('should display modal when isOpen is true', () => {
      component.isOpen = true;
      fixture.detectChanges();

      const modal = compiled.querySelector('.fixed.inset-0');
      expect(modal).not.toBeNull();
    });

    it('should display title "Rate Your Ride"', () => {
      component.isOpen = true;
      fixture.detectChanges();

      const title = compiled.querySelector('h2');
      expect(title?.textContent?.trim()).toContain('Rate Your Ride');
    });

    it('should set driver rating when clicking on star', () => {
      component.isOpen = true;
      fixture.detectChanges();

      component.setDriverRating(4);

      expect(component.driverRating).toBe(4);
    });

    it('should allow changing driver rating', () => {
      component.setDriverRating(3);
      expect(component.driverRating).toBe(3);

      component.setDriverRating(5);
      expect(component.driverRating).toBe(5);
    });

    it('should set vehicle rating when clicking on star', () => {
      component.isOpen = true;
      fixture.detectChanges();

      component.setVehicleRating(5);

      expect(component.vehicleRating).toBe(5);
    });

    it('should update comment when user types', () => {
      component.isOpen = true;
      fixture.detectChanges();

      const testComment = 'Odlicna voznja!';
      component.comment = testComment;

      expect(component.comment).toBe(testComment);
    });

    it('should emit submit event with correct data when form is valid', () => {
      const submitSpy = jasmine.createSpy('submit');
      component.submit.emit = submitSpy;

      component.driverRating = 5;
      component.vehicleRating = 4;
      component.comment = 'Sve super!';

      component.onSubmit();

      expect(submitSpy).toHaveBeenCalledWith({
        driverRating: 5,
        vehicleRating: 4,
        comment: 'Sve super!'
      });
    });

    it('should trim comment before submitting', () => {
      const submitSpy = jasmine.createSpy('submit');
      component.submit.emit = submitSpy;

      component.driverRating = 5;
      component.vehicleRating = 4;
      component.comment = '  Komentar sa spacevima  ';

      component.onSubmit();

      expect(submitSpy).toHaveBeenCalledWith({
        driverRating: 5,
        vehicleRating: 4,
        comment: 'Komentar sa spacevima'
      });
    });

    it('should reset form after successful submit', () => {
      component.driverRating = 5;
      component.vehicleRating = 4;
      component.comment = 'Test';

      component.resetForm();

      expect(component.driverRating).toBe(0);
      expect(component.vehicleRating).toBe(0);
      expect(component.comment).toBe('');
    });

    it('should emit close event when Cancel button is clicked', () => {
      const closeSpy = jasmine.createSpy('close');
      component.close.emit = closeSpy;

      component.onCancel();

      expect(closeSpy).toHaveBeenCalled();
    });

    it('should emit close event when x button is clicked', () => {
      const closeSpy = jasmine.createSpy('close');
      component.close.emit = closeSpy;

      component.onCancel();

      expect(closeSpy).toHaveBeenCalled();
    });

    it('should emit close event when backdrop is clicked', () => {
      const closeSpy = jasmine.createSpy('close');
      component.close.emit = closeSpy;

      const event = new MouseEvent('click');
      component.onBackdropClick(event);

      expect(closeSpy).toHaveBeenCalled();
    });
  });

  // NEGATIVNI TESTOVI - VALIDACIJA

  describe('Negativni testovi - Validacija forme', () => {

    it('should return false from isValid() when both ratings are 0', () => {
      component.driverRating = 0;
      component.vehicleRating = 0;

      expect(component.isValid()).toBe(false);
    });

    it('should return false from isValid() when only driver rating is set', () => {
      component.driverRating = 5;
      component.vehicleRating = 0;

      expect(component.isValid()).toBe(false);
    });

    it('should return false from isValid() when only vehicle rating is set', () => {
      component.driverRating = 0;
      component.vehicleRating = 5;

      expect(component.isValid()).toBe(false);
    });

    it('should return true from isValid() when both ratings are set', () => {
      component.driverRating = 4;
      component.vehicleRating = 3;

      expect(component.isValid()).toBe(true);
    });

    it('should NOT emit submit when form is invalid', () => {
      const submitSpy = jasmine.createSpy('submit');
      component.submit.emit = submitSpy;

      component.driverRating = 0;
      component.vehicleRating = 0;

      component.onSubmit();

      expect(submitSpy).not.toHaveBeenCalled();
    });

    it('should disable Submit button when form is invalid', () => {
      component.isOpen = true;
      component.driverRating = 0;
      component.vehicleRating = 0;
      fixture.detectChanges();

      const submitButton = Array.from(compiled.querySelectorAll('button'))
        .find(btn => btn.textContent?.includes('Submit Rating')) as HTMLButtonElement;

      expect(submitButton?.disabled).toBe(true);
    });

    it('should enable Submit button when form is valid', () => {
      component.isOpen = true;
      component.driverRating = 5;
      component.vehicleRating = 4;
      fixture.detectChanges();

      const submitButton = Array.from(compiled.querySelectorAll('button'))
        .find(btn => btn.textContent?.includes('Submit Rating')) as HTMLButtonElement;

      expect(submitButton?.disabled).toBe(false);
    });
  });

  // GRANICNI SLUCAJEVI

  describe('Granicni slucajevi', () => {

    it('should accept minimum driver rating (1 star)', () => {
      component.setDriverRating(1);

      expect(component.driverRating).toBe(1);
    });

    it('should accept maximum driver rating (5 stars)', () => {
      component.setDriverRating(5);

      expect(component.driverRating).toBe(5);
    });

    it('should accept minimum vehicle rating (1 star)', () => {
      component.setVehicleRating(1);

      expect(component.vehicleRating).toBe(1);
    });

    it('should accept maximum vehicle rating (5 stars)', () => {
      component.setVehicleRating(5);

      expect(component.vehicleRating).toBe(5);
    });

    it('should accept empty comment (0 characters)', () => {
      component.driverRating = 5;
      component.vehicleRating = 4;
      component.comment = '';

      expect(component.isValid()).toBe(true);
    });

    it('should display comment length counter at 0/500', () => {
      component.isOpen = true;
      component.comment = '';
      fixture.detectChanges();

      const counter = Array.from(compiled.querySelectorAll('.text-xs.text-gray-500'))
        .find(el => el.textContent?.includes('/500'));

      expect(counter?.textContent).toContain('0/500');
    });

    it('should accept comment with exactly 500 characters', () => {
      component.isOpen = true;
      component.comment = 'a'.repeat(500);
      fixture.detectChanges();

      const counter = Array.from(compiled.querySelectorAll('.text-xs.text-gray-500'))
        .find(el => el.textContent?.includes('/500'));

      expect(counter?.textContent).toContain('500/500');
      expect(component.comment.length).toBe(500);
    });

    it('should have maxlength attribute set to 500 on textarea', () => {
      component.isOpen = true;
      fixture.detectChanges();

      const textarea = compiled.querySelector('textarea') as HTMLTextAreaElement;

      expect(textarea?.maxLength).toBe(500);
    });

    it('should submit valid form with empty comment', () => {
      const submitSpy = jasmine.createSpy('submit');
      component.submit.emit = submitSpy;

      component.driverRating = 5;
      component.vehicleRating = 4;
      component.comment = '';

      component.onSubmit();

      expect(submitSpy).toHaveBeenCalledWith({
        driverRating: 5,
        vehicleRating: 4,
        comment: ''
      });
    });
  });

  // UI INTERAKCIJA

  describe('UI interakcija - Zvijezde', () => {

    it('should highlight stars up to selected driver rating', () => {
      component.isOpen = true;
      component.driverRating = 3;
      fixture.detectChanges();

      const labels = Array.from(compiled.querySelectorAll('label'));
      const driverLabel = labels.find(label => label.textContent?.includes('Driver Rating'));

      const driverSection = driverLabel?.parentElement;
      const stars = driverSection?.querySelectorAll('button');

      expect(stars?.[0].classList.contains('text-yellow-400')).toBe(true);
      expect(stars?.[1].classList.contains('text-yellow-400')).toBe(true);
      expect(stars?.[2].classList.contains('text-yellow-400')).toBe(true);
      expect(stars?.[3].classList.contains('text-gray-300')).toBe(true);
      expect(stars?.[4].classList.contains('text-gray-300')).toBe(true);
    });

    it('should highlight stars up to selected vehicle rating', () => {
      component.isOpen = true;
      component.vehicleRating = 4;
      fixture.detectChanges();

      const labels = Array.from(compiled.querySelectorAll('label'));
      const vehicleLabel = labels.find(label => label.textContent?.includes('Vehicle Rating'));

      const vehicleSection = vehicleLabel?.parentElement;
      const stars = vehicleSection?.querySelectorAll('button');

      expect(stars?.[0].classList.contains('text-yellow-400')).toBe(true);
      expect(stars?.[1].classList.contains('text-yellow-400')).toBe(true);
      expect(stars?.[2].classList.contains('text-yellow-400')).toBe(true);
      expect(stars?.[3].classList.contains('text-yellow-400')).toBe(true);
      expect(stars?.[4].classList.contains('text-gray-300')).toBe(true);
    });

    it('should update driver rating when star button is clicked in UI', () => {
      component.isOpen = true;
      fixture.detectChanges();

      const labels = Array.from(compiled.querySelectorAll('label'));
      const driverLabel = labels.find(label => label.textContent?.includes('Driver Rating'));

      const driverSection = driverLabel?.parentElement;
      const stars = driverSection?.querySelectorAll('button') as NodeListOf<HTMLButtonElement>;

      stars[3].click();
      fixture.detectChanges();

      expect(component.driverRating).toBe(4);
    });

    it('should update vehicle rating when star button is clicked in UI', () => {
      component.isOpen = true;
      fixture.detectChanges();

      const labels = Array.from(compiled.querySelectorAll('label'));
      const vehicleLabel = labels.find(label => label.textContent?.includes('Vehicle Rating'));

      const vehicleSection = vehicleLabel?.parentElement;
      const stars = vehicleSection?.querySelectorAll('button') as NodeListOf<HTMLButtonElement>;

      stars[2].click();
      fixture.detectChanges();

      expect(component.vehicleRating).toBe(3);
    });
  });

  // KOMPLETNI WORKFLOW

  describe('Kompletni workflow ocenjivanja', () => {

    it('should complete full rating workflow successfully', () => {
      const submitSpy = jasmine.createSpy('submit');
      const closeSpy = jasmine.createSpy('close');
      component.submit.emit = submitSpy;
      component.close.emit = closeSpy;

      component.isOpen = true;
      fixture.detectChanges();

      component.setDriverRating(5);

      component.setVehicleRating(4);

      component.comment = 'Odlicna voznja, vozac veoma ljubazan!';

      expect(component.isValid()).toBe(true);

      component.onSubmit();

      expect(submitSpy).toHaveBeenCalledWith({
        driverRating: 5,
        vehicleRating: 4,
        comment: 'Odlicna voznja, vozac veoma ljubazan!'
      });

      expect(component.driverRating).toBe(0);
      expect(component.vehicleRating).toBe(0);
      expect(component.comment).toBe('');
    });

    it('should handle cancel workflow correctly', () => {
      const closeSpy = jasmine.createSpy('close');
      const submitSpy = jasmine.createSpy('submit');
      component.close.emit = closeSpy;
      component.submit.emit = submitSpy;

      component.isOpen = true;
      component.driverRating = 5;
      component.vehicleRating = 4;
      component.comment = 'Test komentar';
      fixture.detectChanges();

      component.onCancel();

      expect(submitSpy).not.toHaveBeenCalled();

      expect(closeSpy).toHaveBeenCalled();
    });
  });
});
