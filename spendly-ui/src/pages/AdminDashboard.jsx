import { useState, useEffect } from 'react';
import {
    Users,
    UserCheck,
    UserX,
    Shield,
    ShieldCheck,
    TrendingUp,
    Activity,
    Search,
    RefreshCw,
    ChevronDown,
    ChevronUp,
    AlertCircle
} from 'lucide-react';
import Header from '../components/layout/Header';
import { adminAPI } from '../api/api';
import './AdminDashboard.css';

export default function AdminDashboard() {
    const [loading, setLoading] = useState(true);
    const [stats, setStats] = useState(null);
    const [users, setUsers] = useState([]);
    const [error, setError] = useState(null);
    const [searchTerm, setSearchTerm] = useState('');
    const [filterRole, setFilterRole] = useState('all');
    const [filterStatus, setFilterStatus] = useState('all');
    const [sortField, setSortField] = useState('createdAt');
    const [sortDirection, setSortDirection] = useState('desc');
    const [actionLoading, setActionLoading] = useState(null);

    useEffect(() => {
        loadAdminData();
    }, []);

    const loadAdminData = async () => {
        setLoading(true);
        setError(null);
        try {
            const [statsData, usersData] = await Promise.all([
                adminAPI.getStats(),
                adminAPI.getAllUsers(),
            ]);
            setStats(statsData);
            setUsers(usersData || []);
        } catch (err) {
            console.error('Failed to load admin data:', err);
            setError(err.message || 'Failed to load admin data. You may not have admin privileges.');
        } finally {
            setLoading(false);
        }
    };

    const handleToggleActive = async (username) => {
        setActionLoading(username);
        try {
            await adminAPI.toggleUserActive(username);
            await loadAdminData();
        } catch (err) {
            console.error('Failed to toggle user status:', err);
            alert('Failed to update user status');
        } finally {
            setActionLoading(null);
        }
    };

    const handleMakeAdmin = async (username) => {
        if (!confirm(`Are you sure you want to make ${username} an admin?`)) return;

        setActionLoading(username);
        try {
            await adminAPI.makeAdmin(username);
            await loadAdminData();
        } catch (err) {
            console.error('Failed to make user admin:', err);
            alert('Failed to update user role');
        } finally {
            setActionLoading(null);
        }
    };

    const handleRemoveAdmin = async (username) => {
        if (!confirm(`Are you sure you want to remove admin privileges from ${username}?`)) return;

        setActionLoading(username);
        try {
            await adminAPI.removeAdmin(username);
            await loadAdminData();
        } catch (err) {
            console.error('Failed to remove admin:', err);
            alert(err.message || 'Failed to update user role');
        } finally {
            setActionLoading(null);
        }
    };

    const handleSort = (field) => {
        if (sortField === field) {
            setSortDirection(prev => prev === 'asc' ? 'desc' : 'asc');
        } else {
            setSortField(field);
            setSortDirection('asc');
        }
    };

    // Filter and sort users
    const filteredUsers = users
        .filter(user => {
            const matchesSearch =
                user.username?.toLowerCase().includes(searchTerm.toLowerCase()) ||
                user.firstName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
                user.lastName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
                user.email?.toLowerCase().includes(searchTerm.toLowerCase());

            const matchesRole = filterRole === 'all' ||
                (filterRole === 'admin' && user.role === 'ADMIN') ||
                (filterRole === 'user' && user.role !== 'ADMIN');

            const matchesStatus = filterStatus === 'all' ||
                (filterStatus === 'active' && user.active) ||
                (filterStatus === 'inactive' && !user.active);

            return matchesSearch && matchesRole && matchesStatus;
        })
        .sort((a, b) => {
            let aVal = a[sortField];
            let bVal = b[sortField];

            if (sortField === 'createdAt') {
                aVal = new Date(aVal || 0).getTime();
                bVal = new Date(bVal || 0).getTime();
            }

            if (typeof aVal === 'string') aVal = aVal.toLowerCase();
            if (typeof bVal === 'string') bVal = bVal.toLowerCase();

            if (aVal < bVal) return sortDirection === 'asc' ? -1 : 1;
            if (aVal > bVal) return sortDirection === 'asc' ? 1 : -1;
            return 0;
        });

    const formatDate = (dateStr) => {
        if (!dateStr) return 'N/A';
        return new Date(dateStr).toLocaleDateString('en-IN', {
            day: 'numeric',
            month: 'short',
            year: 'numeric'
        });
    };

    if (loading) {
        return (
            <>
                <Header title="Admin Dashboard" />
                <div className="page-content">
                    <div className="loading-container">
                        <div className="spinner"></div>
                    </div>
                </div>
            </>
        );
    }

    if (error) {
        return (
            <>
                <Header title="Admin Dashboard" />
                <div className="page-content">
                    <div className="admin-error">
                        <AlertCircle size={48} />
                        <h2>Access Denied</h2>
                        <p>{error}</p>
                        <button className="btn btn-primary" onClick={() => window.history.back()}>
                            Go Back
                        </button>
                    </div>
                </div>
            </>
        );
    }

    return (
        <>
            <Header title="Admin Dashboard" />
            <div className="page-content">
                {/* Page Header */}
                <div className="admin-header">
                    <div>
                        <h1>Admin Dashboard</h1>
                        <p className="text-secondary">Manage users and monitor platform activity</p>
                    </div>
                    <button className="btn btn-secondary" onClick={loadAdminData}>
                        <RefreshCw size={18} />
                        Refresh
                    </button>
                </div>

                {/* Statistics Cards */}
                <div className="admin-stats-grid">
                    <div className="admin-stat-card">
                        <div className="admin-stat-icon blue">
                            <Users size={24} />
                        </div>
                        <div className="admin-stat-content">
                            <div className="admin-stat-value">{stats?.totalUsers || 0}</div>
                            <div className="admin-stat-label">Total Users</div>
                        </div>
                    </div>

                    <div className="admin-stat-card">
                        <div className="admin-stat-icon green">
                            <UserCheck size={24} />
                        </div>
                        <div className="admin-stat-content">
                            <div className="admin-stat-value">{stats?.activeUsers || 0}</div>
                            <div className="admin-stat-label">Active Users</div>
                        </div>
                    </div>

                    <div className="admin-stat-card">
                        <div className="admin-stat-icon red">
                            <UserX size={24} />
                        </div>
                        <div className="admin-stat-content">
                            <div className="admin-stat-value">{stats?.inactiveUsers || 0}</div>
                            <div className="admin-stat-label">Inactive Users</div>
                        </div>
                    </div>

                    <div className="admin-stat-card">
                        <div className="admin-stat-icon purple">
                            <ShieldCheck size={24} />
                        </div>
                        <div className="admin-stat-content">
                            <div className="admin-stat-value">{stats?.adminUsers || 0}</div>
                            <div className="admin-stat-label">Admins</div>
                        </div>
                    </div>
                </div>

                {/* Growth Stats */}
                <div className="admin-growth-cards">
                    <div className="admin-growth-card">
                        <div className="admin-growth-icon">
                            <TrendingUp size={20} />
                        </div>
                        <div className="admin-growth-content">
                            <div className="admin-growth-value">{stats?.newUsersThisWeek || 0}</div>
                            <div className="admin-growth-label">New users this week</div>
                        </div>
                    </div>
                    <div className="admin-growth-card">
                        <div className="admin-growth-icon">
                            <Activity size={20} />
                        </div>
                        <div className="admin-growth-content">
                            <div className="admin-growth-value">{stats?.newUsersThisMonth || 0}</div>
                            <div className="admin-growth-label">New users this month</div>
                        </div>
                    </div>
                </div>

                {/* User Management Section */}
                <div className="admin-users-section">
                    <div className="admin-users-header">
                        <h2>User Management</h2>
                        <div className="admin-users-filters">
                            <div className="input-with-icon">
                                <Search size={18} className="icon" />
                                <input
                                    type="text"
                                    className="input"
                                    placeholder="Search users..."
                                    value={searchTerm}
                                    onChange={(e) => setSearchTerm(e.target.value)}
                                />
                            </div>
                            <select
                                className="select"
                                value={filterRole}
                                onChange={(e) => setFilterRole(e.target.value)}
                            >
                                <option value="all">All Roles</option>
                                <option value="admin">Admins Only</option>
                                <option value="user">Users Only</option>
                            </select>
                            <select
                                className="select"
                                value={filterStatus}
                                onChange={(e) => setFilterStatus(e.target.value)}
                            >
                                <option value="all">All Status</option>
                                <option value="active">Active Only</option>
                                <option value="inactive">Inactive Only</option>
                            </select>
                        </div>
                    </div>

                    {/* Users Table */}
                    <div className="admin-users-table-wrapper">
                        <table className="admin-users-table">
                            <thead>
                                <tr>
                                    <th onClick={() => handleSort('username')} className="sortable">
                                        Username
                                        {sortField === 'username' && (sortDirection === 'asc' ? <ChevronUp size={14} /> : <ChevronDown size={14} />)}
                                    </th>
                                    <th onClick={() => handleSort('firstName')} className="sortable">
                                        Name
                                        {sortField === 'firstName' && (sortDirection === 'asc' ? <ChevronUp size={14} /> : <ChevronDown size={14} />)}
                                    </th>
                                    <th onClick={() => handleSort('email')} className="sortable">
                                        Email
                                        {sortField === 'email' && (sortDirection === 'asc' ? <ChevronUp size={14} /> : <ChevronDown size={14} />)}
                                    </th>
                                    <th>Role</th>
                                    <th>Status</th>
                                    <th onClick={() => handleSort('createdAt')} className="sortable">
                                        Joined
                                        {sortField === 'createdAt' && (sortDirection === 'asc' ? <ChevronUp size={14} /> : <ChevronDown size={14} />)}
                                    </th>
                                    <th>Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                {filteredUsers.length === 0 ? (
                                    <tr>
                                        <td colSpan="7" className="empty-row">
                                            No users found matching your criteria
                                        </td>
                                    </tr>
                                ) : (
                                    filteredUsers.map((user) => (
                                        <tr key={user.username} className={!user.active ? 'inactive-row' : ''}>
                                            <td className="username-cell">
                                                <span className="user-avatar">
                                                    {user.firstName?.[0]?.toUpperCase() || user.username?.[0]?.toUpperCase() || '?'}
                                                </span>
                                                {user.username}
                                            </td>
                                            <td>{user.firstName} {user.lastName}</td>
                                            <td>{user.email}</td>
                                            <td>
                                                <span className={`role-badge ${user.role === 'ADMIN' ? 'admin' : 'user'}`}>
                                                    {user.role === 'ADMIN' ? <Shield size={12} /> : null}
                                                    {user.role || 'USER'}
                                                </span>
                                            </td>
                                            <td>
                                                <span className={`status-badge ${user.active ? 'active' : 'inactive'}`}>
                                                    {user.active ? 'Active' : 'Inactive'}
                                                </span>
                                            </td>
                                            <td>{formatDate(user.createdAt)}</td>
                                            <td className="actions-cell">
                                                <button
                                                    className={`btn-action ${user.active ? 'deactivate' : 'activate'}`}
                                                    onClick={() => handleToggleActive(user.username)}
                                                    disabled={actionLoading === user.username}
                                                    title={user.active ? 'Deactivate user' : 'Activate user'}
                                                >
                                                    {user.active ? <UserX size={16} /> : <UserCheck size={16} />}
                                                </button>
                                                {user.role === 'ADMIN' ? (
                                                    <button
                                                        className="btn-action demote"
                                                        onClick={() => handleRemoveAdmin(user.username)}
                                                        disabled={actionLoading === user.username}
                                                        title="Remove admin privileges"
                                                    >
                                                        <Shield size={16} />
                                                    </button>
                                                ) : (
                                                    <button
                                                        className="btn-action promote"
                                                        onClick={() => handleMakeAdmin(user.username)}
                                                        disabled={actionLoading === user.username}
                                                        title="Make admin"
                                                    >
                                                        <ShieldCheck size={16} />
                                                    </button>
                                                )}
                                            </td>
                                        </tr>
                                    ))
                                )}
                            </tbody>
                        </table>
                    </div>

                    {/* Users Count */}
                    <div className="admin-users-footer">
                        Showing {filteredUsers.length} of {users.length} users
                    </div>
                </div>
            </div>
        </>
    );
}
