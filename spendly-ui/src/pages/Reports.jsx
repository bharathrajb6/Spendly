import { useState, useEffect } from 'react';
import { Download, Calendar } from 'lucide-react';
import { Chart as ChartJS, ArcElement, Tooltip, Legend, CategoryScale, LinearScale, BarElement, PointElement, LineElement } from 'chart.js';
import { Bar, Pie, Line } from 'react-chartjs-2';
import Header from '../components/layout/Header';
import { reportAPI, transactionAPI, analyticsAPI, goalAPI } from '../api/api';
import './Reports.css';

// Register ChartJS components
ChartJS.register(ArcElement, Tooltip, Legend, CategoryScale, LinearScale, BarElement, PointElement, LineElement);

export default function Reports() {
    const [dateRange, setDateRange] = useState('Last 6 Months');
    const [exporting, setExporting] = useState(false);
    const [loading, setLoading] = useState(true);
    const [transactions, setTransactions] = useState([]);
    const [summary, setSummary] = useState(null);
    const [goalSummary, setGoalSummary] = useState(null);

    useEffect(() => {
        loadReportData();
    }, []);

    const loadReportData = async () => {
        try {
            const [txData, summaryData, goalsData] = await Promise.all([
                transactionAPI.getAll(0, 100).catch(() => ({ content: [] })),
                analyticsAPI.getFinancialSummary().catch(() => null),
                goalAPI.getSummary().catch(() => null),
            ]);
            setTransactions(txData?.content || txData || []);
            setSummary(summaryData);
            setGoalSummary(goalsData);
        } catch (error) {
            console.error('Failed to load report data:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleExport = async (type) => {
        setExporting(true);
        try {
            const endDate = new Date().toISOString().split('T')[0];
            const startDate = new Date(Date.now() - 180 * 24 * 60 * 60 * 1000).toISOString().split('T')[0];
            await reportAPI.generate(type, startDate, endDate);
            alert(`${type.toUpperCase()} export initiated. Check your email for the download link.`);
        } catch (error) {
            console.error('Export failed:', error);
            alert('Export failed. Please try again.');
        } finally {
            setExporting(false);
        }
    };

    // Compute income vs expense from real data
    const totalIncome = summary?.totalIncome ?? 0;
    const totalExpense = summary?.totalExpense ?? 0;

    const incomeExpenseData = {
        labels: ['Total'],
        datasets: [
            {
                label: 'Expense',
                data: [totalExpense],
                backgroundColor: '#ef4444',
                borderRadius: 4,
            },
            {
                label: 'Income',
                data: [totalIncome],
                backgroundColor: '#10b981',
                borderRadius: 4,
            }
        ]
    };

    // Compute spending by category from real transactions
    const computeCategoryData = () => {
        const categories = ['Food', 'Transport', 'Shopping', 'Bills', 'Other'];
        const colors = ['#ef4444', '#10b981', '#8b5cf6', '#f59e0b', '#3b82f6'];

        const spending = categories.map(cat => {
            return transactions
                .filter(tx => tx.type === 'EXPENSE' && tx.category === cat)
                .reduce((sum, tx) => sum + (tx.amount || 0), 0);
        });

        const labels = categories.map((cat, i) => `${cat}: $${spending[i].toLocaleString()}`);

        return {
            labels,
            datasets: [{
                data: spending,
                backgroundColor: colors,
                borderWidth: 0,
            }]
        };
    };

    const categoryData = computeCategoryData();

    // Savings data from goal summary
    const totalSaved = goalSummary?.totalSaved ?? summary?.remainingBalance ?? 0;
    const savingsData = {
        labels: ['Current'],
        datasets: [{
            label: 'Total Savings',
            data: [totalSaved],
            borderColor: '#3b82f6',
            backgroundColor: 'rgba(59, 130, 246, 0.1)',
            tension: 0.4,
            fill: true,
            pointBackgroundColor: '#3b82f6',
            pointBorderColor: '#fff',
            pointBorderWidth: 2,
            pointRadius: 6,
        }]
    };

    const barOptions = {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
            legend: {
                position: 'bottom',
                labels: { usePointStyle: true, padding: 20 }
            }
        },
        scales: {
            x: { grid: { display: false } },
            y: { grid: { color: '#f0f0f0' }, beginAtZero: true }
        }
    };

    const pieOptions = {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
            legend: {
                position: 'right',
                labels: { usePointStyle: true, padding: 15 }
            }
        }
    };

    const lineOptions = {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
            legend: { display: false }
        },
        scales: {
            x: { grid: { display: false } },
            y: { grid: { color: '#f0f0f0' }, beginAtZero: true }
        }
    };

    if (loading) {
        return (
            <>
                <Header title="Reports" />
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
            <Header title="Reports" />
            <div className="page-content">
                <div className="reports-header">
                    <div>
                        <h1>Reports & Insights</h1>
                        <p className="text-secondary">Analyze your financial performance</p>
                    </div>
                    <div className="reports-actions">
                        <button
                            className="btn btn-secondary"
                            onClick={() => handleExport('csv')}
                            disabled={exporting}
                        >
                            <Download size={18} />
                            Export CSV
                        </button>
                        <button
                            className="btn btn-secondary"
                            onClick={() => handleExport('pdf')}
                            disabled={exporting}
                        >
                            <Download size={18} />
                            Export PDF
                        </button>
                    </div>
                </div>

                {/* Date Range Selector */}
                <div className="card date-range-card">
                    <Calendar size={20} />
                    <select
                        className="select"
                        value={dateRange}
                        onChange={(e) => setDateRange(e.target.value)}
                    >
                        <option>Last 6 Months</option>
                        <option>Last 3 Months</option>
                        <option>This Year</option>
                        <option>Last Year</option>
                    </select>
                </div>

                {/* Income vs Expense Chart */}
                <div className="card" style={{ marginTop: 'var(--spacing-lg)' }}>
                    <div className="card-header">
                        <h3 className="card-title">Income vs Expense</h3>
                        <span className="text-sm text-secondary">
                            Balance: ${(totalIncome - totalExpense).toLocaleString()}
                        </span>
                    </div>
                    <div className="chart-container" style={{ height: '300px' }}>
                        <Bar data={incomeExpenseData} options={barOptions} />
                    </div>
                </div>

                {/* Two column charts */}
                <div className="grid-2" style={{ marginTop: 'var(--spacing-lg)' }}>
                    {/* Spending by Category */}
                    <div className="card">
                        <div className="card-header">
                            <h3 className="card-title">Spending by Category</h3>
                        </div>
                        <div className="chart-container" style={{ height: '250px' }}>
                            <Pie data={categoryData} options={pieOptions} />
                        </div>
                    </div>

                    {/* Total Savings */}
                    <div className="card">
                        <div className="card-header">
                            <h3 className="card-title">Total Savings</h3>
                        </div>
                        <div className="chart-container" style={{ height: '250px' }}>
                            <Line data={savingsData} options={lineOptions} />
                        </div>
                    </div>
                </div>
            </div>
        </>
    );
}
