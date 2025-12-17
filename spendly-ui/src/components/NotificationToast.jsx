import { Bell, Target, AlertTriangle, FileText, X } from 'lucide-react';
import './NotificationToast.css';

const NOTIFICATION_ICONS = {
    GOAL_ACHIEVED: Target,
    BUDGET_EXCEEDED: AlertTriangle,
    REPORT_READY: FileText,
    GENERAL: Bell,
};

export default function NotificationToast({ notification, onClose }) {
    if (!notification) return null;

    const Icon = NOTIFICATION_ICONS[notification.type] || Bell;

    return (
        <div className="notification-toast">
            <div className="toast-icon">
                <Icon size={24} />
            </div>
            <div className="toast-content">
                <h4 className="toast-title">{notification.title}</h4>
                <p className="toast-message">{notification.message}</p>
            </div>
            <button className="toast-close" onClick={onClose}>
                <X size={18} />
            </button>
        </div>
    );
}
