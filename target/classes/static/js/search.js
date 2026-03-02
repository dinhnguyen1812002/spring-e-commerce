async function searchProducts() {
    const query = document.getElementById('searchInput').value;
    const searchResults = document.getElementById('searchResults');

    if (query.length < 2) {
        searchResults.classList.add('hidden');
        return;
    }

    try {
        const response = await fetch(`/products/find?query=${query}`); // Updated path to include /products scope
        const products = await response.json();

        searchResults.innerHTML = ''; // Clear previous results
        if (products.length > 0) {
            products.forEach(product => {
                const item = document.createElement('a');
                item.href = `/products/${product.id}`;
                item.classList.add('flex', 'items-center', 'p-4', 'hover:bg-gray-50', 'transition-colors', 'border-b', 'border-gray-100', 'last:border-0');

                const formattedPrice = new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(product.price);
                const stars = renderStars(product.averageRating);

                item.innerHTML = `
                    <div class="flex-shrink-0 w-16 h-16 bg-gray-100 rounded-lg overflow-hidden mr-4">
                        <img src="/${product.image}" alt="${product.name}" class="w-full h-full object-cover"/>
                    </div>
                    <div class="flex-grow min-w-0">
                        <div class="flex justify-between items-start mb-1">
                            <h4 class="text-sm font-semibold text-gray-900 truncate pr-4">${product.name}</h4>
                            <span class="text-sm font-bold text-blue-600 whitespace-nowrap">${formattedPrice}</span>
                        </div>
                        <div class="flex items-center gap-4">
                            <div class="flex items-center">
                                ${stars}
                                <span class="text-xs text-gray-500 ml-1">(${product.averageRating})</span>
                            </div>
                            <span class="text-xs ${product.stock > 10 ? 'text-green-600' : 'text-orange-600'}">
                                <i class="fas fa-box-open mr-1"></i>${product.stock} in stock
                            </span>
                        </div>
                    </div>
                `;
                searchResults.appendChild(item);
            });
            searchResults.classList.remove('hidden');
        } else {
            searchResults.innerHTML = '<div class="p-6 text-center text-gray-500 text-sm">No products found</div>';
            searchResults.classList.remove('hidden');
        }
    } catch (error) {
        console.error('Search error:', error);
    }
}

function renderStars(rating) {
    let starsHtml = '<div class="flex text-yellow-400 text-[10px]">';
    for (let i = 1; i <= 5; i++) {
        if (i <= Math.floor(rating)) {
            starsHtml += '<i class="fas fa-star"></i>';
        } else if (i - 0.5 <= rating) {
            starsHtml += '<i class="fas fa-star-half-alt"></i>';
        } else {
            starsHtml += '<i class="far fa-star text-gray-300"></i>';
        }
    }
    starsHtml += '</div>';
    return starsHtml;
}

// Hide search results when clicking outside
document.addEventListener('click', (event) => {
    const searchResults = document.getElementById('searchResults');
    const searchInput = document.getElementById('searchInput');
    if (searchResults && searchInput && !searchInput.contains(event.target) && !searchResults.contains(event.target)) {
        searchResults.classList.add('hidden');
    }
});