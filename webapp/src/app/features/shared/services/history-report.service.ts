import { HttpClient, HttpParams } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { ConfigService } from "../../../core/services/config.service";
import { Observable } from "rxjs";

export interface HistoryReportDTO {
  rows: RowElementDTO[];
  cumulativeMoney: number[];
  cumulativeKms: number[];
  cumulativeRides: number[];
  totalMoney: number;
  totalKms: number;
  totalRides: number;
}

export interface RowElementDTO {
  date: string;
  numberOfRides: number;
  money: number;
  kms: number;
}

@Injectable({
  providedIn: 'root'
})
export class HistoryReportService {
    constructor(
        private http: HttpClient,
        private config: ConfigService
    ) {}

    getHistoryReport(from: Date, to: Date, uuid?: number): Observable<HistoryReportDTO> {
        let params = new HttpParams()
        .set('from', this.formatDate(from))
        .set('to', this.formatDate(to));
        
        if (uuid !== undefined && uuid !== null) {
        params = params.set('uuid', uuid.toString());
        }

        return this.http.get<HistoryReportDTO>(this.config.historyReportUrl, { params });
    }

  private formatDate(date: Date): string {
    return date.toISOString().split('T')[0];
  }
}