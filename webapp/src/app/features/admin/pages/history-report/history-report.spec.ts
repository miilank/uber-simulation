import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HistoryReport } from './history-report';

describe('HistoryReport', () => {
  let component: HistoryReport;
  let fixture: ComponentFixture<HistoryReport>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HistoryReport]
    })
    .compileComponents();

    fixture = TestBed.createComponent(HistoryReport);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
