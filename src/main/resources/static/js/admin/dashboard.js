/**
 * Admin Dashboard JavaScript
 * Handles chart rendering and real-time updates
 */

document.addEventListener('DOMContentLoaded', () => {
    const revenueChartCtx = document.getElementById('revenueChart')?.getContext('2d');
    const salesChartCtx = document.getElementById('salesChart')?.getContext('2d');

    if (!revenueChartCtx || !salesChartCtx) {
        console.error('Chart canvas elements not found');
        return;
    }

    let revenueChart, salesChart;

    const chartColors = {
        revenue: {
            backgroundColor: 'rgba(59, 130, 246, 0.1)',
            borderColor: 'rgba(59, 130, 246, 1)',
            pointBackgroundColor: 'rgba(59, 130, 246, 1)',
            pointBorderColor: '#fff',
        },
        sales: {
            backgroundColor: 'rgba(16, 185, 129, 0.1)',
            borderColor: 'rgba(16, 185, 129, 1)',
            pointBackgroundColor: 'rgba(16, 185, 129, 1)',
            pointBorderColor: '#fff',
        }
    };

    /**
     * Create a new chart instance
     */
    const createChart = (ctx, chartType, labels, data, label) => {
        return new Chart(ctx, {
            type: 'line',
            data: {
                labels: labels,
                datasets: [{
                    label: label,
                    data: data,
                    backgroundColor: chartColors[chartType].backgroundColor,
                    borderColor: chartColors[chartType].borderColor,
                    borderWidth: 2,
                    tension: 0.4,
                    fill: true,
                    pointRadius: 4,
                    pointHoverRadius: 6,
                    pointBackgroundColor: chartColors[chartType].pointBackgroundColor,
                    pointBorderColor: chartColors[chartType].pointBorderColor,
                    pointBorderWidth: 2,
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                interaction: {
                    mode: 'index',
                    intersect: false,
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            callback: function(value) {
                                if (chartType === 'revenue') {
                                    return new Intl.NumberFormat('vi-VN', {
                                        style: 'currency',
                                        currency: 'VND',
                                        notation: 'compact',
                                        compactDisplay: 'short'
                                    }).format(value);
                                }
                                return value;
                            }
                        },
                        grid: {
                            display: true,
                            drawBorder: false,
                        }
                    },
                    x: {
                        grid: {
                            display: false,
                        }
                    }
                },
                plugins: {
                    legend: {
                        display: true,
                        position: 'top',
                        labels: {
                            usePointStyle: true,
                            padding: 15,
                            font: {
                                size: 12,
                                weight: '500'
                            }
                        }
                    },
                    tooltip: {
                        backgroundColor: 'rgba(0, 0, 0, 0.8)',
                        padding: 12,
                        titleFont: {
                            size: 13,
                            weight: 'bold'
                        },
                        bodyFont: {
                            size: 12
                        },
                        callbacks: {
                            label: function(context) {
                                let label = context.dataset.label || '';
                                if (label) {
                                    label += ': ';
                                }
                                if (chartType === 'revenue') {
                                    label += new Intl.NumberFormat('vi-VN', {
                                        style: 'currency',
                                        currency: 'VND'
                                    }).format(context.parsed.y);
                                } else {
                                    label += context.parsed.y + ' orders';
                                }
                                return label;
                            }
                        }
                    }
                }
            }
        });
    };

    /**
     * Fetch data from API endpoint
     */
    const fetchData = async (endpoint) => {
        try {
            const response = await fetch(endpoint);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return await response.json();
        } catch (error) {
            console.error(`Could not fetch data from ${endpoint}:`, error);
            // Return dummy data for development
            return generateDummyData(endpoint);
        }
    };

    /**
     * Generate dummy data if API fails (for development)
     */
    const generateDummyData = (endpoint) => {
        if (endpoint.includes('revenue')) {
            return {
                labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'],
                data: [12000000, 19000000, 15000000, 25000000, 22000000, 30000000, 28000000, 32000000, 27000000, 35000000, 40000000, 45000000]
            };
        } else if (endpoint.includes('sales')) {
            return {
                labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'],
                data: [45, 59, 48, 81, 76, 95, 88, 102, 87, 115, 134, 142]
            };
        }
        return { labels: [], data: [] };
    };

    /**
     * Update existing chart with new data
     */
    const updateChart = (chart, labels, data) => {
        if (chart) {
            chart.data.labels = labels;
            chart.data.datasets[0].data = data;
            chart.update('active');
        }
    };

    /**
     * Load chart data for specific period
     */
    const loadChartData = async (chartType, period) => {
        const data = await fetchData(`/api/admin/dashboard/${chartType}?period=${period}`);
        if (data && data.labels && data.data) {
            if (chartType === 'revenue') {
                if (revenueChart) {
                    updateChart(revenueChart, data.labels, data.data);
                } else {
                    revenueChart = createChart(revenueChartCtx, 'revenue', data.labels, data.data, 'Revenue (VND)');
                }
            } else if (chartType === 'sales') {
                if (salesChart) {
                    updateChart(salesChart, data.labels, data.data);
                } else {
                    salesChart = createChart(salesChartCtx, 'sales', data.labels, data.data, 'Sales (Orders)');
                }
            }
        }
    };

    /**
     * Setup event listeners for period buttons
     */
    const setupEventListeners = () => {
        document.querySelectorAll('button[data-chart]').forEach(button => {
            button.addEventListener('click', () => {
                const chartType = button.dataset.chart;
                const period = button.dataset.period;

                // Update active button style
                document.querySelectorAll(`button[data-chart="${chartType}"]`).forEach(btn => {
                    btn.classList.remove('bg-blue-100', 'text-blue-700');
                    btn.classList.add('hover:bg-gray-100');
                });
                button.classList.add('bg-blue-100', 'text-blue-700');
                button.classList.remove('hover:bg-gray-100');

                // Load new data
                loadChartData(chartType, period);
            });
        });
    };

    /**
     * Load summary statistics
     */
    const loadSummaryData = async () => {
        const summary = await fetchData('/api/admin/dashboard/summary');
        if (summary) {
            const revenueEl = document.querySelector('[data-stat="revenue"]');
            if (revenueEl && summary.totalRevenue !== undefined) {
                revenueEl.textContent = new Intl.NumberFormat('vi-VN', {
                    style: 'currency',
                    currency: 'VND',
                    notation: 'compact',
                    compactDisplay: 'short'
                }).format(summary.totalRevenue);
            }
        }
    };

    /**
     * Initialize dashboard
     */
    const initDashboard = () => {
        // Load initial chart data (monthly by default)
        loadChartData('revenue', 'month');
        loadChartData('sales', 'month');
        
        // Load summary statistics
        loadSummaryData();
        
        // Setup button event listeners
        setupEventListeners();
        
        console.log('Dashboard initialized successfully');
    };

    // Start the dashboard
    initDashboard();

    // Make updateCharts available globally for notifications.js
    window.updateCharts = () => {
        const activeRevenueBtn = document.querySelector('button[data-chart="revenue"].bg-blue-100');
        const activeSalesBtn = document.querySelector('button[data-chart="sales"].bg-blue-100');
        
        const revenuePeriod = activeRevenueBtn ? activeRevenueBtn.dataset.period : 'month';
        const salesPeriod = activeSalesBtn ? activeSalesBtn.dataset.period : 'month';
        
        loadChartData('revenue', revenuePeriod);
        loadChartData('sales', salesPeriod);
        loadSummaryData();
    };
});
