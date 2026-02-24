/* auth-login.js — Login page interactions */

// ── Toggle password visibility ──
document.getElementById('togglePassword').addEventListener('click', function () {
    const pw = document.getElementById('password');
    const icon = this.querySelector('i');
    const isHidden = pw.type === 'password';
    pw.type = isHidden ? 'text' : 'password';
    icon.classList.toggle('fa-eye', !isHidden);
    icon.classList.toggle('fa-eye-slash', isHidden);
});

// ── Loading state on submit ──
document.getElementById('loginForm').addEventListener('submit', function () {
    const btn = document.getElementById('submitBtn');
    btn.classList.add('loading');
    btn.disabled = true;
});

// ── Cookie theft detection ──
(function () {
    const getCookie = function (name) {
        const value = '; ' + document.cookie;
        const parts = value.split('; ' + name + '=');
        return parts.length === 2 ? parts.pop().split(';').shift() : null;
    };
    const err = getCookie('rm_err');
    const url = new URL(window.location.href);
    if (err === 'theft' && url.searchParams.get('error') !== 'cookieTheft') {
        url.searchParams.set('error', 'cookieTheft');
        document.cookie = 'rm_err=; Max-Age=0; path=/';
        window.location.replace(url.toString());
    }
})();
