import { HttpClient } from '@angular/common/http';
import { Injectable, signal } from '@angular/core';
import { interval } from 'rxjs';
import { ConfigService } from '../../../core/services/config.service';

export interface PanicDTO {
  id: number;
  rideId: number;
  activatedBy: string;
  driverId: number;
  timestamp: string;
  userType: 'DRIVER' | 'PASSENGER';
}
@Injectable({providedIn: 'root'})
export class PanicsService {
  unresolvedPanics = signal<PanicDTO[]>([]);

  constructor(private http: HttpClient, private configService: ConfigService) {
    this.loadPanics();
    interval(5000).subscribe(() => this.loadPanics());
  }

  loadPanics() {
    this.http.get<PanicDTO[]>(this.configService.getPanicsUrl).subscribe(panics => {
      this.unresolvedPanics.set(panics);
    });
  }

  resolvePanic(id: number) {
    this.http.put(`${this.configService.getPanicsUrl}/${id}/resolve`, {}).subscribe(() => {
      this.unresolvedPanics.update(p => p.filter(p => p.id !== id));
    });
  }
}
