document.addEventListener('DOMContentLoaded', () => {
    // Create particles dynamically (optional enhancement)
    const particlesContainer = document.querySelector('.particles-container');
    if (particlesContainer) {
        for (let i = 0; i < 50; i++) {
            const particle = document.createElement('div');
            particle.className = 'particle';

            // Random styles
            particle.style.width = Math.random() * 5 + 'px';
            particle.style.height = particle.style.width;
            particle.style.background = `rgba(${Math.floor(Math.random() * 100) + 100}, ${Math.floor(Math.random() * 100) + 100}, 255, ${Math.random() * 0.3})`;
            particle.style.position = 'absolute';
            particle.style.borderRadius = '50%';
            particle.style.top = Math.random() * 100 + '%';
            particle.style.left = Math.random() * 100 + '%';

            // Random animation
            particle.style.animation = `particle-move ${Math.random() * 10 + 5}s ease-in-out infinite`;
            particle.style.animationDelay = `${Math.random() * 5}s`;

            particlesContainer.appendChild(particle);
        }
    }
});