// API Client with JWT Authentication
const API_BASE_URL = 'http://localhost:8080';

// Get token from localStorage
const getToken = () => localStorage.getItem('token');
const getUsername = () => localStorage.getItem('username');

// Set auth data
export const setAuthData = (token, username) => {
    localStorage.setItem('token', token);
    localStorage.setItem('username', username);
};

// Clear auth data
export const clearAuthData = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
};

// Check if user is authenticated
export const isAuthenticated = () => {
    return !!getToken();
};

// API request wrapper with auth
const apiRequest = async (endpoint, options = {}) => {
    const token = getToken();
    const username = getUsername();

    const headers = {
        'Content-Type': 'application/json',
        ...options.headers,
    };

    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    if (username) {
        headers['X-Username'] = username;
    }

    // Add timeout to prevent hanging requests
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), 30000); // 30 second timeout

    try {
        const response = await fetch(`${API_BASE_URL}${endpoint}`, {
            ...options,
            headers,
            signal: controller.signal,
        });

        clearTimeout(timeoutId);

        // Handle unauthorized/forbidden - redirect to login
        if (response.status === 401 || response.status === 403) {
            clearAuthData();
            alert('Your session has expired. Please login again.');
            window.location.href = '/login';
            throw new Error('Session expired');
        }

        if (!response.ok) {
            const error = await response.json().catch(() => ({ message: 'An error occurred' }));
            throw new Error(error.message || 'An error occurred');
        }

        // Handle empty responses and non-JSON responses
        const text = await response.text();
        if (!text || text.trim() === '') {
            return null;
        }
        try {
            return JSON.parse(text);
        } catch {
            // If response is not JSON, return the text as-is
            return text;
        }
    } catch (error) {
        clearTimeout(timeoutId);
        if (error.name === 'AbortError') {
            throw new Error('Request timed out');
        }
        throw error;
    }
};

// Auth API
export const authAPI = {
    login: async (username, password) => {
        const response = await apiRequest('/login', {
            method: 'POST',
            body: JSON.stringify({ username, password }),
        });
        return response;
    },

    register: async (userData) => {
        const response = await apiRequest('/register', {
            method: 'POST',
            body: JSON.stringify(userData),
        });
        return response;
    },
};

// User API
export const userAPI = {
    getDetails: async () => {
        return apiRequest('/api/v1/users/getUserDetails');
    },

    updateDetails: async (userData) => {
        return apiRequest('/api/v1/users/updateUserDetails', {
            method: 'PUT',
            body: JSON.stringify(userData),
        });
    },

    updatePassword: async (password) => {
        const username = getUsername();
        return apiRequest('/api/v1/users/updatePassword', {
            method: 'PUT',
            body: JSON.stringify({ username, password }),
        });
    },
};

// Transaction API
export const transactionAPI = {
    getAll: async (page = 0, size = 10) => {
        return apiRequest(`/api/v1/transaction?page=${page}&size=${size}`);
    },

    getById: async (id) => {
        return apiRequest(`/api/v1/transaction/${id}`);
    },

    create: async (transaction) => {
        return apiRequest('/api/v1/transaction', {
            method: 'POST',
            body: JSON.stringify(transaction),
        });
    },

    update: async (id, transaction) => {
        return apiRequest(`/api/v1/transaction/${id}`, {
            method: 'PUT',
            body: JSON.stringify(transaction),
        });
    },

    delete: async (id) => {
        return apiRequest(`/api/v1/transaction/${id}`, {
            method: 'DELETE',
        });
    },

    getByCategory: async (category, page = 0, size = 10) => {
        return apiRequest(`/api/v1/transaction/category/${category}?page=${page}&size=${size}`);
    },

    getByDateRange: async (start, end) => {
        return apiRequest(`/api/v1/transaction/filter?start=${start}&end=${end}`);
    },

    getByMonth: async (month, year, page = 0, size = 10) => {
        return apiRequest(`/api/v1/transaction/month?month=${month}&year=${year}&page=${page}&size=${size}`);
    },

    getMonthlyTotal: async (month, year) => {
        return apiRequest(`/api/v1/transaction/month/total?month=${month}&year=${year}`);
    },

    getYearlyTotal: async (year) => {
        return apiRequest(`/api/v1/transaction/year/total?year=${year}`);
    },

    getSavings: async () => {
        const username = getUsername();
        return apiRequest(`/api/v1/savings/${username}`);
    },
};

// Analytics API
export const analyticsAPI = {
    getFinancialSummary: async () => {
        const username = getUsername();
        return apiRequest(`/api/v1/analytics/users/${username}/summary`);
    },
};

// Goal API
export const goalAPI = {
    getById: async (goalId) => {
        return apiRequest(`/api/v1/goal/${goalId}`);
    },

    create: async (goal) => {
        return apiRequest('/api/v1/goal', {
            method: 'POST',
            body: JSON.stringify(goal),
        });
    },

    update: async (goalId, goal) => {
        return apiRequest(`/api/v1/goal/${goalId}`, {
            method: 'PUT',
            body: JSON.stringify(goal),
        });
    },

    delete: async (goalId) => {
        return apiRequest(`/api/v1/goal/${goalId}`, {
            method: 'DELETE',
        });
    },

    refreshProgress: async () => {
        const username = getUsername();
        return apiRequest(`/api/v1/goal/update-progress/${username}`, {
            method: 'PUT',
        });
    },

    getSummary: async () => {
        const username = getUsername();
        return apiRequest(`/api/v1/goal/${username}/summary`);
    },

    getAll: async () => {
        const username = getUsername();
        return apiRequest(`/api/v1/goal/all/${username}`);
    },
};

// Budget API
export const budgetAPI = {
    getForMonth: async (month) => {
        const username = getUsername();
        const params = month ? `?month=${month}` : '';
        return apiRequest(`/api/v1/budget/${username}${params}`);
    },

    createDefaults: async () => {
        const username = getUsername();
        return apiRequest(`/api/v1/budget/default/${username}`, {
            method: 'POST',
        });
    },

    getRecommendations: async () => {
        const username = getUsername();
        return apiRequest(`/api/v1/budget/recommendations/${username}`);
    },
};

// Report API
export const reportAPI = {
    generate: async (type, startDate, endDate) => {
        return apiRequest(`/report?type=${type}&start=${startDate}&end=${endDate}`);
    },
};

export default {
    auth: authAPI,
    user: userAPI,
    transaction: transactionAPI,
    analytics: analyticsAPI,
    goal: goalAPI,
    budget: budgetAPI,
    report: reportAPI,
};
