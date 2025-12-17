import { useEffect, useRef } from 'react';
import { X, Bell, Target, AlertTriangle, FileText, CheckCircle } from 'lucide-react';
import './NotificationDropdown.css';

const NOTIFICATION_ICONS = {
    GOAL_ACHIEVED: Target,
    BUDGET_EXCEEDED: AlertTriangle,
    REPORT_READY: FileText,
    GENERAL: Bell,
};

const NOTIFICATION_COLORS = {
    GOAL_ACHIEVED: 'var(--success-color)',
    BUDGET_EXCEEDED: 'var(--warning-color)',
    REPORT_READY: 'var(--primary-color)',
    GENERAL: 'var(--text-secondary)',
};

function formatTime(timestamp) {
    if (!timestamp) return '';
    const date = new Date(timestamp);
    const now = new Date();
    const diff = now - date;

    if (diff < 60000) return 'Just now';
    if (diff < 3600000) return `${Math.floor(diff / 60000)}m ago`;
    if (diff < 86400000) return `${Math.floor(diff / 3600000)}h ago`;
    return date.toLocaleDateString();
}

export default function NotificationDropdown({
    notifications,
    unreadCount,
    onMarkAsRead,
    onMarkAllAsRead,
    onClear,
    onClearAll,
    onClose,
    connected,
}) {
    const dropdownRef = useRef(null);

    // Close dropdown when clicking outside
    useEffect(() => {
        function handleClickOutside(event) {
            if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
                // Check if click is on the notification button itself
                if (!event.target.closest('.header-notification')) {
                    onClose();
                }
            }
        }

        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, [onClose]);

    return (
        <div className="notification-dropdown" ref={dropdownRef}>
            <div className="notification-header">
                <div className="notification-header-left">
                    <h3>Notifications</h3>
                    {unreadCount > 0 && (
                        <span className="notification-badge">{unreadCount}</span>
                    )}
                </div>
                <div className="notification-header-right">
                    <span className={`connection-status ${connected ? 'connected' : 'disconnected'}`}>
                        {connected ? '● Live' : '○ Offline'}
                    </span>
                    {notifications.length > 0 && (
                        <button className="mark-all-read" onClick={onMarkAllAsRead}>
                            <CheckCircle size={14} />
                            Mark all read
                        </button>
                    )}
                </div>
            </div>

            <div className="notification-list">
                {notifications.length === 0 ? (
                    <div className="notification-empty">
                        <Bell size={48} strokeWidth={1} />
                        <p>No notifications yet</p>
                        <span>We'll notify you when something important happens</span>
                    </div>
                ) : (
                    notifications.map((notification) => {
                        const Icon = NOTIFICATION_ICONS[notification.type] || Bell;
                        const color = NOTIFICATION_COLORS[notification.type] || 'var(--text-secondary)';

                        return (
                            <div
                                key={notification.id}
                                className={`notification-item ${notification.read ? 'read' : 'unread'}`}
                                onClick={() => onMarkAsRead(notification.id)}
                            >
                                <div
                                    className="notification-icon"
                                    style={{ backgroundColor: `${color}20`, color }}
                                >
                                    <Icon size={20} />
                                </div>
                                <div className="notification-content">
                                    <h4 className="notification-title">{notification.title}</h4>
                                    <p className="notification-message">{notification.message}</p>
                                    <span className="notification-time">
                                        {formatTime(notification.receivedAt || notification.timestamp)}
                                    </span>
                                </div>
                                <button
                                    className="notification-close"
                                    onClick={(e) => {
                                        e.stopPropagation();
                                        onClear(notification.id);
                                    }}
                                >
                                    <X size={16} />
                                </button>
                            </div>
                        );
                    })
                )}
            </div>

            {notifications.length > 0 && (
                <div className="notification-footer">
                    <button className="clear-all" onClick={onClearAll}>
                        Clear all notifications
                    </button>
                </div>
            )}
        </div>
    );
}
