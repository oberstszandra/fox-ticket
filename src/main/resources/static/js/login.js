document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementsByTagName("form")[0];
    const email = document.getElementById("email");
    const password = document.getElementById("psw");

    form.addEventListener("submit", async (event) => {
        event.preventDefault();

        let inputLogin = {email: email.value, password: password.value};

        try {
            let response = await fetch('/api/users/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(inputLogin),
            });
            let data = await response.json();
            if (response.ok) {
                window.localStorage.setItem("token", data.token);
                window.location.assign('/home');

            } else {
                throw new Error(data.errorMessage);
            }
        } catch (error) {
            console.error(error);
        }

    });
});