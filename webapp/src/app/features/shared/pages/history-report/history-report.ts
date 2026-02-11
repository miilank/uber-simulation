import { ChangeDetectorRef, Component, ElementRef, inject, signal, ViewChild } from '@angular/core';
import { HistoryReportDTO, HistoryReportService, RowElementDTO } from '../../../shared/services/history-report.service';
import { User } from '../../../../core/models/user';
import { Chart, registerables } from 'chart.js';
import { FormsModule } from '@angular/forms';
import { ErrorAlert } from "../../../shared/components/error-alert";
import { CurrentUserService } from '../../../../core/services/current-user.service';

Chart.register(...registerables);

@Component({
  selector: 'app-history-report',
  imports: [FormsModule, ErrorAlert],
  templateUrl: './history-report.html',
})
export class UserHistoryReport {
  @ViewChild('dailyRidesChart') dailyRidesCanvas?: ElementRef<HTMLCanvasElement>;
  @ViewChild('dailyMoneyChart') dailyMoneyCanvas?: ElementRef<HTMLCanvasElement>;
  @ViewChild('dailyKmsChart') dailyKmsCanvas?: ElementRef<HTMLCanvasElement>;
  @ViewChild('cumulativeRidesChart') cumulativeRidesCanvas?: ElementRef<HTMLCanvasElement>;
  @ViewChild('cumulativeMoneyChart') cumulativeMoneyCanvas?: ElementRef<HTMLCanvasElement>;
  @ViewChild('cumulativeKmsChart') cumulativeKmsCanvas?: ElementRef<HTMLCanvasElement>;

  historyReportService = inject(HistoryReportService);
  currentUserService = inject(CurrentUserService);
  cdr = inject(ChangeDetectorRef);

  reportData = signal<HistoryReportDTO | null>(null);
  currentUser = signal<User | null>(null);

  averageRides = signal<number>(0);
  averageKms = signal<number>(0);
  averageMoney = signal<number>(0);

  private charts: Chart[] = [];

  startDate = signal<string>('');
  endDate = signal<string>('');

  isErrorOpen = signal<boolean>(false);
  errorTitle: string = "Error";
  errorMessage: string = "Error occured during ride booking.";


  ngOnInit() {
    this.currentUserService.currentUser$
      .subscribe({
        next: user => {
          this.currentUser.set(user); 
        },
        error: err => {
          this.errorMessage =
            err?.error?.message ??
            "Could not fetch user. Please try again later.";

          this.isErrorOpen.set(true);
        }
    });
  }

  showHistoryReportUser() {
    if(!this.currentUser()) {
      this.errorMessage = "Could not fetch user. Please try again later."
      this.isErrorOpen.set(true);
      return;
    }

    if(!this.startDate() || !this.endDate()) {
      this.errorMessage = "Please select the date range."
      this.isErrorOpen.set(true);
      return;
    }

    let start: Date = new Date(this.startDate());
    let end: Date = new Date(this.endDate());

    if(end < start) {
      const tempStr: string = this.startDate();
      this.startDate.set(this.endDate());
      this.endDate.set(tempStr);

      const tempDate: Date = start;
      start = end;
      end = tempDate;
    }

    if(this.tooManyMonthsApart(start, end, 6)) {
      this.errorMessage = "Start and end date cannot be more than 6 months apart."
      this.isErrorOpen.set(true);
      return;
    }

    this.historyReportService
      .getHistoryReport(start, end)
      .subscribe({
        next: data => {
          const filled = this.fillAndComputeReport(data, start, end);
          this.reportData.set(filled);

          const numDays = filled.rows.length;
          this.averageMoney.set(filled.totalMoney / numDays);
          this.averageKms.set(filled.totalKms / numDays);
          this.averageRides.set(filled.totalRides / numDays);

          this.cdr.detectChanges();
          setTimeout(() => this.createCharts(), 0);
        },
        error: err => {
          console.error('History report error:', err);

          this.errorMessage =
            err?.error?.message ??
            "Failed to load history report. Please try again later.";

          this.isErrorOpen.set(true);
        }
      });
  }

  private createCharts() {
    if (!this.reportData()) return;

    this.charts.forEach(chart => chart.destroy());
    this.charts = [];

    const dates = this.reportData()!.rows.map(row => row.date);

    if (this.dailyRidesCanvas) {
      this.charts.push(new Chart(this.dailyRidesCanvas.nativeElement, {
        type: 'bar',
        data: {
          labels: dates,
          datasets: [{
            label: 'Number of Rides',
            data: this.reportData()!.rows.map(row => row.numberOfRides),
            backgroundColor: 'rgb(192, 236, 78)',
            borderColor: 'rgb(192, 236, 78)',
            borderWidth: 1
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: { display: false }
          }
        }
      }));
    }

    // Daily Money Chart
    if (this.dailyMoneyCanvas) {
      this.charts.push(new Chart(this.dailyMoneyCanvas.nativeElement, {
        type: 'line',
        data: {
          labels: dates,
          datasets: [{
            label: 'Money ($)',
            data: this.reportData()!.rows.map(row => row.money),
            backgroundColor: 'rgba(192, 236, 78, 0.3)',
            borderColor: 'rgb(192, 236, 78)',
            borderWidth: 2,
            fill: true,
            tension: 0.4
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: { display: false }
          }
        }
      }));
    }

    // Daily Kms Chart
    if (this.dailyKmsCanvas) {
      this.charts.push(new Chart(this.dailyKmsCanvas.nativeElement, {
        type: 'line',
        data: {
          labels: dates,
          datasets: [{
            label: 'Distance (km)',
            data: this.reportData()!.rows.map(row => row.kms),
            backgroundColor: 'rgba(192, 236, 78, 0)',
            borderColor: 'rgb(192, 236, 78)',
            borderWidth: 2,
            fill: true,
            tension: 0.4
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: { display: false }
          }
        }
      }));
    }

    // Cumulative Rides Chart
    if (this.cumulativeRidesCanvas) {
      this.charts.push(new Chart(this.cumulativeRidesCanvas.nativeElement, {
        type: 'line',
        data: {
          labels: dates,
          datasets: [{
            label: 'Cumulative Rides',
            data: this.reportData()!.cumulativeRides,
            backgroundColor: 'rgba(192, 236, 78, 0.4)',
            borderColor: 'rgb(192, 236, 78)',
            borderWidth: 2,
            fill: true,
            tension: 0.4
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: { display: false }
          }
        }
      }));
    }

    // Cumulative Money Chart
    if (this.cumulativeMoneyCanvas) {
      this.charts.push(new Chart(this.cumulativeMoneyCanvas.nativeElement, {
        type: 'line',
        data: {
          labels: dates,
          datasets: [{
            label: 'Cumulative Money ($)',
            data: this.reportData()!.cumulativeMoney,
            backgroundColor: 'rgba(192, 236, 78, 0.3)',
            borderColor: 'rgb(192, 236, 78)',
            borderWidth: 2,
            fill: true,
            tension: 0.4
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: { display: false }
          }
        }
      }));
    }

    if (this.cumulativeKmsCanvas) {
      this.charts.push(new Chart(this.cumulativeKmsCanvas.nativeElement, {
        type: 'line',
        data: {
          labels: dates,
          datasets: [{
            label: 'Cumulative Distance (km)',
            data: this.reportData()!.cumulativeKms,
            backgroundColor: 'rgba(192, 236, 78, 0)',
            borderColor: 'rgb(192, 236, 78)',
            borderWidth: 2,
            fill: true,
            tension: 0.4
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: { display: false }
          }
        }
      }));
    }
  }

  private fillAndComputeReport(raw: HistoryReportDTO, start: Date, end: Date): HistoryReportDTO {
    const formatDate = (d: Date) => d.toISOString().slice(0, 10);
    const map = new Map<string, RowElementDTO>();
    (raw.rows || []).forEach(r => map.set(r.date, { ...r }));

    const rows: RowElementDTO[] = [];
    for (let d = new Date(start.getTime()); d <= end; d.setDate(d.getDate() + 1)) {
      const key = formatDate(new Date(d));
      if (map.has(key)) {
        rows.push(map.get(key)!);
      } else {
        rows.push({ date: key, numberOfRides: 0, money: 0, kms: 0 });
      }
    }

    const cumulativeRides: number[] = [];
    const cumulativeMoney: number[] = [];
    const cumulativeKms: number[] = [];

    let ridesAcc = 0;
    let moneyAcc = 0;
    let kmsAcc = 0;

    rows.forEach(r => {
      ridesAcc += r.numberOfRides;
      moneyAcc += r.money;
      kmsAcc += r.kms;

      cumulativeRides.push(ridesAcc);
      cumulativeMoney.push(Math.round((moneyAcc + Number.EPSILON) * 100) / 100);
      cumulativeKms.push(Math.round((kmsAcc + Number.EPSILON) * 100) / 100);
    });

    const totalMoney = Math.round((moneyAcc + Number.EPSILON) * 100) / 100;
    const totalKms = Math.round((kmsAcc + Number.EPSILON) * 100) / 100;
    const totalRides = ridesAcc;

    return {
      rows,
      cumulativeMoney,
      cumulativeKms,
      cumulativeRides,
      totalMoney,
      totalKms,
      totalRides
    };
  }

  tooManyMonthsApart(d1: Date, d2: Date, limit:number) {
    if (d1 > d2) [d1, d2] = [d2, d1];

    const plusMonths = new Date(d1);
    plusMonths.setMonth(plusMonths.getMonth() + limit);

    return d2 > plusMonths;
  }

  closeErrorAlert(): void {
    this.isErrorOpen.set(false);
  }

  ngOnDestroy() {
    this.charts.forEach(chart => chart.destroy());
  }
}
