import { useState, useEffect } from 'react';
import { Plus, Target, Trash2, Wallet, Eye } from 'lucide-react';
import Header from '../components/layout/Header';
import { goalAPI, transactionAPI } from '../api/api';
import './Goals.css';

const goalIcons = ['🏦', '✈️', '💻', '📈', '🏠', '🚗', '💍', '🎓'];

export default function Goals() {
    const [loading, setLoading] = useState(true);
    const [goals, setGoals] = useState([]);
    const [summary, setSummary] = useState(null);
    const [currentSavings, setCurrentSavings] = useState(0);
    const [error, setError] = useState(null);
    const [showAddModal, setShowAddModal] = useState(false);
    const [showAddFundsModal, setShowAddFundsModal] = useState(null);
    const [selectedGoal, setSelectedGoal] = useState(null); // For viewing goal details
    const [deleteConfirm, setDeleteConfirm] = useState(null); // For delete confirmation

    useEffect(() => {
        loadGoals();
    }, []);

    const loadGoals = async () => {
        setLoading(true);
        setError(null);
        try {
            // Fetch goals and savings data
            const [goalsData, summaryData, savingsData] = await Promise.all([
                goalAPI.getAll().catch(() => []),
                goalAPI.getSummary().catch(() => null),
                transactionAPI.getSavings().catch(() => 0),
            ]);

            // Map API response to UI format
            const mappedGoals = (goalsData || []).map(goal => ({
                id: goal.goalId,
                name: goal.goalName,
                icon: '🎯', // Default icon since API doesn't store icons
                target: goal.targetAmount,
                current: goal.savedAmount,
                progress: Math.round(goal.progressPercent || 0),
                deadline: goal.deadline,
                status: goal.status,
                daysLeft: goal.deadline ? Math.ceil((new Date(goal.deadline) - new Date()) / (1000 * 60 * 60 * 24)) : 0,
            }));

            setGoals(mappedGoals);
            setSummary(summaryData);
            console.log('Goals API response:', goalsData);
            console.log('Mapped goals:', mappedGoals);
            console.log('Savings data from API:', savingsData, typeof savingsData);
            setCurrentSavings(Number(savingsData) || 0);
        } catch (err) {
            console.error('Failed to load goals:', err);
            setError('Failed to load goals. Please make sure the backend is running.');
            setGoals([]);
        } finally {
            setLoading(false);
        }
    };

    const handleDeleteGoal = async (goalId) => {
        // Close modal immediately
        setDeleteConfirm(null);

        try {
            await goalAPI.delete(goalId);
            // Update state immediately to remove the deleted goal
            setGoals(prevGoals => prevGoals.filter(g => g.id !== goalId));
        } catch (error) {
            console.error('Failed to delete goal:', error);
            alert('Failed to delete goal. Please try again.');
        }
    };

    const getDaysLeftText = (days) => {
        if (days < 0) return `${Math.abs(days)} days overdue`;
        return `${days} days left`;
    };

    if (loading) {
        return (
            <>
                <Header title="Goals" />
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
            <Header title="Goals" />
            <div className="page-content">
                <div className="goals-header">
                    <div>
                        <h1>Savings Goals</h1>
                        <p className="text-secondary">Track your financial goals and progress</p>
                    </div>
                    <button className="btn btn-primary" onClick={() => setShowAddModal(true)}>
                        <Plus size={18} />
                        Add New Goal
                    </button>
                </div>

                {/* Current Savings Card */}
                <div className="current-savings-card">
                    <div className="current-savings-icon">
                        <Wallet size={28} />
                    </div>
                    <div className="current-savings-content">
                        <div className="current-savings-label">Current Savings</div>
                        <div className="current-savings-value">
                            ₹{(currentSavings || 0).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                        </div>
                        <div className="current-savings-subtitle">Available for your goals</div>
                    </div>
                </div>

                {/* Error State */}
                {error && (
                    <div className="card" style={{ marginBottom: 'var(--spacing-lg)' }}>
                        <div className="empty-state">
                            <div className="empty-state-icon">⚠️</div>
                            <div className="empty-state-title">Error loading goals</div>
                            <p>{error}</p>
                            <button className="btn btn-primary" style={{ marginTop: 'var(--spacing-md)' }} onClick={loadGoals}>
                                Try Again
                            </button>
                        </div>
                    </div>
                )}

                {/* Goals Grid */}
                {!error && goals.length === 0 ? (
                    <div className="card" style={{ marginBottom: 'var(--spacing-lg)' }}>
                        <div className="empty-state">
                            <div className="empty-state-icon">🎯</div>
                            <div className="empty-state-title">No savings goals yet</div>
                            <p>Create your first goal to start tracking your progress.</p>
                            <button className="btn btn-primary" style={{ marginTop: 'var(--spacing-md)' }} onClick={() => setShowAddModal(true)}>
                                <Plus size={18} /> Add Your First Goal
                            </button>
                        </div>
                    </div>
                ) : (
                    <div className="grid-2">
                        {goals.map((goal) => (
                            <div key={goal.id} className="goal-card">
                                <div className="goal-header">
                                    <div className="goal-icon">{goal.icon}</div>
                                    <div className="goal-info">
                                        <div className="goal-name">{goal.name}</div>
                                        <div className="goal-deadline">{getDaysLeftText(goal.daysLeft)}</div>
                                    </div>
                                </div>

                                <div className="goal-progress">
                                    <div className="goal-progress-header">
                                        <span className="goal-progress-label">Progress</span>
                                        <span className="goal-progress-percentage">{goal.progress}%</span>
                                    </div>
                                    <div className="progress-bar">
                                        <div
                                            className="progress-bar-fill"
                                            style={{ width: `${goal.progress}%` }}
                                        ></div>
                                    </div>
                                    <div className="goal-amounts">
                                        <span className="goal-current">₹{(goal.current || 0).toLocaleString()}</span>
                                        <span>of ₹{(goal.target || 0).toLocaleString()}</span>
                                    </div>
                                </div>

                                <div className="goal-actions">
                                    <button
                                        className="btn btn-secondary"
                                        onClick={() => setSelectedGoal(goal)}
                                    >
                                        <Eye size={16} />
                                        View Details
                                    </button>
                                    <button
                                        className="btn btn-danger"
                                        onClick={() => setDeleteConfirm(goal)}
                                        title="Delete Goal"
                                    >
                                        <Trash2 size={16} />
                                    </button>
                                </div>
                            </div>
                        ))}
                    </div>
                )}

                {/* Overall Progress */}
                <div className="overall-progress">
                    <div className="overall-progress-icon">
                        <Target size={24} />
                    </div>
                    <div>
                        <div className="overall-progress-title">Overall Progress</div>
                    </div>
                    <div className="overall-progress-stats">
                        <div className="overall-progress-stat">
                            <div className="overall-progress-stat-label">Total Goals</div>
                            <div className="overall-progress-stat-value">{goals.length}</div>
                        </div>
                        <div className="overall-progress-stat">
                            <div className="overall-progress-stat-label">Total Target</div>
                            <div className="overall-progress-stat-value">₹{goals.reduce((sum, g) => sum + (g.target || 0), 0).toLocaleString()}</div>
                        </div>
                        <div className="overall-progress-stat">
                            <div className="overall-progress-stat-label">Total Saved</div>
                            <div className="overall-progress-stat-value">₹{goals.reduce((sum, g) => sum + (g.current || 0), 0).toLocaleString()}</div>
                        </div>
                    </div>
                </div>

                {/* FAB */}
                <button className="fab" onClick={() => setShowAddModal(true)}>
                    <Plus size={24} />
                </button>

                {/* Add Goal Modal */}
                {showAddModal && (
                    <AddGoalModal onClose={() => setShowAddModal(false)} onAdd={loadGoals} icons={goalIcons} />
                )}

                {/* Add Funds Modal */}
                {showAddFundsModal && (
                    <AddFundsModal
                        goal={showAddFundsModal}
                        onClose={() => setShowAddFundsModal(null)}
                        onAdd={loadGoals}
                    />
                )}

                {/* View Details Modal */}
                {selectedGoal && (
                    <ViewDetailsModal
                        goal={selectedGoal}
                        onClose={() => setSelectedGoal(null)}
                    />
                )}

                {/* Delete Confirmation Modal */}
                {deleteConfirm && (
                    <div className="modal-overlay" onClick={() => setDeleteConfirm(null)}>
                        <div className="modal modal-sm" onClick={(e) => e.stopPropagation()}>
                            <div className="modal-header">
                                <h2 className="modal-title">Delete Goal</h2>
                                <button className="modal-close" onClick={() => setDeleteConfirm(null)}>×</button>
                            </div>
                            <div className="modal-body">
                                <p>Are you sure you want to delete this goal?</p>
                                <div className="delete-preview">
                                    <span style={{ marginRight: '8px' }}>{deleteConfirm.icon}</span>
                                    <strong>{deleteConfirm.name}</strong>
                                    <span style={{ marginLeft: 'auto' }}>
                                        ₹{(deleteConfirm.target || 0).toLocaleString()}
                                    </span>
                                </div>
                                <p className="text-muted" style={{ fontSize: '0.85rem', marginTop: 'var(--spacing-sm)' }}>
                                    This action cannot be undone.
                                </p>
                            </div>
                            <div className="modal-footer">
                                <button className="btn btn-secondary" onClick={() => setDeleteConfirm(null)}>Cancel</button>
                                <button className="btn btn-danger" onClick={() => handleDeleteGoal(deleteConfirm.id)}>
                                    Delete
                                </button>
                            </div>
                        </div>
                    </div>
                )}
            </div>
        </>
    );
}

function AddGoalModal({ onClose, onAdd, icons }) {
    const [formData, setFormData] = useState({
        name: '',
        targetAmount: '',
        deadline: '',
        icon: icons[0],
    });
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        try {
            await goalAPI.create({
                goalName: formData.name,
                targetAmount: parseFloat(formData.targetAmount),
                deadline: formData.deadline, // Use the date string directly (YYYY-MM-DD format)
                status: 'ACTIVE', // Valid status: ACTIVE, PAUSED, COMPLETED, ACHIEVED
            });
            // Close modal first, then reload in background
            onClose();
            setTimeout(() => onAdd(), 100);
        } catch (error) {
            console.error('Failed to create goal:', error);
            alert('Failed to create goal. Please try again.');
            setLoading(false);
        }
    };

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="modal" onClick={(e) => e.stopPropagation()}>
                <div className="modal-header">
                    <h2 className="modal-title">Add New Goal</h2>
                    <button className="modal-close" onClick={onClose}>×</button>
                </div>
                <form onSubmit={handleSubmit}>
                    <div className="modal-body">
                        <div className="input-group">
                            <label>Goal Name</label>
                            <input
                                type="text"
                                className="input"
                                placeholder="e.g., Emergency Fund"
                                value={formData.name}
                                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                                required
                            />
                        </div>
                        <div className="input-group" style={{ marginTop: 'var(--spacing-md)' }}>
                            <label>Select Icon</label>
                            <div className="icon-selector">
                                {icons.map((icon) => (
                                    <button
                                        key={icon}
                                        type="button"
                                        className={`icon-option ${formData.icon === icon ? 'active' : ''}`}
                                        onClick={() => setFormData({ ...formData, icon })}
                                    >
                                        {icon}
                                    </button>
                                ))}
                            </div>
                        </div>
                        <div className="input-group" style={{ marginTop: 'var(--spacing-md)' }}>
                            <label>Target Amount (₹)</label>
                            <input
                                type="number"
                                className="input"
                                placeholder="10000"
                                value={formData.targetAmount}
                                onChange={(e) => setFormData({ ...formData, targetAmount: e.target.value })}
                                required
                            />
                        </div>
                        <div className="input-group" style={{ marginTop: 'var(--spacing-md)' }}>
                            <label>Deadline</label>
                            <input
                                type="date"
                                className="input"
                                value={formData.deadline}
                                onChange={(e) => setFormData({ ...formData, deadline: e.target.value })}
                                required
                            />
                        </div>
                    </div>
                    <div className="modal-footer">
                        <button type="button" className="btn btn-secondary" onClick={onClose}>Cancel</button>
                        <button type="submit" className="btn btn-primary" disabled={loading}>
                            {loading ? 'Creating...' : 'Create Goal'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}

function AddFundsModal({ goal, onClose, onAdd }) {
    const [amount, setAmount] = useState('');
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        try {
            // In production, this would update the goal with additional funds
            await goalAPI.update(goal.id, {
                currentAmount: goal.current + parseFloat(amount)
            });
            onAdd();
            onClose();
        } catch (error) {
            console.error('Failed to add funds:', error);
            alert('Failed to add funds. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="modal" onClick={(e) => e.stopPropagation()}>
                <div className="modal-header">
                    <h2 className="modal-title">Add Funds to {goal.name}</h2>
                    <button className="modal-close" onClick={onClose}>×</button>
                </div>
                <form onSubmit={handleSubmit}>
                    <div className="modal-body">
                        <div className="add-funds-info">
                            <div className="add-funds-current">
                                <span className="text-secondary">Current Progress</span>
                                <span className="font-bold">₹{goal.current.toLocaleString()} / ₹{goal.target.toLocaleString()}</span>
                            </div>
                            <div className="progress-bar" style={{ marginTop: 'var(--spacing-sm)' }}>
                                <div className="progress-bar-fill" style={{ width: `${goal.progress}%` }}></div>
                            </div>
                        </div>
                        <div className="input-group" style={{ marginTop: 'var(--spacing-lg)' }}>
                            <label>Amount to Add (₹)</label>
                            <input
                                type="number"
                                className="input"
                                placeholder="500"
                                value={amount}
                                onChange={(e) => setAmount(e.target.value)}
                                required
                                min="1"
                            />
                        </div>
                    </div>
                    <div className="modal-footer">
                        <button type="button" className="btn btn-secondary" onClick={onClose}>Cancel</button>
                        <button type="submit" className="btn btn-primary" disabled={loading}>
                            {loading ? 'Adding...' : 'Add Funds'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}

function ViewDetailsModal({ goal, onClose }) {
    const formatDate = (dateStr) => {
        if (!dateStr) return 'No deadline set';
        const date = new Date(dateStr);
        return date.toLocaleDateString('en-US', { month: 'long', day: 'numeric', year: 'numeric' });
    };

    const remaining = (goal.target || 0) - (goal.current || 0);
    const daysLeft = goal.daysLeft > 0 ? goal.daysLeft : 0;
    const dailySavingsNeeded = daysLeft > 0 ? (remaining / daysLeft) : remaining;

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="modal" onClick={(e) => e.stopPropagation()}>
                <div className="modal-header">
                    <h2 className="modal-title">
                        <span style={{ marginRight: '8px' }}>{goal.icon}</span>
                        {goal.name}
                    </h2>
                    <button className="modal-close" onClick={onClose}>×</button>
                </div>
                <div className="modal-body">
                    {/* Progress Section */}
                    <div className="goal-detail-section">
                        <div className="goal-detail-progress">
                            <div className="goal-progress-header">
                                <span className="goal-progress-label">Progress</span>
                                <span className="goal-progress-percentage">{goal.progress}%</span>
                            </div>
                            <div className="progress-bar" style={{ height: '12px' }}>
                                <div className="progress-bar-fill" style={{ width: `${goal.progress}%` }}></div>
                            </div>
                        </div>
                    </div>

                    {/* Amounts */}
                    <div className="goal-detail-grid" style={{ marginTop: 'var(--spacing-lg)' }}>
                        <div className="goal-detail-item">
                            <div className="goal-detail-label">Target Amount</div>
                            <div className="goal-detail-value">₹{(goal.target || 0).toLocaleString()}</div>
                        </div>
                        <div className="goal-detail-item">
                            <div className="goal-detail-label">Saved Amount</div>
                            <div className="goal-detail-value" style={{ color: 'var(--success)' }}>₹{(goal.current || 0).toLocaleString()}</div>
                        </div>
                        <div className="goal-detail-item">
                            <div className="goal-detail-label">Remaining</div>
                            <div className="goal-detail-value" style={{ color: 'var(--warning)' }}>₹{remaining.toLocaleString()}</div>
                        </div>
                        <div className="goal-detail-item">
                            <div className="goal-detail-label">Deadline</div>
                            <div className="goal-detail-value">{formatDate(goal.deadline)}</div>
                        </div>
                    </div>

                    {/* Status and Days Left */}
                    <div className="goal-detail-grid" style={{ marginTop: 'var(--spacing-md)' }}>
                        <div className="goal-detail-item">
                            <div className="goal-detail-label">Status</div>
                            <div className={`badge badge-${goal.status?.toLowerCase() || 'active'}`}>
                                {goal.status || 'ACTIVE'}
                            </div>
                        </div>
                        <div className="goal-detail-item">
                            <div className="goal-detail-label">Days Left</div>
                            <div className="goal-detail-value" style={{ color: goal.daysLeft < 0 ? 'var(--error)' : 'inherit' }}>
                                {goal.daysLeft < 0 ? `${Math.abs(goal.daysLeft)} days overdue` : `${goal.daysLeft} days`}
                            </div>
                        </div>
                    </div>

                    {/* Daily Savings Tip */}
                    {remaining > 0 && daysLeft > 0 && (
                        <div className="goal-detail-tip" style={{ marginTop: 'var(--spacing-lg)' }}>
                            <span className="tip-icon">💡</span>
                            <span>Save <strong>₹{dailySavingsNeeded.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</strong> per day to reach your goal on time!</span>
                        </div>
                    )}

                    {goal.progress >= 100 && (
                        <div className="goal-detail-tip goal-achieved" style={{ marginTop: 'var(--spacing-lg)' }}>
                            <span className="tip-icon">🎉</span>
                            <span><strong>Congratulations!</strong> You've achieved this goal!</span>
                        </div>
                    )}
                </div>
                <div className="modal-footer">
                    <button className="btn btn-primary" onClick={onClose}>Close</button>
                </div>
            </div>
        </div>
    );
}
