document.addEventListener('DOMContentLoaded', function () {
    // Auto-dismiss alerts after 6 seconds
    const alerts = document.querySelectorAll('.alert-dismissible');
    alerts.forEach(function (alert) {
        setTimeout(function () {
            try {
                const bsAlert = new bootstrap.Alert(alert);
                bsAlert.close();
            } catch (e) {
                alert.style.display = 'none';
            }
        }, 6000);
    });

    // Form confirmation prompts
    const confirmForms = document.querySelectorAll('.confirm-action');
    confirmForms.forEach(function (form) {
        form.addEventListener('submit', function (e) {
            const message = form.getAttribute('data-confirm-message') || 'Are you sure you want to perform this action?';
            if (!confirm(message)) {
                e.preventDefault();
            }
        });
    });
});

// Helper function to fill demo credentials on login page
function fillLogin(email, password) {
    const emailField = document.getElementById('email');
    const passwordField = document.getElementById('password');
    if (emailField && passwordField) {
        emailField.value = email;
        passwordField.value = password;
        // highlight briefly
        emailField.classList.add('is-valid');
        passwordField.classList.add('is-valid');
    }
}
