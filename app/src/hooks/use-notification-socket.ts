import { useEffect, useRef } from 'react';
import { useNotificationStore, useUserStore } from '@/stores';
import type { ExperienceNotificationVO } from '@/api/user/notificationApi';

const RECONNECT_MAX_DELAY_MS = 30_000;

function normalizeWsBase(base: string): string {
  return base.replace(/^http/, 'ws').replace(/\/+$/, '');
}

function resolveWsBase(): string {
  const wsEnv = import.meta.env.VITE_WS_BASE_URL;
  if (wsEnv) {
    return normalizeWsBase(wsEnv);
  }

  const apiEnv = import.meta.env.VITE_API_BASE_URL;
  if (apiEnv && /^https?:\/\//.test(apiEnv)) {
    return normalizeWsBase(apiEnv).replace(/\/api\/?$/, '');
  }

  const host = typeof window !== 'undefined' ? window.location.hostname : 'localhost';
  const protocol =
    typeof window !== 'undefined' && window.location.protocol === 'https:' ? 'wss' : 'ws';
  if (import.meta.env.DEV) {
    return `${protocol}://${host}:8080`;
  }
  const port = typeof window !== 'undefined' ? window.location.port : '';
  return `${protocol}://${host}${port ? `:${port}` : ''}`;
}

export function useNotificationSocket() {
  const token = useUserStore((state) => state.token);
  const userId = useUserStore((state) => state.user?.id);
  const incrementUnreadCount = useNotificationStore((state) => state.incrementUnreadCount);
  const fetchUnreadCount = useNotificationStore((state) => state.fetchUnreadCount);

  const socketRef = useRef<WebSocket | null>(null);
  const reconnectTimerRef = useRef<number | null>(null);
  const reconnectAttemptsRef = useRef(0);
  const manualCloseRef = useRef(false);

  useEffect(() => {
    if (!token || !userId) {
      if (socketRef.current) {
        manualCloseRef.current = true;
        socketRef.current.close();
        socketRef.current = null;
      }
      if (reconnectTimerRef.current) {
        window.clearTimeout(reconnectTimerRef.current);
        reconnectTimerRef.current = null;
      }
      return;
    }

    manualCloseRef.current = false;
    let cancelled = false;

    const connect = () => {
      if (cancelled) {
        return;
      }
      const wsBase = resolveWsBase();
      const wsUrl = `${wsBase}/ws/${userId}?token=${encodeURIComponent(token)}`;
      const socket = new WebSocket(wsUrl);
      socketRef.current = socket;

      socket.onopen = () => {
        reconnectAttemptsRef.current = 0;
        fetchUnreadCount();
      };

      socket.onmessage = (event) => {
        if (!event?.data) {
          return;
        }
        try {
          const notification = JSON.parse(event.data) as ExperienceNotificationVO;
          if (!notification) {
            return;
          }
          const normalizedNotification: ExperienceNotificationVO = {
            ...notification,
            clientId:
              notification.id == null
                ? notification.clientId ??
                  `client-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`
                : notification.clientId,
          };
          incrementUnreadCount(1);
          window.dispatchEvent(
            new CustomEvent('notification:received', { detail: normalizedNotification })
          );
        } catch (error) {
          console.error('Failed to parse notification message:', error);
        }
      };

      socket.onerror = (event) => {
        if (manualCloseRef.current || cancelled) {
          return;
        }
        console.warn('WebSocket error', event);
        socket.close();
      };

      socket.onclose = (event) => {
        if (manualCloseRef.current || cancelled) {
          return;
        }
        console.warn('WebSocket closed', { code: event.code, reason: event.reason });
        const attempt = reconnectAttemptsRef.current + 1;
        reconnectAttemptsRef.current = attempt;
        const delay = Math.min(1000 * 2 ** attempt, RECONNECT_MAX_DELAY_MS);
        scheduleConnect(delay);
      };
    };

    const scheduleConnect = (delayMs = 0) => {
      if (reconnectTimerRef.current) {
        window.clearTimeout(reconnectTimerRef.current);
      }
      reconnectTimerRef.current = window.setTimeout(() => {
        if (cancelled) {
          return;
        }
        connect();
      }, delayMs);
    };

    scheduleConnect(0);

    return () => {
      cancelled = true;
      manualCloseRef.current = true;
      if (socketRef.current) {
        socketRef.current.close();
        socketRef.current = null;
      }
      if (reconnectTimerRef.current) {
        window.clearTimeout(reconnectTimerRef.current);
        reconnectTimerRef.current = null;
      }
    };
  }, [token, userId, incrementUnreadCount, fetchUnreadCount]);
}