import { useState } from 'react';
import { Bell } from 'lucide-react';
import useWebSocket from '../../hooks/useWebSocket';
import NotificationDropdown from '../NotificationDropdown';
import NotificationToast from '../NotificationToast';
import './Header.css';

export default function Header({ title }) {
    const username = localStorage.getItem('username') || 'User';
    const [showNotifications, setShowNotifications] = useState(false);

    const {
        notifications,
        connected,
        unreadCount,
        toast,
        hideToast,
        markAsRead,
        markAllAsRead,
        clearNotification,
        clearAllNotifications,
    } = useWebSocket();

    return (
        <>
            <header className="header">
                <h1 className="header-title">{title}</h1>

                <div className="header-actions">
                    <div className="notification-wrapper">
                        <button
                            className="header-notification"
                            onClick={() => setShowNotifications(!showNotifications)}
                        >
                            <Bell size={20} />
                            {unreadCount > 0 && (
                                <span className="notification-dot">{unreadCount > 9 ? '9+' : unreadCount}</span>
                            )}
                        </button>

                        {showNotifications && (
                            <NotificationDropdown
                                notifications={notifications}
                                unreadCount={unreadCount}
                                connected={connected}
                                onMarkAsRead={markAsRead}
                                onMarkAllAsRead={markAllAsRead}
                                onClear={clearNotification}
                                onClearAll={clearAllNotifications}
                                onClose={() => setShowNotifications(false)}
                            />
                        )}
                    </div>

                    <div className="header-user">
                        <div className="header-user-avatar">
                            {username.charAt(0).toUpperCase()}
                        </div>
                        <span className="header-user-name">{username}</span>
                    </div>
                </div>
            </header>

            {/* Toast notification for new alerts */}
            <NotificationToast notification={toast} onClose={hideToast} />
        </>
    );
}
