/**
 * User notifications handler
 * Connects to WebSocket and displays real-time notifications for order status changes
 */

document.addEventListener('DOMContentLoaded', function() {
    // Check if user is logged in
    const userId = getUserId();
    if (userId) {
        // Connect to WebSocket
        connectWebSocket(userId);
    }
});

/**
 * Get current user ID from page
 * @returns {string|null} User ID or null if not found
 */
function getUserId() {
    // Try to get user ID from meta tag
    const userIdMeta = document.querySelector('meta[name="user-id"]');
    if (userIdMeta) {
        return userIdMeta.getAttribute('content');
    }
    
    // Try to get from data attribute
    const userIdElement = document.querySelector('[data-user-id]');
    if (userIdElement) {
        return userIdElement.dataset.userId;
    }
    
    return null;
}

/**
 * Connect to WebSocket and subscribe to user order notifications
 * @param {string} userId - User ID
 */
function connectWebSocket(userId) {
    const socket = new SockJS('/ws');
    const stompClient = Stomp.over(socket);
    
    stompClient.connect({}, function(frame) {
        console.log('Connected to WebSocket');
        
        // Subscribe to user-specific order notifications
        stompClient.subscribe(`/topic/user/${userId}/orders`, function(message) {
            const notification = JSON.parse(message.body);
            handleOrderStatusNotification(notification);
        });
    }, function(error) {
        console.error('WebSocket connection error:', error);
        // Try to reconnect after 5 seconds
        setTimeout(() => connectWebSocket(userId), 5000);
    });
}

/**
 * Handle order status change notification
 * @param {Object} notification - Order notification data
 */
function handleOrderStatusNotification(notification) {
    // Show toast notification
    showToastNotification(notification);
    
    // Play notification sound
    playNotificationSound();
    
    // Update order status if on order detail page
    if (window.location.pathname.includes(`/orders/${notification.id}`)) {
        updateOrderStatus(notification);
    }
    
    // Update orders list if on orders page
    if (window.location.pathname === '/orders') {
        updateOrdersList(notification);
    }
}

/**
 * Show toast notification for order status change
 * @param {Object} notification - Order notification data
 */
function showToastNotification(notification) {
    // Create toast container if it doesn't exist
    let toastContainer = document.getElementById('toast-container');
    if (!toastContainer) {
        toastContainer = document.createElement('div');
        toastContainer.id = 'toast-container';
        toastContainer.className = 'fixed bottom-4 right-4 z-50 flex flex-col space-y-4';
        document.body.appendChild(toastContainer);
    }
    
    // Create toast element
    const toast = document.createElement('div');
    toast.className = 'bg-white rounded-lg shadow-lg overflow-hidden max-w-md w-full transform transition-all duration-300 ease-in-out';
    toast.style.opacity = '0';
    toast.style.transform = 'translateY(100%)';
    
    // Get status color
    const statusColor = getStatusColor(notification.status);
    
    // Create toast content
    toast.innerHTML = `
        <div class="p-4 flex items-start">
            <div class="flex-shrink-0 ${statusColor.bg} rounded-full p-2">
                <svg class="w-6 h-6 ${statusColor.text}" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
                </svg>
            </div>
            <div class="ml-3 w-0 flex-1">
                <p class="text-sm font-medium text-gray-900">
                    ${notification.message}
                </p>
                <div class="mt-2 flex">
                    <a href="/orders/${notification.id}" class="inline-flex items-center px-3 py-1 border border-transparent text-sm leading-4 font-medium rounded-md text-blue-700 bg-blue-100 hover:bg-blue-200 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500">
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
        toast.style.transform = 'translateY(0)';
    }, 10);
    
    // Remove toast after 5 seconds
    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateY(100%)';
        setTimeout(() => {
            toast.remove();
        }, 300);
    }, 5000);
}

/**
 * Get color classes for order status
 * @param {string} status - Order status
 * @returns {Object} Object with bg and text color classes
 */
function getStatusColor(status) {
    switch (status) {
        case 'PENDING':
            return { bg: 'bg-yellow-100', text: 'text-yellow-600' };
        case 'PROCESSING':
            return { bg: 'bg-blue-100', text: 'text-blue-600' };
        case 'SHIPPED':
            return { bg: 'bg-indigo-100', text: 'text-indigo-600' };
        case 'DELIVERED':
            return { bg: 'bg-green-100', text: 'text-green-600' };
        case 'SUCCESS':
            return { bg: 'bg-green-100', text: 'text-green-600' };
        case 'CANCELLED':
        case 'CANCELED':
            return { bg: 'bg-red-100', text: 'text-red-600' };
        case 'FAILED':
            return { bg: 'bg-red-100', text: 'text-red-600' };
        default:
            return { bg: 'bg-gray-100', text: 'text-gray-600' };
    }
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
 * Update order status on order detail page
 * @param {Object} notification - Order notification data
 */
function updateOrderStatus(notification) {
    // Update status badge
    const statusBadge = document.querySelector('.order-status-badge');
    if (statusBadge) {
        // Get status color
        const statusColor = getStatusColor(notification.status);
        
        // Update badge
        statusBadge.className = `order-status-badge px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${statusColor.bg} ${statusColor.text}`;
        statusBadge.textContent = notification.status;
    }
    
    // Update status timeline if it exists
    const timelineItem = document.querySelector(`.timeline-item[data-status="${notification.status}"]`);
    if (timelineItem) {
        timelineItem.classList.add('active');
        
        // Update timestamp
        const timestamp = timelineItem.querySelector('.timeline-timestamp');
        if (timestamp) {
            const now = new Date();
            timestamp.textContent = now.toLocaleString('vi-VN');
        }
    }
}

/**
 * Update orders list on orders page
 * @param {Object} notification - Order notification data
 */
function updateOrdersList(notification) {
    // Find order row
    const orderRow = document.querySelector(`tr[data-order-id="${notification.id}"]`);
    if (!orderRow) return;
    
    // Get status color
    const statusColor = getStatusColor(notification.status);
    
    // Update status badge
    const statusBadge = orderRow.querySelector('.order-status-badge');
    if (statusBadge) {
        statusBadge.className = `order-status-badge px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${statusColor.bg} ${statusColor.text}`;
        statusBadge.textContent = notification.status;
    }
    
    // Highlight row briefly
    orderRow.className = 'bg-yellow-50 transition-colors duration-300';
    setTimeout(() => {
        orderRow.className = 'hover:bg-gray-50 transition-colors duration-300';
    }, 3000);
}