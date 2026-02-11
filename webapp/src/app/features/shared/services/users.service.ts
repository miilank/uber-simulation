import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { ConfigService } from "../../../core/services/config.service";
import { User } from "../../../core/models/user";
import { Observable } from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class UserService {
    constructor(
        private http: HttpClient,
        private config: ConfigService
    ) {}

    searchUsers(searchString: string, pageSize: number, pageNumber: number) : Observable<User[]> {
        return this.http.get<User[]>(this.config.searchUsersUrl(searchString, pageSize, pageNumber));
    }

    blockUser(userId: number, blockReason: string) : Observable<void> {
        return this.http.put<void>(this.config.blockUserUrl(userId), blockReason);
    }

    unblockUser(userId: number) : Observable<void> {
        return this.http.put<void>(this.config.unblockUserUrl(userId), {});
    }
}