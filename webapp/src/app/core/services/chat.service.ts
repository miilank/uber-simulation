import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {ChatHistory, ChatMessage, ConversationPreview, SendMessageRequest} from '../../features/shared/models/chat';
import { ConfigService } from './config.service';

@Injectable({
  providedIn: 'root'
})
export class ChatService {

  constructor(
    private http: HttpClient,
    private config: ConfigService
  ) {}

  getChatHistory(): Observable<ChatHistory> {
    return this.http.get<ChatHistory>(`${this.config.chatUrl}/history`);
  }

  getChatHistoryWithUser(userId: number): Observable<ChatHistory> {
    return this.http.get<ChatHistory>(`${this.config.chatUrl}/history/${userId}`);
  }

  sendMessage(request: SendMessageRequest): Observable<ChatMessage> {
    return this.http.post<ChatMessage>(this.config.chatUrl, request);
  }

  markAsRead(senderId: number): Observable<void> {
    return this.http.post<void>(`${this.config.chatUrl}/mark-read/${senderId}`, {});
  }

  getConversations(): Observable<ConversationPreview[]> {
    return this.http.get<ConversationPreview[]>(`${this.config.chatUrl}/conversations`);
  }
}
