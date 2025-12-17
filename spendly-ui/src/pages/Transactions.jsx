import { useState, useEffect } from 'react';
import { Search, Filter, Plus, ArrowUpCircle, ArrowDownCircle, Edit2, Trash2 } from 'lucide-react';
import Header from '../components/layout/Header';
import { transactionAPI } from '../api/api';
import './Transactions.css';

// Income Categories from backend: SALARY, BUSINESS, INVESTMENTINCOME, RENTALINCOME, TAXREFUND, BONUS, GIFTS, OTHER
const incomeCategories = ['SALARY', 'BUSINESS', 'INVESTMENTINCOME', 'RENTALINCOME', 'TAXREFUND', 'BONUS', 'GIFTS', 'OTHER'];

// Expense Categories from backend: RENT, UTILITIES, HOUSEHOLD, FUEL, TRANSPORT, INSURANCE, FOOD, HEALTH, BEAUTY, ENTERTAINMENT, OTHER
const expenseCategories = ['RENT', 'UTILITIES', 'HOUSEHOLD', 'FUEL', 'TRANSPORT', 'INSURANCE', 'FOOD', 'HEALTH', 'BEAUTY', 'ENTERTAINMENT', 'OTHER'];

// All categories for filtering
const allCategories = ['All Categories', ...new Set([...incomeCategories, ...expenseCategories])];

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

const types = ['All Types', 'INCOME', 'EXPENSE'];

// Helper function to format category for display
const formatCategory = (category) => {
    if (!category) return 'Other';

    // Map of special compound words
    const categoryMap = {
        'INVESTMENTINCOME': 'Investment Income',
        'RENTALINCOME': 'Rental Income',
        'TAXREFUND': 'Tax Refund',
    };

    // Check if it's a compound word
    if (categoryMap[category]) {
        return categoryMap[category];
    }

    // Convert single word from UPPERCASE to Title Case
    return category.charAt(0).toUpperCase() + category.slice(1).toLowerCase();
};

export default function Transactions() {
    const [loading, setLoading] = useState(true);
    const [transactions, setTransactions] = useState([]);
    const [error, setError] = useState(null);
    const [searchTerm, setSearchTerm] = useState('');
    const [selectedType, setSelectedType] = useState('All Types');
    const [selectedCategory, setSelectedCategory] = useState('All Categories');
    const [showAddModal, setShowAddModal] = useState(false);
    const [editingTransaction, setEditingTransaction] = useState(null);
    const [deleteConfirm, setDeleteConfirm] = useState(null);

    useEffect(() => {
        loadTransactions();
    }, []);

    const loadTransactions = async () => {
        setLoading(true);
        setError(null);
        try {
            const data = await transactionAPI.getAll(0, 50);
            setTransactions(data?.content || data || []);
        } catch (err) {
            console.error('Failed to load transactions:', err);
            setError('Failed to load transactions. Please make sure the backend is running.');
            setTransactions([]);
        } finally {
            setLoading(false);
        }
    };

    const handleDelete = (transactionId) => {
        console.log('Deleting transaction:', transactionId);

        // Close modal immediately
        setDeleteConfirm(null);

        // Fire API call in background
        transactionAPI.delete(transactionId)
            .then(() => {
                console.log('Transaction deleted successfully');
                loadTransactions();
            })
            .catch((err) => {
                console.error('Failed to delete transaction:', err);
            });

        // Reload after a delay
        setTimeout(() => loadTransactions(), 1500);
    };

    const filteredTransactions = transactions.filter((tx) => {
        const searchText = (tx.notes || tx.description || '').toLowerCase();
        const matchesSearch = searchText.includes(searchTerm.toLowerCase());
        const matchesType = selectedType === 'All Types' || tx.type === selectedType;
        const matchesCategory = selectedCategory === 'All Categories' || tx.category === selectedCategory;
        return matchesSearch && matchesType && matchesCategory;
    });

    const formatDate = (dateStr) => {
        const date = new Date(dateStr);
        return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
    };

    return (
        <>
            <Header title="Transactions" />
            <div className="page-content">
                <div className="page-header">
                    <h1>All Transactions</h1>
                    <p>Track and manage your income and expenses</p>
                </div>

                {/* Filters */}
                <div className="card filters-card">
                    <div className="filters-header">
                        <Filter size={20} />
                        <span>Filters</span>
                    </div>
                    <div className="filters-row">
                        <div className="input-with-icon" style={{ flex: 1 }}>
                            <Search size={18} className="icon" />
                            <input
                                type="text"
                                className="input"
                                placeholder="Search transactions..."
                                value={searchTerm}
                                onChange={(e) => setSearchTerm(e.target.value)}
                            />
                        </div>
                        <select
                            className="select"
                            value={selectedType}
                            onChange={(e) => setSelectedType(e.target.value)}
                        >
                            {types.map((type) => (
                                <option key={type} value={type}>{type === 'All Types' ? type : type.charAt(0) + type.slice(1).toLowerCase()}</option>
                            ))}
                        </select>
                        <select
                            className="select"
                            value={selectedCategory}
                            onChange={(e) => setSelectedCategory(e.target.value)}
                        >
                            {allCategories.map((cat) => (
                                <option key={cat} value={cat}>{cat === 'All Categories' ? cat : formatCategory(cat)}</option>
                            ))}
                        </select>
                    </div>
                </div>

                {/* Transactions List */}
                <div className="card" style={{ marginTop: 'var(--spacing-lg)', padding: 0 }}>
                    {loading ? (
                        <div className="loading-container">
                            <div className="spinner"></div>
                        </div>
                    ) : error ? (
                        <div className="empty-state">
                            <div className="empty-state-icon">⚠️</div>
                            <div className="empty-state-title">Error loading transactions</div>
                            <p>{error}</p>
                            <button className="btn btn-primary" style={{ marginTop: 'var(--spacing-md)' }} onClick={loadTransactions}>
                                Try Again
                            </button>
                        </div>
                    ) : filteredTransactions.length === 0 ? (
                        <div className="empty-state">
                            <div className="empty-state-icon">📝</div>
                            <div className="empty-state-title">No transactions found</div>
                            <p>Try adjusting your filters or add a new transaction.</p>
                        </div>
                    ) : (
                        <div className="transactions-list">
                            {filteredTransactions.map((tx) => (
                                <div key={tx.id || tx.transactionID} className="transaction-item">
                                    <div className="transaction-icon">
                                        {categoryConfig[tx.category]?.emoji || '💰'}
                                    </div>
                                    <div className="transaction-details">
                                        <div className="transaction-name">{tx.notes || tx.description || 'No description'}</div>
                                        <div className="transaction-meta">
                                            <span className={`badge badge-${tx.category?.toLowerCase() || 'other'}`}>
                                                {formatCategory(tx.category)}
                                            </span>
                                            <span className="text-muted">• {formatDate(tx.transactionDate || tx.date)}</span>
                                        </div>
                                    </div>
                                    <div className={`transaction-amount ${tx.type === 'INCOME' ? 'income' : 'expense'}`}>
                                        {tx.type === 'INCOME' ? (
                                            <ArrowUpCircle size={18} />
                                        ) : (
                                            <ArrowDownCircle size={18} />
                                        )}
                                        {tx.type === 'INCOME' ? '+' : '-'}${Math.abs(tx.amount).toLocaleString()}
                                    </div>
                                    <div className="transaction-actions">
                                        <button
                                            className="btn-icon"
                                            onClick={() => setEditingTransaction(tx)}
                                            title="Edit transaction"
                                        >
                                            <Edit2 size={16} />
                                        </button>
                                        <button
                                            className="btn-icon btn-icon-danger"
                                            onClick={() => setDeleteConfirm(tx)}
                                            title="Delete transaction"
                                        >
                                            <Trash2 size={16} />
                                        </button>
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}
                </div>

                {/* FAB */}
                <button className="fab" onClick={() => setShowAddModal(true)}>
                    <Plus size={24} />
                </button>

                {/* Add Transaction Modal */}
                {showAddModal && (
                    <AddTransactionModal onClose={() => setShowAddModal(false)} onAdd={loadTransactions} />
                )}

                {/* Edit Transaction Modal */}
                {editingTransaction && (
                    <EditTransactionModal
                        transaction={editingTransaction}
                        onClose={() => setEditingTransaction(null)}
                        onUpdate={loadTransactions}
                    />
                )}

                {/* Delete Confirmation Modal */}
                {deleteConfirm && (
                    <div className="modal-overlay" onClick={() => setDeleteConfirm(null)}>
                        <div className="modal modal-sm" onClick={(e) => e.stopPropagation()}>
                            <div className="modal-header">
                                <h2 className="modal-title">Delete Transaction</h2>
                                <button className="modal-close" onClick={() => setDeleteConfirm(null)}>×</button>
                            </div>
                            <div className="modal-body">
                                <p>Are you sure you want to delete this transaction?</p>
                                <div className="delete-preview">
                                    <strong>{deleteConfirm.notes || deleteConfirm.description || 'No description'}</strong>
                                    <span className={deleteConfirm.type === 'INCOME' ? 'income' : 'expense'}>
                                        {deleteConfirm.type === 'INCOME' ? '+' : '-'}${Math.abs(deleteConfirm.amount).toLocaleString()}
                                    </span>
                                </div>
                                <p className="text-muted" style={{ fontSize: '0.85rem', marginTop: 'var(--spacing-sm)' }}>
                                    This action cannot be undone.
                                </p>
                            </div>
                            <div className="modal-footer">
                                <button className="btn btn-secondary" onClick={() => setDeleteConfirm(null)}>Cancel</button>
                                <button className="btn btn-danger" onClick={() => handleDelete(deleteConfirm.transactionID || deleteConfirm.id)}>
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

function AddTransactionModal({ onClose, onAdd }) {
    const [formData, setFormData] = useState({
        description: '',
        amount: '',
        type: 'EXPENSE',
        category: expenseCategories[0], // Default to first expense category
        date: new Date().toISOString().split('T')[0],
    });
    const [loading, setLoading] = useState(false);

    // Get categories based on current type
    const currentCategories = formData.type === 'INCOME' ? incomeCategories : expenseCategories;

    // Handle type change - reset category to first option of new type
    const handleTypeChange = (newType) => {
        const newCategories = newType === 'INCOME' ? incomeCategories : expenseCategories;
        setFormData({
            ...formData,
            type: newType,
            category: newCategories[0],
        });
    };

    const handleSubmit = (e) => {
        e.preventDefault();

        // Close modal immediately
        onClose();

        // Fire API call in background (don't await)
        transactionAPI.create({
            amount: parseFloat(formData.amount),
            category: formData.category,
            type: formData.type,
            notes: formData.description,
            paymentType: 'CARD',
            recurring: false,
            frequency: 0,
        }).then(() => {
            console.log('Transaction created successfully');
            onAdd(); // Reload list
        }).catch((error) => {
            console.error('Failed to add transaction:', error);
        });

        // Reload after a delay to show new transaction
        setTimeout(() => onAdd(), 1000);
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
                                <select
                                    className="select"
                                    value={formData.type}
                                    onChange={(e) => handleTypeChange(e.target.value)}
                                    style={{ width: '100%' }}
                                >
                                    <option value="EXPENSE">Expense</option>
                                    <option value="INCOME">Income</option>
                                </select>
                            </div>
                            <div className="input-group">
                                <label>Category</label>
                                <select
                                    className="select"
                                    value={formData.category}
                                    onChange={(e) => setFormData({ ...formData, category: e.target.value })}
                                    style={{ width: '100%' }}
                                >
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
                        <button type="submit" className="btn btn-primary" disabled={loading}>
                            {loading ? 'Adding...' : 'Add Transaction'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}

function EditTransactionModal({ transaction, onClose, onUpdate }) {
    // Determine initial category - use existing or fallback to first of appropriate type
    const initialCategory = transaction.category ||
        (transaction.type === 'INCOME' ? incomeCategories[0] : expenseCategories[0]);

    const [formData, setFormData] = useState({
        description: transaction.notes || transaction.description || '',
        amount: transaction.amount || '',
        type: transaction.type || 'EXPENSE',
        category: initialCategory,
        paymentType: transaction.paymentType || 'CARD',
    });
    const [loading, setLoading] = useState(false);

    // Get categories based on current type
    const currentCategories = formData.type === 'INCOME' ? incomeCategories : expenseCategories;

    // Handle type change - reset category to first option of new type
    const handleTypeChange = (newType) => {
        const newCategories = newType === 'INCOME' ? incomeCategories : expenseCategories;
        setFormData({
            ...formData,
            type: newType,
            category: newCategories[0],
        });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);

        const transactionId = transaction.transactionID || transaction.id;

        // Close modal immediately for better UX
        onClose();

        // Fire API call in background
        transactionAPI.update(transactionId, {
            amount: parseFloat(formData.amount),
            category: formData.category,
            type: formData.type,
            notes: formData.description,
            paymentType: formData.paymentType,
            recurring: transaction.recurring || false,
            frequency: transaction.frequency || 0,
        }).then(() => {
            console.log('Transaction updated successfully');
            onUpdate();
        }).catch((error) => {
            console.error('Failed to update transaction:', error);
            alert('Failed to update transaction: ' + (error.message || error));
        });

        // Reload after a delay
        setTimeout(() => onUpdate(), 1000);
    };

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="modal" onClick={(e) => e.stopPropagation()}>
                <div className="modal-header">
                    <h2 className="modal-title">Edit Transaction</h2>
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
                                <select
                                    className="select"
                                    value={formData.type}
                                    onChange={(e) => handleTypeChange(e.target.value)}
                                    style={{ width: '100%' }}
                                >
                                    <option value="EXPENSE">Expense</option>
                                    <option value="INCOME">Income</option>
                                </select>
                            </div>
                            <div className="input-group">
                                <label>Category</label>
                                <select
                                    className="select"
                                    value={formData.category}
                                    onChange={(e) => setFormData({ ...formData, category: e.target.value })}
                                    style={{ width: '100%' }}
                                >
                                    {currentCategories.map((cat) => (
                                        <option key={cat} value={cat}>{formatCategory(cat)}</option>
                                    ))}
                                </select>
                            </div>
                        </div>
                    </div>
                    <div className="modal-footer">
                        <button type="button" className="btn btn-secondary" onClick={onClose}>Cancel</button>
                        <button type="submit" className="btn btn-primary" disabled={loading}>
                            {loading ? 'Saving...' : 'Save Changes'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}
