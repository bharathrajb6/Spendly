import { useState, useEffect } from 'react';
import {
    PieChart,
    TrendingUp,
    AlertTriangle,
    CheckCircle,
    ChevronLeft,
    ChevronRight,
    Sparkles,
    DollarSign,
    RefreshCw,
    Edit2,
    Save,
    X
} from 'lucide-react';
import Header from '../components/layout/Header';
import { budgetAPI, transactionAPI } from '../api/api';
import './Budget.css';

// Category icons matching the transaction categories
const categoryConfig = {
    RENT: { emoji: '🏠', color: '#ef4444' },
    UTILITIES: { emoji: '💡', color: '#f59e0b' },
    HOUSEHOLD: { emoji: '🏡', color: '#8b5cf6' },
    FUEL: { emoji: '⛽', color: '#f97316' },
    TRANSPORT: { emoji: '🚗', color: '#3b82f6' },
    INSURANCE: { emoji: '🛡️', color: '#6366f1' },
    FOOD: { emoji: '🍕', color: '#10b981' },
    HEALTH: { emoji: '💊', color: '#ec4899' },
    BEAUTY: { emoji: '💄', color: '#d946ef' },
    ENTERTAINMENT: { emoji: '🎬', color: '#14b8a6' },
    HOUSING: { emoji: '🏠', color: '#ef4444' },
    OTHER: { emoji: '💰', color: '#64748b' },
};

// Helper to format category names
const formatCategory = (category) => {
    if (!category) return 'Other';
    const categoryMap = {
        'INVESTMENTINCOME': 'Investment Income',
        'RENTALINCOME': 'Rental Income',
        'TAXREFUND': 'Tax Refund',
    };
    if (categoryMap[category]) return categoryMap[category];
    return category.charAt(0).toUpperCase() + category.slice(1).toLowerCase();
};

// Format month-year for display
const formatMonthYear = (date) => {
    return date.toLocaleDateString('en-US', { month: 'long', year: 'numeric' });
};

// Format for API call (yyyy-MM)
const formatForAPI = (date) => {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    return `${year}-${month}`;
};

export default function Budget() {
    const [loading, setLoading] = useState(true);
    const [budgets, setBudgets] = useState([]);
    const [recommendations, setRecommendations] = useState([]);
    const [selectedMonth, setSelectedMonth] = useState(new Date());
    const [error, setError] = useState(null);
    const [showRecommendations, setShowRecommendations] = useState(false);
    const [creatingDefaults, setCreatingDefaults] = useState(false);
    const [spending, setSpending] = useState({});
    const [editingBudget, setEditingBudget] = useState(null);
    const [editAmount, setEditAmount] = useState('');
    const [saving, setSaving] = useState(false);


    useEffect(() => {
        loadBudgetData();
    }, [selectedMonth]);

    const loadBudgetData = async () => {
        setLoading(true);
        setError(null);
        try {
            const monthParam = formatForAPI(selectedMonth);

            // Fetch budgets, recommendations, and current month's transactions
            const [budgetsData, recommendationsData, transactionsData] = await Promise.all([
                budgetAPI.getForMonth(monthParam).catch(() => []),
                budgetAPI.getRecommendations().catch(() => []),
                transactionAPI.getByMonth(
                    selectedMonth.getMonth() + 1,
                    selectedMonth.getFullYear(),
                    0,
                    100
                ).catch(() => ({ content: [] })),
            ]);

            // Calculate spending by category from transactions
            const spendingByCategory = {};
            const transactions = transactionsData?.content || transactionsData || [];
            transactions.forEach(tx => {
                const type = (tx.type || tx.transactionType || '').toUpperCase();
                if (type === 'EXPENSE' && tx.category) {
                    const normalizedCategory = tx.category.toUpperCase();
                    spendingByCategory[normalizedCategory] = (spendingByCategory[normalizedCategory] || 0) + (tx.amount || 0);
                }
            });

            setBudgets(budgetsData || []);
            setRecommendations(recommendationsData || []);
            setSpending(spendingByCategory);
        } catch (err) {
            console.error('Failed to load budget data:', err);
            setError('Failed to load budget data. Please make sure the backend is running.');
        } finally {
            setLoading(false);
        }
    };

    const createDefaultBudgets = async () => {
        setCreatingDefaults(true);
        try {
            await budgetAPI.createDefaults();
            await loadBudgetData();
        } catch (err) {
            console.error('Failed to create default budgets:', err);
            alert('Failed to create default budgets. Please try again.');
        } finally {
            setCreatingDefaults(false);
        }
    };

    const handleEditBudget = (budget) => {
        setEditingBudget(budget);
        setEditAmount(budget.limitAmount.toString());
    };

    const handleCancelEdit = () => {
        setEditingBudget(null);
        setEditAmount('');
    };

    const handleSaveBudget = async () => {
        if (!editingBudget || !editAmount) return;

        const newAmount = parseFloat(editAmount);
        if (isNaN(newAmount) || newAmount <= 0) {
            alert('Please enter a valid amount greater than 0');
            return;
        }

        setSaving(true);
        try {
            await budgetAPI.update(editingBudget.budgetId, newAmount);
            await loadBudgetData();
            setEditingBudget(null);
            setEditAmount('');
        } catch (err) {
            console.error('Failed to update budget:', err);
            alert('Failed to update budget. Please try again.');
        } finally {
            setSaving(false);
        }
    };

    const navigateMonth = (direction) => {
        const newDate = new Date(selectedMonth);
        newDate.setMonth(newDate.getMonth() + direction);
        setSelectedMonth(newDate);
    };

    const getBudgetStatus = (budget) => {
        const spent = spending[budget.category] || 0;
        const percentage = budget.limitAmount > 0 ? (spent / budget.limitAmount) * 100 : 0;

        if (percentage >= 100) return 'exceeded';
        if (percentage >= 80) return 'warning';
        return 'on-track';
    };

    const getStatusIcon = (status) => {
        switch (status) {
            case 'exceeded': return <AlertTriangle size={16} />;
            case 'warning': return <AlertTriangle size={16} />;
            case 'on-track': return <CheckCircle size={16} />;
            default: return <CheckCircle size={16} />;
        }
    };

    const getStatusLabel = (status) => {
        switch (status) {
            case 'exceeded': return 'Over Budget';
            case 'warning': return 'Almost There';
            case 'on-track': return 'On Track';
            default: return 'On Track';
        }
    };

    // Calculate totals
    const totalBudget = budgets.reduce((sum, b) => sum + (b.limitAmount || 0), 0);
    const totalSpent = Object.values(spending).reduce((sum, s) => sum + s, 0);
    const totalRemaining = totalBudget - totalSpent;
    const overallPercentage = totalBudget > 0 ? Math.min((totalSpent / totalBudget) * 100, 100) : 0;

    if (loading) {
        return (
            <>
                <Header title="Budget" />
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
            <Header title="Budget" />
            <div className="page-content">
                {/* Header Section */}
                <div className="budget-header">
                    <div>
                        <h1>Monthly Budget</h1>
                        <p className="text-secondary">Track and manage your spending limits</p>
                    </div>
                    <div className="budget-actions">
                        {recommendations.length > 0 && (
                            <button
                                className="btn btn-outline recommendations-btn"
                                onClick={() => setShowRecommendations(true)}
                            >
                                <Sparkles size={18} />
                                View Recommendations
                                <span className="recommendations-badge">{recommendations.length}</span>
                            </button>
                        )}
                    </div>
                </div>

                {/* Month Navigation */}
                <div className="month-navigator">
                    <button className="month-nav-btn" onClick={() => navigateMonth(-1)}>
                        <ChevronLeft size={20} />
                    </button>
                    <span className="month-label">{formatMonthYear(selectedMonth)}</span>
                    <button className="month-nav-btn" onClick={() => navigateMonth(1)}>
                        <ChevronRight size={20} />
                    </button>
                </div>

                {/* Error State */}
                {error && (
                    <div className="card" style={{ marginBottom: 'var(--spacing-lg)' }}>
                        <div className="empty-state">
                            <div className="empty-state-icon">⚠️</div>
                            <div className="empty-state-title">Error loading budgets</div>
                            <p>{error}</p>
                            <button className="btn btn-primary" style={{ marginTop: 'var(--spacing-md)' }} onClick={loadBudgetData}>
                                <RefreshCw size={16} />
                                Try Again
                            </button>
                        </div>
                    </div>
                )}

                {/* No Budgets State */}
                {!error && budgets.length === 0 ? (
                    <div className="card" style={{ marginBottom: 'var(--spacing-lg)' }}>
                        <div className="empty-state">
                            <div className="empty-state-icon">📊</div>
                            <div className="empty-state-title">No budgets set for this month</div>
                            <p>Create default budgets based on your spending patterns to get started.</p>
                            <button
                                className="btn btn-primary"
                                style={{ marginTop: 'var(--spacing-md)' }}
                                onClick={createDefaultBudgets}
                                disabled={creatingDefaults}
                            >
                                {creatingDefaults ? (
                                    <>
                                        <RefreshCw size={16} className="spinning" />
                                        Creating...
                                    </>
                                ) : (
                                    <>
                                        <Sparkles size={16} />
                                        Create Default Budgets
                                    </>
                                )}
                            </button>
                        </div>
                    </div>
                ) : (
                    <>
                        {/* Budget Overview Cards */}
                        <div className="budget-overview">
                            <div className="budget-overview-card">
                                <div className="budget-overview-icon blue">
                                    <DollarSign size={24} />
                                </div>
                                <div className="budget-overview-content">
                                    <div className="budget-overview-label">Total Budget</div>
                                    <div className="budget-overview-value">₹{totalBudget.toLocaleString()}</div>
                                </div>
                            </div>
                            <div className="budget-overview-card">
                                <div className="budget-overview-icon red">
                                    <TrendingUp size={24} />
                                </div>
                                <div className="budget-overview-content">
                                    <div className="budget-overview-label">Total Spent</div>
                                    <div className="budget-overview-value">₹{totalSpent.toLocaleString()}</div>
                                </div>
                            </div>
                            <div className="budget-overview-card">
                                <div className={`budget-overview-icon ${totalRemaining >= 0 ? 'green' : 'red'}`}>
                                    <PieChart size={24} />
                                </div>
                                <div className="budget-overview-content">
                                    <div className="budget-overview-label">Remaining</div>
                                    <div className={`budget-overview-value ${totalRemaining < 0 ? 'negative' : ''}`}>
                                        ₹{Math.abs(totalRemaining).toLocaleString()}
                                        {totalRemaining < 0 && ' over'}
                                    </div>
                                </div>
                            </div>
                        </div>

                        {/* Overall Progress Bar */}
                        <div className="budget-progress-section">
                            <div className="budget-progress-header">
                                <span>Overall Budget Usage</span>
                                <span className={`budget-percentage ${overallPercentage >= 100 ? 'exceeded' : overallPercentage >= 80 ? 'warning' : ''}`}>
                                    {overallPercentage.toFixed(1)}%
                                </span>
                            </div>
                            <div className="budget-progress-bar">
                                <div
                                    className={`budget-progress-fill ${overallPercentage >= 100 ? 'exceeded' : overallPercentage >= 80 ? 'warning' : ''}`}
                                    style={{ width: `${Math.min(overallPercentage, 100)}%` }}
                                ></div>
                            </div>
                        </div>

                        {/* Budget Cards Grid */}
                        <div className="budget-grid">
                            {budgets.map((budget) => {
                                const spent = spending[budget.category] || 0;
                                const percentage = budget.limitAmount > 0 ? (spent / budget.limitAmount) * 100 : 0;
                                const remaining = budget.limitAmount - spent;
                                const status = getBudgetStatus(budget);
                                const config = categoryConfig[budget.category] || categoryConfig.OTHER;

                                return (
                                    <div key={budget.budgetId} className={`budget-card status-${status}`}>
                                        <div className="budget-card-header">
                                            <div className="budget-card-category">
                                                <span
                                                    className="budget-card-icon"
                                                    style={{ background: `${config.color}15` }}
                                                >
                                                    {config.emoji}
                                                </span>
                                                <div>
                                                    <div className="budget-card-name">{formatCategory(budget.category)}</div>
                                                    <div className={`budget-card-status ${status}`}>
                                                        {getStatusIcon(status)}
                                                        {getStatusLabel(status)}
                                                    </div>
                                                </div>
                                            </div>
                                            <button
                                                className="budget-edit-btn"
                                                onClick={() => handleEditBudget(budget)}
                                                title="Edit budget limit"
                                            >
                                                <Edit2 size={16} />
                                            </button>
                                        </div>

                                        <div className="budget-card-amounts">
                                            <div className="budget-card-spent">
                                                <span className="amount">₹{spent.toLocaleString()}</span>
                                                <span className="label">spent</span>
                                            </div>
                                            <div className="budget-card-limit">
                                                <span className="label">of</span>
                                                <span className="amount">₹{budget.limitAmount.toLocaleString()}</span>
                                            </div>
                                        </div>

                                        <div className="budget-card-progress">
                                            <div className="budget-card-progress-bar">
                                                <div
                                                    className={`budget-card-progress-fill ${status}`}
                                                    style={{
                                                        width: `${Math.min(percentage, 100)}%`,
                                                        background: status === 'exceeded' ? '#ef4444' :
                                                            status === 'warning' ? '#f59e0b' : config.color
                                                    }}
                                                ></div>
                                            </div>
                                            <div className="budget-card-percentage">{percentage.toFixed(0)}%</div>
                                        </div>

                                        <div className="budget-card-footer">
                                            <span className={`budget-remaining ${remaining < 0 ? 'negative' : ''}`}>
                                                {remaining >= 0 ? (
                                                    <>💚 ₹{remaining.toLocaleString()} remaining</>
                                                ) : (
                                                    <>🔴 ₹{Math.abs(remaining).toLocaleString()} over budget</>
                                                )}
                                            </span>
                                        </div>

                                        {budget.recommendedLimitAmount && budget.recommendedLimitAmount !== budget.limitAmount && (
                                            <div className="budget-card-recommendation">
                                                <Sparkles size={14} />
                                                Recommended: ₹{budget.recommendedLimitAmount.toLocaleString()}
                                            </div>
                                        )}
                                    </div>
                                );
                            })}
                        </div>
                    </>
                )}

                {/* Recommendations Modal */}
                {showRecommendations && (
                    <div className="modal-overlay" onClick={() => setShowRecommendations(false)}>
                        <div className="modal recommendations-modal" onClick={(e) => e.stopPropagation()}>
                            <div className="modal-header">
                                <h2 className="modal-title">
                                    <Sparkles size={20} style={{ marginRight: '8px', color: 'var(--color-accent)' }} />
                                    Budget Recommendations
                                </h2>
                                <button className="modal-close" onClick={() => setShowRecommendations(false)}>×</button>
                            </div>
                            <div className="modal-body">
                                <p className="recommendations-intro">
                                    Based on your spending patterns over the last 3 months, here are our recommendations:
                                </p>
                                <div className="recommendations-list">
                                    {recommendations.map((rec, index) => {
                                        const config = categoryConfig[rec.category] || categoryConfig.OTHER;
                                        const diff = rec.suggestedLimit - rec.currentLimit;
                                        const isIncrease = diff > 0;

                                        return (
                                            <div key={index} className="recommendation-item">
                                                <div className="recommendation-category">
                                                    <span className="recommendation-icon" style={{ background: `${config.color}15` }}>
                                                        {config.emoji}
                                                    </span>
                                                    <span className="recommendation-name">{formatCategory(rec.category)}</span>
                                                </div>
                                                <div className="recommendation-amounts">
                                                    <div className="recommendation-current">
                                                        <span className="label">Current</span>
                                                        <span className="amount">₹{rec.currentLimit.toLocaleString()}</span>
                                                    </div>
                                                    <div className="recommendation-arrow">→</div>
                                                    <div className="recommendation-suggested">
                                                        <span className="label">Suggested</span>
                                                        <span className={`amount ${isIncrease ? 'increase' : 'decrease'}`}>
                                                            ₹{rec.suggestedLimit.toLocaleString()}
                                                        </span>
                                                    </div>
                                                </div>
                                                {rec.recommendationNote && (
                                                    <div className="recommendation-note">
                                                        💡 {rec.recommendationNote}
                                                    </div>
                                                )}
                                            </div>
                                        );
                                    })}
                                </div>
                            </div>
                            <div className="modal-footer">
                                <button className="btn btn-primary" onClick={() => setShowRecommendations(false)}>
                                    Got it!
                                </button>
                            </div>
                        </div>
                    </div>
                )}

                {/* Edit Budget Modal */}
                {editingBudget && (
                    <div className="modal-overlay" onClick={handleCancelEdit}>
                        <div className="modal edit-budget-modal" onClick={(e) => e.stopPropagation()}>
                            <div className="modal-header">
                                <h2 className="modal-title">
                                    <Edit2 size={20} style={{ marginRight: '8px', color: 'var(--color-accent)' }} />
                                    Edit Budget Limit
                                </h2>
                                <button className="modal-close" onClick={handleCancelEdit}>×</button>
                            </div>
                            <div className="modal-body">
                                <div className="edit-budget-category">
                                    <span
                                        className="budget-card-icon"
                                        style={{ background: `${(categoryConfig[editingBudget.category] || categoryConfig.OTHER).color}15` }}
                                    >
                                        {(categoryConfig[editingBudget.category] || categoryConfig.OTHER).emoji}
                                    </span>
                                    <span className="edit-budget-category-name">
                                        {formatCategory(editingBudget.category)}
                                    </span>
                                </div>
                                <div className="edit-budget-form">
                                    <label htmlFor="budget-limit">Monthly Limit (₹)</label>
                                    <div className="edit-budget-input-wrapper">
                                        <DollarSign size={18} className="input-icon" />
                                        <input
                                            id="budget-limit"
                                            type="number"
                                            min="0"
                                            step="0.01"
                                            value={editAmount}
                                            onChange={(e) => setEditAmount(e.target.value)}
                                            placeholder="Enter budget limit"
                                            autoFocus
                                        />
                                    </div>
                                    <p className="edit-budget-hint">
                                        Current limit: ₹{editingBudget.limitAmount.toLocaleString()}
                                    </p>
                                </div>
                            </div>
                            <div className="modal-footer">
                                <button className="btn btn-outline" onClick={handleCancelEdit}>
                                    <X size={16} />
                                    Cancel
                                </button>
                                <button
                                    className="btn btn-primary"
                                    onClick={handleSaveBudget}
                                    disabled={saving || !editAmount}
                                >
                                    {saving ? (
                                        <>
                                            <RefreshCw size={16} className="spinning" />
                                            Saving...
                                        </>
                                    ) : (
                                        <>
                                            <Save size={16} />
                                            Save Changes
                                        </>
                                    )}
                                </button>
                            </div>
                        </div>
                    </div>
                )}
            </div>
        </>
    );
}
