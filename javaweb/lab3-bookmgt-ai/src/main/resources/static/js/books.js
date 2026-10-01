document.addEventListener('DOMContentLoaded', () => {
    const form = document.querySelector('#bookForm');
    const floatingAdd = document.querySelector('#floatingAdd');
    const deleteDialog = document.querySelector('#deleteDialog');
    const deleteBookTitle = document.querySelector('#deleteBookTitle');
    const cancelDelete = document.querySelector('#cancelDelete');
    const confirmDelete = document.querySelector('#confirmDelete');
    let pendingDeleteForm = null;

    floatingAdd.addEventListener('click', () => {
        document.querySelector('#book-form').scrollIntoView({ behavior: 'smooth', block: 'center' });
        form.querySelector('input[name="title"]').focus({ preventScroll: true });
    });

    form.addEventListener('submit', (event) => {
        let firstInvalidField = null;

        form.querySelectorAll('.floating-field').forEach((field) => {
            const input = field.querySelector('input');
            const error = field.querySelector('.field-error');
            const value = input.value.trim();
            let message = '';

            if (!value) {
                message = '此项为必填内容。';
            } else if (value.length > input.maxLength) {
                message = `内容不能超过 ${input.maxLength} 个字符。`;
            }

            field.classList.toggle('invalid', Boolean(message));
            error.textContent = message;
            if (message && !firstInvalidField) {
                firstInvalidField = input;
            }
        });

        if (firstInvalidField) {
            event.preventDefault();
            firstInvalidField.focus();
        }
    });

    form.querySelectorAll('.floating-field input').forEach((input) => {
        input.addEventListener('input', () => {
            input.closest('.floating-field').classList.remove('invalid');
            input.closest('.floating-field').querySelector('.field-error').textContent = '';
        });
    });

    document.querySelectorAll('.delete-form').forEach((deleteForm) => {
        deleteForm.addEventListener('submit', (event) => {
            event.preventDefault();
            pendingDeleteForm = deleteForm;
            deleteBookTitle.textContent = deleteForm.querySelector('button').dataset.bookTitle;
            deleteDialog.showModal();
        });
    });

    cancelDelete.addEventListener('click', () => {
        pendingDeleteForm = null;
        deleteDialog.close();
    });

    confirmDelete.addEventListener('click', () => {
        if (pendingDeleteForm) {
            pendingDeleteForm.submit();
        }
    });

    deleteDialog.addEventListener('click', (event) => {
        if (event.target === deleteDialog) {
            pendingDeleteForm = null;
            deleteDialog.close();
        }
    });
});
