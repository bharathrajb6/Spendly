import { useState, useEffect } from 'react';
import {
    Wallet,
    TrendingUp,
    TrendingDown,
    Target,
    Plus,
    ArrowUpRight,
    ArrowDownRight,
    X,
    Receipt
} from 'lucide-react';
import { Chart as ChartJS, ArcElement, Tooltip, Legend, CategoryScale, LinearScale, BarElement } from 'chart.js';
import { Doughnut, Bar } from 'react-chartjs-2';
import Header from '../components/layout/Header';
import { analyticsAPI, transactionAPI, goalAPI } from '../api/api';
import './Dashboard.css';

// Register ChartJS components
ChartJS.register(ArcElement, Tooltip, Legend, CategoryScale, LinearScale, BarElement);

// Category icons and colors - matching backend ExpenseCategory and IncomeCategory enums
const categoryConfig = {
    // Expense categories
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
    // Income categories
    SALARY: { emoji: '💵', color: '#22c55e' },
    BUSINESS: { emoji: '💼', color: '#0ea5e9' },
    INVESTMENTINCOME: { emoji: '📈', color: '#84cc16' },
    RENTALINCOME: { emoji: '🏢', color: '#a855f7' },
    TAXREFUND: { emoji: '🧾', color: '#06b6d4' },
    BONUS: { emoji: '🎁', color: '#f43f5e' },
    GIFTS: { emoji: '🎀', color: '#e879f9' },
    OTHER: { emoji: '💰', color: '#64748b' },
};

// Helper to format category names for display
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

export default function Dashboard() {
    const [loading, setLoading] = useState(true);
    const [summary, setSummary] = useState(null);
    const [transactions, setTransactions] = useState([]);
    const [allTransactions, setAllTransactions] = useState([]);
    const [goalSummary, setGoalSummary] = useState(null);
    const [fabMenuOpen, setFabMenuOpen] = useState(false);
    const [showAddTransaction, setShowAddTransaction] = useState(false);
    const [showAddGoal, setShowAddGoal] = useState(false);

    useEffect(() => {
        loadDashboardData();
    }, []);

    const loadDashboardData = async () => {
        try {
            const [summaryData, recentTransactions, allTxData, goalsData] = await Promise.all([
                analyticsAPI.getFinancialSummary().catch(() => null),
                transactionAPI.getAll(0, 5).catch(() => ({ content: [] })),
                transactionAPI.getAll(0, 100).catch(() => ({ content: [] })), // Get more for charts
                goalAPI.getSummary().catch(() => null),
            ]);

            setSummary(summaryData);
            setTransactions(recentTransactions?.content || recentTransactions || []);
            setAllTransactions(allTxData?.content || allTxData || []);
            setGoalSummary(goalsData);
        } catch (error) {
            console.error('Failed to load dashboard data:', error);
        } finally {
            setLoading(false);
        }
    };

    // Compute spending by category from real transactions
    const computeSpendingByCategory = () => {
        // Use backend ExpenseCategory enum values
        const expenseCategories = ['FOOD', 'TRANSPORT', 'RENT', 'UTILITIES', 'ENTERTAINMENT', 'HEALTH', 'OTHER'];
        const colors = ['#10b981', '#3b82f6', '#ef4444', '#f59e0b', '#14b8a6', '#ec4899', '#64748b'];

        const spending = expenseCategories.map(cat => {
            return allTransactions
                .filter(tx => tx.type === 'EXPENSE' && tx.category === cat)
                .reduce((sum, tx) => sum + (tx.amount || 0), 0);
        });

        // If no data, show placeholder
        const hasData = spending.some(v => v > 0);

        return {
            labels: expenseCategories.map(cat => formatCategory(cat)),
            categories: expenseCategories, // Store original category names for reference
            datasets: [{
                data: hasData ? spending : expenseCategories.map(() => 0),
                backgroundColor: colors,
                borderWidth: 0,
                cutout: '70%',
            }],
            hasData
        };
    };

    const spendingData = computeSpendingByCategory();

    // Financial overview - shows current summary data
    const financialOverviewData = {
        labels: ['Current Month'],
        datasets: [
            {
                label: 'Income',
                data: [summary?.totalIncome ?? 0],
                backgroundColor: '#10b981',
                borderRadius: 6,
            },
            {
                label: 'Expenses',
                data: [summary?.totalExpense ?? 0],
                backgroundColor: '#ef4444',
                borderRadius: 6,
            }
        ]
    };

    const chartOptions = {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
            legend: {
                position: 'bottom',
                labels: {
                    usePointStyle: true,
                    padding: 20,
                }
            }
        }
    };

    const barOptions = {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
            legend: {
                position: 'bottom',
                labels: {
                    usePointStyle: true,
                    padding: 20,
                }
            }
        },
        scales: {
            x: {
                grid: { display: false },
            },
            y: {
                grid: { color: '#f0f0f0' },
                beginAtZero: true,
            }
        }
    };

    if (loading) {
        return (
            <>
                <Header title="Dashboard" />
                <div className="page-content">
                    <div className="loading-container">
                        <div className="spinner"></div>
                    </div>
                </div>
            </>
        );
    }

    const totalBalance = summary?.remainingBalance ?? 0;
    const monthlyIncome = summary?.totalIncome ?? 0;
    const monthlyExpenses = summary?.totalExpense ?? 0;
    const savingsProgress = goalSummary?.progressPercentage ?? 0;

    return (
        <>
            <Header title="Dashboard" />
            <div className="page-content">
                {/* Summary Cards */}
                <div className="grid-4">
                    <div className="summary-card">
                        <div className="summary-card-icon blue">
                            <Wallet size={24} />
                        </div>
                        <div className="summary-card-content">
                            <div className="summary-card-label">Total Balance</div>
                            <div className="summary-card-value">₹{totalBalance.toLocaleString()}</div>
                        </div>
                        <span className="summary-card-trend positive">
                            <ArrowUpRight size={14} /> +12%
                        </span>
                    </div>

                    <div className="summary-card">
                        <div className="summary-card-icon green">
                            <TrendingUp size={24} />
                        </div>
                        <div className="summary-card-content">
                            <div className="summary-card-label">Monthly Income</div>
                            <div className="summary-card-value">₹{monthlyIncome.toLocaleString()}</div>
                        </div>
                        <span className="summary-card-trend positive">
                            <ArrowUpRight size={14} /> +8%
                        </span>
                    </div>

                    <div className="summary-card">
                        <div className="summary-card-icon red">
                            <TrendingDown size={24} />
                        </div>
                        <div className="summary-card-content">
                            <div className="summary-card-label">Monthly Expenses</div>
                            <div className="summary-card-value">₹{monthlyExpenses.toLocaleString()}</div>
                        </div>
                        <span className="summary-card-trend negative">
                            <ArrowDownRight size={14} /> -5%
                        </span>
                    </div>

                    <div className="summary-card">
                        <div className="summary-card-icon purple">
                            <Target size={24} />
                        </div>
                        <div className="summary-card-content">
                            <div className="summary-card-label">Savings Goal</div>
                            <div className="summary-card-value">{savingsProgress}%</div>
                        </div>
                        <span className="summary-card-trend positive">
                            <ArrowUpRight size={14} /> +15%
                        </span>
                    </div>
                </div>

                {/* Charts Row */}
                <div className="grid-2" style={{ marginTop: 'var(--spacing-lg)' }}>
                    {/* Spending Categories */}
                    <div className="card">
                        <div className="card-header">
                            <h3 className="card-title">Spending Categories</h3>
                            <span className="text-sm text-secondary">This month</span>
                        </div>
                        <div className="chart-container" style={{ height: '250px' }}>
                            <Doughnut data={spendingData} options={chartOptions} />
                        </div>
                        <div className="spending-legend">
                            {spendingData.labels.map((label, idx) => (
                                <div key={label} className="spending-legend-item">
                                    <span
                                        className="spending-legend-dot"
                                        style={{ background: spendingData.datasets[0].backgroundColor[idx] }}
                                    ></span>
                                    <span className="spending-legend-label">{label}</span>
                                    <span className="spending-legend-value">₹{spendingData.datasets[0].data[idx].toLocaleString()}</span>
                                </div>
                            ))}
                        </div>
                    </div>

                    {/* Financial Overview */}
                    <div className="card">
                        <div className="card-header">
                            <h3 className="card-title">Financial Overview</h3>
                            <select className="select">
                                <option>Last 6 months</option>
                                <option>Last 3 months</option>
                                <option>This year</option>
                            </select>
                        </div>
                        <div className="chart-container" style={{ height: '300px' }}>
                            <Bar data={financialOverviewData} options={barOptions} />
                        </div>
                    </div>
                </div>

                {/* Recent Transactions */}
                <div className="card" style={{ marginTop: 'var(--spacing-lg)' }}>
                    <div className="card-header">
                        <h3 className="card-title">Recent Transactions</h3>
                        <a href="/transactions" className="text-sm" style={{ color: 'var(--color-accent)' }}>
                            View All
                        </a>
                    </div>
                    <div className="transactions-list">
                        {transactions.length > 0 ? (
                            transactions.map((tx) => (
                                <div key={tx.transactionID || tx.id} className="transaction-item">
                                    <div className="transaction-icon">
                                        {categoryConfig[tx.category]?.emoji || '💰'}
                                    </div>
                                    <div className="transaction-details">
                                        <div className="transaction-name">{tx.notes || tx.description || tx.name || 'No description'}</div>
                                        <div className="transaction-meta">
                                            <span className={`badge badge-${tx.category?.toLowerCase() || 'other'}`}>
                                                {formatCategory(tx.category)}
                                            </span>
                                            <span className="text-muted">• {new Date(tx.transactionDate || tx.date || tx.createdAt).toLocaleDateString()}</span>
                                        </div>
                                    </div>
                                    <div className={`transaction-amount ${tx.type === 'INCOME' ? 'income' : 'expense'}`}>
                                        {tx.type === 'INCOME' ? '+' : '-'}₹{Math.abs(tx.amount).toLocaleString()}
                                    </div>
                                </div>
                            ))
                        ) : (
                            <div className="empty-state">
                                <div className="empty-state-icon">📝</div>
                                <div className="empty-state-title">No transactions yet</div>
                                <p>Add your first transaction to get started.</p>
                            </div>
                        )}
                    </div>
                </div>

                {/* FAB with Popup Menu */}
                <div className="fab-container">
                    {/* Backdrop when menu is open */}
                    {fabMenuOpen && (
                        <div className="fab-backdrop" onClick={() => setFabMenuOpen(false)} />
                    )}

                    {/* Popup Menu Items */}
                    <div className={`fab-menu ${fabMenuOpen ? 'open' : ''}`}>
                        <button
                            className="fab-menu-item"
                            onClick={() => {
                                setFabMenuOpen(false);
                                setShowAddTransaction(true);
                            }}
                        >
                            <Receipt size={20} />
                            <span>Add Transaction</span>
                        </button>
                        <button
                            className="fab-menu-item"
                            onClick={() => {
                                setFabMenuOpen(false);
                                setShowAddGoal(true);
                            }}
                        >
                            <Target size={20} />
                            <span>Add Goal</span>
                        </button>
                    </div>

                    {/* FAB Button */}
                    <button
                        className={`fab ${fabMenuOpen ? 'active' : ''}`}
                        onClick={() => setFabMenuOpen(!fabMenuOpen)}
                    >
                        {fabMenuOpen ? <X size={24} /> : <Plus size={24} />}
                    </button>
                </div>

                {/* Add Transaction Modal */}
                {showAddTransaction && (
                    <AddTransactionModal
                        onClose={() => setShowAddTransaction(false)}
                        onAdd={() => {
                            loadDashboardData();
                            setShowAddTransaction(false);
                        }}
                    />
                )}

                {/* Add Goal Modal */}
                {showAddGoal && (
                    <AddGoalModal
                        onClose={() => setShowAddGoal(false)}
                        onAdd={() => {
                            loadDashboardData();
                            setShowAddGoal(false);
                        }}
                    />
                )}
            </div>
        </>
    );
}

// Expense and Income categories matching backend
const expenseCategories = ['RENT', 'UTILITIES', 'HOUSEHOLD', 'FUEL', 'TRANSPORT', 'INSURANCE', 'FOOD', 'HEALTH', 'BEAUTY', 'ENTERTAINMENT', 'OTHER'];
const incomeCategories = ['SALARY', 'BUSINESS', 'INVESTMENTINCOME', 'RENTALINCOME', 'TAXREFUND', 'BONUS', 'GIFTS', 'OTHER'];

function AddTransactionModal({ onClose, onAdd }) {
    const [formData, setFormData] = useState({
        description: '',
        amount: '',
        type: 'EXPENSE',
        category: expenseCategories[0],
        date: new Date().toISOString().split('T')[0],
    });

    const currentCategories = formData.type === 'INCOME' ? incomeCategories : expenseCategories;

    const handleTypeChange = (newType) => {
        const newCategories = newType === 'INCOME' ? incomeCategories : expenseCategories;
        setFormData({ ...formData, type: newType, category: newCategories[0] });
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        onClose();

        transactionAPI.create({
            amount: parseFloat(formData.amount),
            category: formData.category,
            type: formData.type,
            notes: formData.description,
            paymentType: 'CARD',
            recurring: false,
            frequency: 0,
        }).then(() => {
            setTimeout(onAdd, 500);
        }).catch(console.error);
    };

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="modal" onClick={(e) => e.stopPropagation()}>
                <div className="modal-header">
                    <h2 className="modal-title">Add Transaction</h2>
                    <button className="modal-close" onClick={onClose}>×</button>
                </div>
                <form onSubmit={handleSubmit}>
                    <div className="modal-body">
                        <div className="input-group">
                            <label>Description</label>
                            <input
                                type="text"
                                className="input"
                                placeholder="Enter description"
                                value={formData.description}
                                onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                                required
                            />
                        </div>
                        <div className="input-group" style={{ marginTop: 'var(--spacing-md)' }}>
                            <label>Amount</label>
                            <input
                                type="number"
                                className="input"
                                placeholder="0.00"
                                value={formData.amount}
                                onChange={(e) => setFormData({ ...formData, amount: e.target.value })}
                                required
                            />
                        </div>
                        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 'var(--spacing-md)', marginTop: 'var(--spacing-md)' }}>
                            <div className="input-group">
                                <label>Type</label>
                                <select className="select" value={formData.type} onChange={(e) => handleTypeChange(e.target.value)} style={{ width: '100%' }}>
                                    <option value="EXPENSE">Expense</option>
                                    <option value="INCOME">Income</option>
                                </select>
                            </div>
                            <div className="input-group">
                                <label>Category</label>
                                <select className="select" value={formData.category} onChange={(e) => setFormData({ ...formData, category: e.target.value })} style={{ width: '100%' }}>
                                    {currentCategories.map((cat) => (
                                        <option key={cat} value={cat}>{formatCategory(cat)}</option>
                                    ))}
                                </select>
                            </div>
                        </div>
                        <div className="input-group" style={{ marginTop: 'var(--spacing-md)' }}>
                            <label>Date</label>
                            <input
                                type="date"
                                className="input"
                                value={formData.date}
                                onChange={(e) => setFormData({ ...formData, date: e.target.value })}
                                required
                            />
                        </div>
                    </div>
                    <div className="modal-footer">
                        <button type="button" className="btn btn-secondary" onClick={onClose}>Cancel</button>
                        <button type="submit" className="btn btn-primary">Add Transaction</button>
                    </div>
                </form>
            </div>
        </div>
    );
}

function AddGoalModal({ onClose, onAdd }) {
    const [formData, setFormData] = useState({
        name: '',
        targetAmount: '',
        deadline: '',
    });

    const handleSubmit = (e) => {
        e.preventDefault();
        onClose();

        goalAPI.create({
            goalName: formData.name,
            targetAmount: parseFloat(formData.targetAmount),
            deadline: formData.deadline,
            status: 'ACTIVE',
        }).then(() => {
            setTimeout(onAdd, 500);
        }).catch(console.error);
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
                        <button type="submit" className="btn btn-primary">Create Goal</button>
                    </div>
                </form>
            </div>
        </div>
    );
}
