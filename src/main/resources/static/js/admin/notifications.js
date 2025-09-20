/**
 * Admin notifications handler
 * Connects to WebSocket and displays real-time notifications for new orders
 */

document.addEventListener('DOMContentLoaded', function() {
    // Connect to WebSocket
    connectWebSocket();
    
    // Initialize notification counter
    initNotificationCounter();
});

/**
 * Connect to WebSocket and subscribe to admin order notifications
 */
function connectWebSocket() {
    const socket = new SockJS('/ws');
    const stompClient = Stomp.over(socket);
    
    stompClient.connect({}, function(frame) {
        console.log('Connected to WebSocket');
        
        // Subscribe to admin order notifications
        stompClient.subscribe('/topic/admin/orders', function(message) {
            const notification = JSON.parse(message.body);
            handleNewOrderNotification(notification);
        });
    }, function(error) {
        console.error('WebSocket connection error:', error);
        // Try to reconnect after 5 seconds
        setTimeout(connectWebSocket, 5000);
    });
}

/**
 * Initialize notification counter in the sidebar
 */
function initNotificationCounter() {
    // Check if notification counter already exists
    if (!document.getElementById('notification-counter')) {
        const ordersLink = document.querySelector('a[href="/admin/orders"]');
        if (ordersLink) {
            // Create notification counter
            const counter = document.createElement('span');
            counter.id = 'notification-counter';
            counter.className = 'inline-flex items-center justify-center px-2 py-1 text-xs font-bold leading-none text-white bg-red-600 rounded-full ml-2';
            counter.style.display = 'none';
            counter.textContent = '0';
            
            // Add counter to orders link
            ordersLink.appendChild(counter);
        }
    }
}

/**
 * Handle new order notification
 * @param {Object} notification - Order notification data
 */
function handleNewOrderNotification(notification) {
    // Update notification counter
    updateNotificationCounter();
    
    // Show toast notification
    showToastNotification(notification);
    
    // Play notification sound
    playNotificationSound();
    
    // Update orders list if on orders page
    if (window.location.pathname.includes('/admin/orders')) {
        updateOrdersList(notification);
    }
    
    // Update dashboard if on dashboard page
    if (window.location.pathname.includes('/admin/dashboard')) {
        updateDashboard();
    }
}

/**
 * Update notification counter in sidebar
 */
function updateNotificationCounter() {
    const counter = document.getElementById('notification-counter');
    if (counter) {
        const currentCount = parseInt(counter.textContent);
        counter.textContent = (currentCount + 1).toString();
        counter.style.display = 'inline-flex';
    }
}

/**
 * Show toast notification for new order
 * @param {Object} notification - Order notification data
 */
function showToastNotification(notification) {
    // Create toast container if it doesn't exist
    let toastContainer = document.getElementById('toast-container');
    if (!toastContainer) {
        toastContainer = document.createElement('div');
        toastContainer.id = 'toast-container';
        toastContainer.className = 'fixed top-4 right-4 z-50 flex flex-col space-y-4';
        document.body.appendChild(toastContainer);
    }
    
    // Create toast element
    const toast = document.createElement('div');
    toast.className = 'bg-white rounded-lg shadow-lg overflow-hidden max-w-md w-full transform transition-all duration-300 ease-in-out';
    toast.style.opacity = '0';
    toast.style.transform = 'translateX(100%)';
    
    // Format currency
    const formatter = new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
    });
    
    // Create toast content
    toast.innerHTML = `
        <div class="p-4 flex items-start">
            <div class="flex-shrink-0 bg-blue-100 rounded-full p-2">
                <svg class="w-6 h-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z"></path>
                </svg>
            </div>
            <div class="ml-3 w-0 flex-1">
                <p class="text-sm font-medium text-gray-900">
                    ${notification.message}
                </p>
                <p class="mt-1 text-sm text-gray-500">
                    ${notification.username} - ${formatter.format(notification.totalAmount)}
                </p>
                <div class="mt-2 flex">
                    <a href="/admin/orders/${notification.id}" class="inline-flex items-center px-3 py-1 border border-transparent text-sm leading-4 font-medium rounded-md text-blue-700 bg-blue-100 hover:bg-blue-200 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500">
                        View Order
                    </a>
                </div>
            </div>
            <div class="ml-4 flex-shrink-0 flex">
                <button class="bg-white rounded-md inline-flex text-gray-400 hover:text-gray-500 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500" onclick="this.parentElement.parentElement.parentElement.remove()">
                    <span class="sr-only">Close</span>
                    <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                    </svg>
                </button>
            </div>
        </div>
    `;
    
    // Add toast to container
    toastContainer.appendChild(toast);
    
    // Animate toast in
    setTimeout(() => {
        toast.style.opacity = '1';
        toast.style.transform = 'translateX(0)';
    }, 10);
    
    // Remove toast after 5 seconds
    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(100%)';
        setTimeout(() => {
            toast.remove();
        }, 300);
    }, 5000);
}

/**
 * Play notification sound
 */
function playNotificationSound() {
    const audio = new Audio('/sounds/notification.mp3');
    audio.play().catch(error => {
        console.error('Failed to play notification sound:', error);
    });
}

/**
 * Update orders list on admin orders page
 * @param {Object} notification - Order notification data
 */
function updateOrdersList(notification) {
    // Check if orders table exists
    const ordersTable = document.querySelector('table tbody');
    if (!ordersTable) return;
    
    // Create new row for order
    const newRow = document.createElement('tr');
    newRow.className = 'bg-yellow-50 hover:bg-yellow-100 transition-colors duration-300';
    
    // Format date
    const orderDate = new Date(notification.orderDate);
    const formattedDate = orderDate.toLocaleDateString('vi-VN');
    
    // Format currency
    const formatter = new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
    });
    
    // Set row content (adjust based on your actual table structure)
    newRow.innerHTML = `
        <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">${notification.id}</td>
        <td class="px-6 py-4 whitespace-nowrap">
            <div class="flex items-center">
                <div class="flex-shrink-0 h-10 w-10">
                    <img class="h-10 w-10 rounded-full" src="data:image/jpeg;base64,${notification.userAvatar}" alt="${notification.username}">
                </div>
                <div class="ml-4">
                    <div class="text-sm font-medium text-gray-900">${notification.username}</div>
                </div>
            </div>
        </td>
        <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">${formattedDate}</td>
        <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">${formatter.format(notification.totalAmount)}</td>
        <td class="px-6 py-4 whitespace-nowrap">
            <span class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-yellow-100 text-yellow-800">
                ${notification.status}
            </span>
        </td>
        <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
            <a href="/admin/orders/${notification.id}" class="text-blue-600 hover:text-blue-900">View</a>
        </td>
    `;
    
    // Add new row to top of table
    ordersTable.insertBefore(newRow, ordersTable.firstChild);
    
    // Highlight row briefly
    setTimeout(() => {
        newRow.className = 'hover:bg-gray-50 transition-colors duration-300';
    }, 5000);
}

/**
 * Update dashboard statistics
 */
function updateDashboard() {
    // Refresh order count and other statistics
    fetch('/admin/api/dashboard/stats')
        .then(response => response.json())
        .then(data => {
            // Update order count
            const orderCountElement = document.querySelector('[data-stat="order-count"]');
            if (orderCountElement) {
                orderCountElement.textContent = data.totalOrders;
            }
            
            // Update revenue
            const revenueElement = document.querySelector('[data-stat="revenue"]');
            if (revenueElement) {
                revenueElement.textContent = data.revenue;
            }
            
            // Update charts if they exist
            if (window.updateCharts) {
                window.updateCharts();
            }
        })
        .catch(error => {
            console.error('Failed to update dashboard stats:', error);
        });
}