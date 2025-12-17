import { useState, useEffect, useCallback, useRef } from 'react';

/**
 * Custom hook to manage WebSocket connection for real-time notifications
 * @param {string} wsUrl - WebSocket server URL
 * @returns {Object} - notifications, connected status, and helper functions
 */
export default function useWebSocket(wsUrl = 'ws://localhost:8086/ws/notifications') {
    const [notifications, setNotifications] = useState([]);
    const [connected, setConnected] = useState(false);
    const [unreadCount, setUnreadCount] = useState(0);
    const [toast, setToast] = useState(null);
    const wsRef = useRef(null);
    const reconnectTimeoutRef = useRef(null);

    const showToast = useCallback((notification) => {
        setToast(notification);
        // Auto-hide toast after 5 seconds
        setTimeout(() => {
            setToast(null);
        }, 5000);
    }, []);

    const hideToast = useCallback(() => {
        setToast(null);
    }, []);

    const connect = useCallback(() => {
        const username = localStorage.getItem('username');
        if (!username) {
            console.log('No username found, skipping WebSocket connection');
            return;
        }

        try {
            const ws = new WebSocket(wsUrl);
            wsRef.current = ws;

            ws.onopen = () => {
                console.log('WebSocket connected');
                setConnected(true);
                // Register with username
                ws.send(JSON.stringify({ type: 'register', username }));
            };

            ws.onmessage = (event) => {
                try {
                    const data = JSON.parse(event.data);
                    console.log('Received message:', data);

                    // Skip connection confirmation messages
                    if (data.type === 'connected') {
                        console.log('Successfully registered for notifications');
                        return;
                    }

                    // Add notification with a unique id
                    const notification = {
                        ...data,
                        id: Date.now(),
                        read: false,
                        receivedAt: new Date().toISOString(),
                    };

                    setNotifications((prev) => [notification, ...prev]);
                    setUnreadCount((prev) => prev + 1);

                    // Show toast alert
                    showToast(notification);
                } catch (e) {
                    console.error('Error parsing WebSocket message:', e);
                }
            };

            ws.onclose = (event) => {
                console.log('WebSocket disconnected:', event.code, event.reason);
                setConnected(false);

                // Attempt to reconnect after 5 seconds
                if (!event.wasClean) {
                    reconnectTimeoutRef.current = setTimeout(() => {
                        console.log('Attempting to reconnect...');
                        connect();
                    }, 5000);
                }
            };

            ws.onerror = (error) => {
                console.error('WebSocket error:', error);
            };
        } catch (error) {
            console.error('Failed to create WebSocket connection:', error);
        }
    }, [wsUrl, showToast]);

    const disconnect = useCallback(() => {
        if (reconnectTimeoutRef.current) {
            clearTimeout(reconnectTimeoutRef.current);
        }
        if (wsRef.current) {
            wsRef.current.close();
            wsRef.current = null;
        }
        setConnected(false);
    }, []);

    const markAsRead = useCallback((notificationId) => {
        setNotifications((prev) =>
            prev.map((n) =>
                n.id === notificationId ? { ...n, read: true } : n
            )
        );
        setUnreadCount((prev) => Math.max(0, prev - 1));
    }, []);

    const markAllAsRead = useCallback(() => {
        setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
        setUnreadCount(0);
    }, []);

    const clearNotification = useCallback((notificationId) => {
        setNotifications((prev) => {
            const notification = prev.find((n) => n.id === notificationId);
            if (notification && !notification.read) {
                setUnreadCount((count) => Math.max(0, count - 1));
            }
            return prev.filter((n) => n.id !== notificationId);
        });
    }, []);

    const clearAllNotifications = useCallback(() => {
        setNotifications([]);
        setUnreadCount(0);
    }, []);

    // Connect on mount, disconnect on unmount
    useEffect(() => {
        connect();
        return () => disconnect();
    }, [connect, disconnect]);

    return {
        notifications,
        connected,
        unreadCount,
        toast,
        hideToast,
        markAsRead,
        markAllAsRead,
        clearNotification,
        clearAllNotifications,
        reconnect: connect,
    };
}
