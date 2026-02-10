import {Component, OnInit, OnDestroy, ViewChild, ElementRef, AfterViewChecked, ChangeDetectorRef} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';
import { ChatService } from '../../../../core/services/chat.service';
import { WebSocketService } from '../../../../core/services/websocket.service';
import { CurrentUserService } from '../../../../core/services/current-user.service';
import {ChatHistory, ChatMessage, SendMessageRequest} from '../../models/chat';

interface MessageGroup {
  date: string;
  messages: ChatMessage[];
}

@Component({
  selector: 'app-support-chat',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './support-chat.html',
})
export class SupportChatComponent implements OnInit, OnDestroy, AfterViewChecked {
  @ViewChild('messagesContainer') private messagesContainer!: ElementRef<HTMLDivElement>;

  chatHistory: ChatHistory | null = null;
  newMessage = '';
  currentUserId: number | null = null;

  private messageSubscription?: Subscription;
  private userSubscription?: Subscription;
  private shouldScrollToBottom = false;

  constructor(
    private chatService: ChatService,
    private websocketService: WebSocketService,
    private currentUserService: CurrentUserService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.userSubscription = this.currentUserService.currentUser$.subscribe({
      next: (user) => {
        if (user?.id) {
          this.currentUserId = user.id;
          this.loadChatHistory();
          this.connectWebSocket();
        }
      }
    });
  }

  ngAfterViewChecked(): void {
    if (this.shouldScrollToBottom) {
      this.scrollToBottom();
      this.shouldScrollToBottom = false;
    }
  }

  ngOnDestroy(): void {
    this.messageSubscription?.unsubscribe();
    this.userSubscription?.unsubscribe();
    this.websocketService.disconnect();
  }

  private loadChatHistory(): void {
    this.chatService.getChatHistory().subscribe({
      next: (history) => {
        this.chatHistory = history;
        this.shouldScrollToBottom = true;
        this.cdr.detectChanges();

        if (history.otherUserId) {
          this.chatService.markAsRead(history.otherUserId).subscribe();
        }
      }
    });
  }

  private connectWebSocket(): void {
    if (!this.currentUserId) return;

    this.websocketService.connect(this.currentUserId).then(() => {
      this.messageSubscription = this.websocketService.messages$.subscribe({
        next: (message) => {
          if (message && this.chatHistory) {
            this.chatHistory.messages.push(message);
            this.shouldScrollToBottom = true;
            this.cdr.detectChanges();

            if (message.senderId === this.chatHistory.otherUserId) {
              this.chatService.markAsRead(message.senderId).subscribe();
            }
          }
        }
      });
    });
  }

  sendMessage(): void {
    if (!this.newMessage.trim() || !this.chatHistory) return;

    const request: SendMessageRequest = {
      recipientId: this.chatHistory.otherUserId,
      message: this.newMessage.trim()
    };

    this.chatService.sendMessage(request).subscribe({
      next: (message) => {
        if (this.chatHistory) {
          this.chatHistory.messages.push(message);
          this.newMessage = '';
          this.shouldScrollToBottom = true;
          this.cdr.detectChanges();
        }
      }
    });
  }

  private scrollToBottom(): void {
    try {
      const container = this.messagesContainer.nativeElement;
      container.scrollTop = container.scrollHeight;
    } catch (err) {}
  }

  isMyMessage(message: ChatMessage): boolean {
    return message.senderId === this.currentUserId;
  }

  formatTime(timestamp: string): string {
    const date = new Date(timestamp);
    return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`;
  }

  groupMessagesByDate(messages: ChatMessage[]): MessageGroup[] {
    const groups: Record<string, ChatMessage[]> = {};

    messages.forEach(msg => {
      const date = new Date(msg.createdAt).toLocaleDateString('en-US');
      if (!groups[date]) groups[date] = [];
      groups[date].push(msg);
    });

    return Object.keys(groups).map(date => ({
      date: this.formatDate(date),
      messages: groups[date]
    }));
  }

  private formatDate(dateString: string): string {
    const msgDate = new Date(dateString);
    const today = new Date();
    const yesterday = new Date(today);
    yesterday.setDate(yesterday.getDate() - 1);

    if (msgDate.toDateString() === today.toDateString()) return 'Today';
    if (msgDate.toDateString() === yesterday.toDateString()) return 'Yesterday';

    return msgDate.toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric',
      year: msgDate.getFullYear() !== today.getFullYear() ? 'numeric' : undefined
    });
  }
}
