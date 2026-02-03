import { useState, useEffect } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import {
    LayoutDashboard,
    ArrowLeftRight,
    Target,
    Wallet2,
    BarChart3,
    Settings,
    Wallet,
    Shield
} from 'lucide-react';
import { clearAuthData, adminAPI } from '../../api/api';
import './Sidebar.css';

const navItems = [
    { to: '/dashboard', icon: LayoutDashboard, label: 'Dashboard' },
    { to: '/transactions', icon: ArrowLeftRight, label: 'Transactions' },
    { to: '/goals', icon: Target, label: 'Goals' },
    { to: '/budget', icon: Wallet2, label: 'Budget' },
    { to: '/reports', icon: BarChart3, label: 'Reports' },
    { to: '/settings', icon: Settings, label: 'Settings' },
];

export default function Sidebar() {
    const navigate = useNavigate();
    const username = localStorage.getItem('username') || 'User';
    const [isAdmin, setIsAdmin] = useState(false);

    useEffect(() => {
        // Check if user is admin
        adminAPI.getRole()
            .then(data => {
                setIsAdmin(data?.isAdmin === true);
            })
            .catch(() => {
                setIsAdmin(false);
            });
    }, []);

    const handleLogout = () => {
        clearAuthData();
        navigate('/login');
    };

    return (
        <aside className="sidebar">
            <div className="sidebar-header">
                <div className="sidebar-logo">
                    <div className="sidebar-logo-icon">
                        <Wallet size={20} />
                    </div>
                    <div className="sidebar-logo-text">
                        <span className="sidebar-brand">Spendly</span>
                        <span className="sidebar-tagline">Finance Manager</span>
                    </div>
                </div>
            </div>

            <nav className="sidebar-nav">
                {navItems.map((item) => (
                    <NavLink
                        key={item.to}
                        to={item.to}
                        className={({ isActive }) =>
                            `sidebar-nav-item ${isActive ? 'active' : ''}`
                        }
                    >
                        <item.icon size={20} />
                        <span>{item.label}</span>
                    </NavLink>
                ))}

                {/* Admin link - only shown for admin users */}
                {isAdmin && (
                    <NavLink
                        to="/admin"
                        className={({ isActive }) =>
                            `sidebar-nav-item admin-link ${isActive ? 'active' : ''}`
                        }
                    >
                        <Shield size={20} />
                        <span>Admin</span>
                    </NavLink>
                )}
            </nav>

            <div className="sidebar-footer">
                <div className="sidebar-user">
                    <div className="sidebar-user-avatar">
                        {username.charAt(0).toUpperCase()}
                    </div>
                    <div className="sidebar-user-info">
                        <span className="sidebar-user-name">{username}</span>
                        {isAdmin && <span className="admin-badge">Admin</span>}
                        <button onClick={handleLogout} className="sidebar-logout">
                            Logout
                        </button>
                    </div>
                </div>
            </div>
        </aside>
    );
}

