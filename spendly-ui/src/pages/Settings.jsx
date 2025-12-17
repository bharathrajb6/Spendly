import { useState, useEffect } from 'react';
import { User, Bell } from 'lucide-react';
import Header from '../components/layout/Header';
import { userAPI } from '../api/api';
import './Settings.css';

export default function Settings() {
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [profile, setProfile] = useState({
        firstName: '',
        lastName: '',
        email: '',
        password: '••••••••',
    });
    const [notifications, setNotifications] = useState({
        budgetAlerts: true,
        goalUpdates: true,
        weeklyReports: false,
    });
    const [message, setMessage] = useState('');

    useEffect(() => {
        loadUserData();
    }, []);

    const loadUserData = async () => {
        try {
            const userData = await userAPI.getDetails();
            setProfile({
                firstName: userData.firstName || '',
                lastName: userData.lastName || '',
                email: userData.email || '',
                username: userData.username || localStorage.getItem('username'),
                password: '••••••••',
            });
        } catch (error) {
            console.error('Failed to load user data:', error);
            // Use sample data if API fails
            const username = localStorage.getItem('username') || 'User';
            setProfile({
                firstName: username,
                lastName: '',
                email: `${username}@example.com`,
                username: username,
                password: '••••••••',
            });
        } finally {
            setLoading(false);
        }
    };

    const handleSaveProfile = async () => {
        setSaving(true);
        setMessage('');
        try {
            await userAPI.updateDetails({
                username: profile.username,
                firstName: profile.firstName,
                lastName: profile.lastName,
                email: profile.email,
            });
            setMessage('Profile updated successfully!');
        } catch (error) {
            console.error('Failed to update profile:', error);
            setMessage('Failed to update profile. Please try again.');
        } finally {
            setSaving(false);
        }
    };

    const handleToggle = (key) => {
        setNotifications({ ...notifications, [key]: !notifications[key] });
    };

    if (loading) {
        return (
            <>
                <Header title="Settings" />
                <div className="page-content">
                    <div className="loading-container">
                        <div className="spinner"></div>
                    </div>
                </div>
            </>
        );
    }

    return (
        <>
            <Header title="Settings" />
            <div className="page-content">
                <div className="page-header">
                    <h1>Settings</h1>
                    <p className="text-secondary">Manage your account and preferences</p>
                </div>

                {/* Profile Information */}
                <div className="settings-section">
                    <div className="settings-section-header">
                        <User size={20} />
                        <span className="settings-section-title">Profile Information</span>
                    </div>

                    {message && (
                        <div className={`settings-message ${message.includes('Failed') ? 'error' : 'success'}`}>
                            {message}
                        </div>
                    )}

                    <div className="settings-form">
                        <div className="input-group">
                            <label>Name</label>
                            <input
                                type="text"
                                className="input"
                                value={`${profile.firstName} ${profile.lastName}`.trim() || profile.username}
                                onChange={(e) => {
                                    const parts = e.target.value.split(' ');
                                    setProfile({
                                        ...profile,
                                        firstName: parts[0] || '',
                                        lastName: parts.slice(1).join(' ') || '',
                                    });
                                }}
                            />
                        </div>
                        <div className="input-group">
                            <label>Email</label>
                            <input
                                type="email"
                                className="input"
                                value={profile.email}
                                onChange={(e) => setProfile({ ...profile, email: e.target.value })}
                            />
                        </div>
                        <div className="input-group">
                            <label>Password</label>
                            <input
                                type="password"
                                className="input"
                                value={profile.password}
                                onChange={(e) => setProfile({ ...profile, password: e.target.value })}
                            />
                        </div>
                        <button
                            className="btn btn-primary"
                            onClick={handleSaveProfile}
                            disabled={saving}
                        >
                            {saving ? 'Saving...' : 'Save Changes'}
                        </button>
                    </div>
                </div>

                {/* Notifications */}
                <div className="settings-section">
                    <div className="settings-section-header">
                        <Bell size={20} />
                        <span className="settings-section-title">Notifications</span>
                    </div>

                    <div className="settings-row">
                        <div className="settings-row-info">
                            <div className="settings-row-label">Budget Limit Alerts</div>
                            <div className="settings-row-description">Get notified when you exceed your budget</div>
                        </div>
                        <label className="toggle">
                            <input
                                type="checkbox"
                                checked={notifications.budgetAlerts}
                                onChange={() => handleToggle('budgetAlerts')}
                            />
                            <span className="toggle-slider"></span>
                        </label>
                    </div>

                    <div className="settings-row">
                        <div className="settings-row-info">
                            <div className="settings-row-label">Goal Updates</div>
                            <div className="settings-row-description">Receive updates on your savings goals</div>
                        </div>
                        <label className="toggle">
                            <input
                                type="checkbox"
                                checked={notifications.goalUpdates}
                                onChange={() => handleToggle('goalUpdates')}
                            />
                            <span className="toggle-slider"></span>
                        </label>
                    </div>

                    <div className="settings-row">
                        <div className="settings-row-info">
                            <div className="settings-row-label">Weekly Reports</div>
                            <div className="settings-row-description">Get weekly spending summaries</div>
                        </div>
                        <label className="toggle">
                            <input
                                type="checkbox"
                                checked={notifications.weeklyReports}
                                onChange={() => handleToggle('weeklyReports')}
                            />
                            <span className="toggle-slider"></span>
                        </label>
                    </div>
                </div>
            </div>
        </>
    );
}
