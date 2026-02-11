import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { Client, IMessage, StompSubscription } from '@stomp/stompjs';
import { ChatMessage } from '../../features/shared/models/chat';
import { AuthService } from './auth.service';

interface SockJSClass {
  new (url: string): WebSocket;
}

declare global {
  interface Window {
    SockJS: SockJSClass;
  }
}

@Injectable({
  providedIn: 'root'
})
export class WebSocketService {
  private stompClient: Client | null = null;
  private messageSubject = new BehaviorSubject<ChatMessage | null>(null);
  private connected = false;
  private subscription: StompSubscription | undefined;

  public messages$: Observable<ChatMessage | null> = this.messageSubject.asObservable();

  constructor(private authService: AuthService) {}

  async connect(userId: number): Promise<void> {
    if (this.connected) {
      console.log('WebSocket already connected');
      return;
    }

    try {
      await this.ensureSockJSLoaded();

      const socket = new window.SockJS('http://localhost:8080/ws') as WebSocket;
      const jwtToken = this.authService.getToken();

      this.stompClient = new Client({
        webSocketFactory: () => socket,
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
        connectHeaders: {
          Authorization: `Bearer ${jwtToken}`
        },
        debug: (str: string) => {
          console.log('STOMP:', str);
        }
      });

      this.stompClient.onConnect = () => {
        console.log('WebSocket connected for user:', userId);
        this.connected = true;

        if (this.stompClient) {
          // Subscribe na /topic/messages/{userId}
          this.subscription = this.stompClient.subscribe(
            `/topic/messages/${userId}`,
            (message: IMessage) => {
              try {
                const chatMessage: ChatMessage = JSON.parse(message.body);
                this.messageSubject.next(chatMessage);
              } catch (error) {
                console.error('Error parsing message:', error);
              }
            }
          );
        }
      };

      this.stompClient.onStompError = (frame) => {
        console.error('STOMP error:', frame.headers['message']);
        this.connected = false;
      };

      this.stompClient.onWebSocketClose = () => {
        console.log('WebSocket connection closed');
        this.connected = false;
      };

      this.stompClient.activate();

    } catch (error) {
      console.error('Failed to connect WebSocket:', error);
      throw error;
    }
  }

  private ensureSockJSLoaded(): Promise<void> {
    return new Promise((resolve, reject) => {
      if (typeof window.SockJS !== 'undefined') {
        resolve();
        return;
      }

      const checkInterval = setInterval(() => {
        if (typeof window.SockJS !== 'undefined') {
          clearInterval(checkInterval);
          resolve();
        }
      }, 100);

      setTimeout(() => {
        clearInterval(checkInterval);
        if (typeof window.SockJS === 'undefined') {
          reject(new Error('SockJS failed to load'));
        }
      }, 5000);
    });
  }

  disconnect(): void {
    if (this.subscription) {
      this.subscription.unsubscribe();
      this.subscription = undefined;
    }

    if (this.stompClient) {
      this.stompClient.deactivate();
      this.stompClient = null;
    }

    this.connected = false;
    console.log('WebSocket disconnected');
  }

  isConnected(): boolean {
    return this.connected;
  }
}
