import { Component, EventEmitter, OnInit, Output, ChangeDetectorRef, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ChatService } from '../../../../core/services/chat.service';
import { ConversationPreview } from '../../../shared/models/chat';
import { WebSocketService } from '../../../../core/services/websocket.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-conversation-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './conversation-list.html',
})
export class ConversationList implements OnInit, OnDestroy {
  @Output() onSelected = new EventEmitter<number>();

  conversations: ConversationPreview[] = [];
  selectedUserId: number | null = null;
  private messageSubscription?: Subscription;

  constructor(
    private chatService: ChatService,
    private websocketService: WebSocketService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadConversations();
    this.subscribeToMessages();
  }

  ngOnDestroy(): void {
    this.messageSubscription?.unsubscribe();
  }

  loadConversations(): void {
    this.chatService.getConversations().subscribe({
      next: (conversations) => {
        this.conversations = conversations;
        this.cdr.detectChanges();
      }
    });
  }

  private subscribeToMessages(): void {
    this.messageSubscription = this.websocketService.messages$.subscribe({
      next: (message) => {
        if (message) {
          this.loadConversations();
        }
      }
    });
  }

  selectConversation(userId: number): void {
    this.selectedUserId = userId;
    this.onSelected.emit(userId);

    // Refresh conversation list nakon sto se poruke markuju kao procitane
    setTimeout(() => {
      this.loadConversations();
    }, 500);
  }

  getRoleBadgeClass(role: string): string {
    if (role === 'DRIVER') return 'bg-blue-100 text-blue-800';
    if (role === 'PASSENGER') return 'bg-green-100 text-green-800';
    return 'bg-gray-100 text-gray-800';
  }

  formatTime(timestamp: string): string {
    const date = new Date(timestamp);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMs / 3600000);
    const diffDays = Math.floor(diffMs / 86400000);

    if (diffMins < 1) return 'Just now';
    if (diffMins < 60) return `${diffMins}m ago`;
    if (diffHours < 24) return `${diffHours}h ago`;
    if (diffDays < 7) return `${diffDays}d ago`;

    return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
  }

  truncateMessage(message: string, maxLength: number = 40): string {
    return message.length > maxLength
      ? message.substring(0, maxLength) + '...'
      : message;
  }
}
