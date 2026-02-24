/* auth-register.js — Register page interactions */

// ── Toggle password visibility ──
document.getElementById('togglePassword').addEventListener('click', function () {
    const pw = document.getElementById('password');
    const icon = this.querySelector('i');
    const isHidden = pw.type === 'password';
    pw.type = isHidden ? 'text' : 'password';
    icon.classList.toggle('fa-eye', !isHidden);
    icon.classList.toggle('fa-eye-slash', isHidden);
});

// ── Password strength meter ──
document.getElementById('password').addEventListener('input', function () {
    const val = this.value;
    const fill = document.getElementById('strengthFill');
    const text = document.getElementById('strengthText');

    let score = 0;
    if (val.length >= 6) score++;
    if (val.length >= 10 && /[A-Z]/.test(val) && /[0-9]/.test(val)) score++;
    if (val.length >= 12 && /[^A-Za-z0-9]/.test(val)) score++;
    if (val.length >= 14) score++;

    const widths = ['0%', '25%', '50%', '75%', '100%'];
    const bgColors = ['transparent', '#EF4444', '#F59E0B', '#10B981', '#059669'];
    const labels = ['—', 'Weak', 'Fair', 'Good', 'Strong'];
    const txColors = ['#9CA3AF', '#EF4444', '#F59E0B', '#10B981', '#059669'];

    fill.style.width = widths[score];
    fill.style.backgroundColor = bgColors[score];
    text.textContent = labels[score];
    text.style.color = txColors[score];
});
