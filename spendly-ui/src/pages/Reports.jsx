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

    // Compute income vs expense from real data
    const totalIncome = summary?.totalIncome ?? 0;
    const totalExpense = summary?.totalExpense ?? 0;

    const handleExport = async (type) => {
        setExporting(true);
        try {
            if (type === 'csv') {
                exportToCSV();
            } else if (type === 'pdf') {
                exportToPDF();
            }
        } catch (error) {
            console.error('Export failed:', error);
            alert('Export failed. Please try again.');
        } finally {
            setExporting(false);
        }
    };

    const exportToCSV = () => {
        if (!transactions || transactions.length === 0) {
            alert('No transactions to export');
            return;
        }

        // CSV headers
        const headers = ['Date', 'Description', 'Category', 'Type', 'Amount'];

        // CSV rows
        const rows = transactions.map(tx => [
            tx.transactionDate ? new Date(tx.transactionDate).toLocaleDateString('en-IN') : 'N/A',
            tx.description || 'N/A',
            tx.category || 'N/A',
            tx.transactionType || tx.type || 'N/A',
            tx.amount || 0
        ]);

        // Add summary row
        rows.push([]);
        rows.push(['Summary']);
        rows.push(['Total Income', '', '', '', totalIncome]);
        rows.push(['Total Expense', '', '', '', totalExpense]);
        rows.push(['Net Balance', '', '', '', totalIncome - totalExpense]);

        // Convert to CSV string
        const csvContent = [headers, ...rows]
            .map(row => row.map(cell => `"${cell}"`).join(','))
            .join('\n');

        // Download
        const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
        const link = document.createElement('a');
        link.href = URL.createObjectURL(blob);
        link.download = `Spendly_Report_${new Date().toISOString().split('T')[0]}.csv`;
        link.click();
        URL.revokeObjectURL(link.href);
    };

    const exportToPDF = () => {
        if (!transactions || transactions.length === 0) {
            alert('No transactions to export');
            return;
        }

        // Create a printable HTML document
        const printWindow = window.open('', '_blank');

        const transactionRows = transactions.map(tx => `
            <tr>
                <td>${tx.transactionDate ? new Date(tx.transactionDate).toLocaleDateString('en-IN') : 'N/A'}</td>
                <td>${tx.description || 'N/A'}</td>
                <td>${tx.category || 'N/A'}</td>
                <td style="color: ${(tx.transactionType || tx.type) === 'INCOME' ? '#10b981' : '#ef4444'}">
                    ${tx.transactionType || tx.type || 'N/A'}
                </td>
                <td style="text-align: right; font-weight: 500;">₹${(tx.amount || 0).toLocaleString()}</td>
            </tr>
        `).join('');

        const htmlContent = `
            <!DOCTYPE html>
            <html>
            <head>
                <title>Spendly Financial Report</title>
                <style>
                    * { margin: 0; padding: 0; box-sizing: border-box; }
                    body { font-family: 'Segoe UI', Arial, sans-serif; padding: 40px; color: #1a1a2e; }
                    .header { text-align: center; margin-bottom: 30px; padding-bottom: 20px; border-bottom: 2px solid #6366f1; }
                    .header h1 { color: #6366f1; font-size: 28px; margin-bottom: 5px; }
                    .header p { color: #666; font-size: 14px; }
                    .summary { display: flex; justify-content: space-around; margin-bottom: 30px; padding: 20px; background: #f8f9fa; border-radius: 8px; }
                    .summary-item { text-align: center; }
                    .summary-item .label { font-size: 12px; color: #666; text-transform: uppercase; }
                    .summary-item .value { font-size: 24px; font-weight: 700; margin-top: 5px; }
                    .summary-item .income { color: #10b981; }
                    .summary-item .expense { color: #ef4444; }
                    .summary-item .balance { color: #6366f1; }
                    table { width: 100%; border-collapse: collapse; margin-top: 20px; }
                    th { background: #6366f1; color: white; padding: 12px; text-align: left; font-size: 12px; text-transform: uppercase; }
                    td { padding: 10px 12px; border-bottom: 1px solid #eee; font-size: 13px; }
                    tr:hover { background: #f8f9fa; }
                    .footer { margin-top: 40px; text-align: center; font-size: 12px; color: #999; }
                    @media print { body { padding: 20px; } }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>Spendly Financial Report</h1>
                    <p>Generated on ${new Date().toLocaleDateString('en-IN', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })}</p>
                </div>
                
                <div class="summary">
                    <div class="summary-item">
                        <div class="label">Total Income</div>
                        <div class="value income">₹${totalIncome.toLocaleString()}</div>
                    </div>
                    <div class="summary-item">
                        <div class="label">Total Expense</div>
                        <div class="value expense">₹${totalExpense.toLocaleString()}</div>
                    </div>
                    <div class="summary-item">
                        <div class="label">Net Balance</div>
                        <div class="value balance">₹${(totalIncome - totalExpense).toLocaleString()}</div>
                    </div>
                </div>

                <h3 style="margin-bottom: 15px; color: #333;">Transaction Details</h3>
                <table>
                    <thead>
                        <tr>
                            <th>Date</th>
                            <th>Description</th>
                            <th>Category</th>
                            <th>Type</th>
                            <th style="text-align: right;">Amount</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${transactionRows}
                    </tbody>
                </table>

                <div class="footer">
                    <p>This report was generated by Spendly - Your Personal Finance Manager</p>
                </div>

                <script>
                    window.onload = function() { window.print(); }
                </script>
            </body>
            </html>
        `;

        printWindow.document.write(htmlContent);
        printWindow.document.close();
    };

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

        const labels = categories.map((cat, i) => `${cat}: ₹${spending[i].toLocaleString()}`);

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
                            Balance: ₹{(totalIncome - totalExpense).toLocaleString()}
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
