export interface ChatMessage {
  id: number;
  senderId: number;
  senderName: string;
  senderRole: 'ADMIN' | 'DRIVER' | 'PASSENGER';
  recipientId: number;
  message: string;
  read: boolean;
  createdAt: string;
}

export interface ChatHistory {
  otherUserId: number;
  otherUserName: string;
  messages: ChatMessage[];
}

export interface SendMessageRequest {
  recipientId: number;
  message: string;
}

export interface ConversationPreview {
  userId: number;
  userName: string;
  userEmail: string;
  userRole: 'ADMIN' | 'DRIVER' | 'PASSENGER';
  lastMessage: string;
  lastMessageTime: string;
  unreadCount: number;
}
