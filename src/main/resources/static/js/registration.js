document.addEventListener('DOMContentLoaded', () => {
    const registrationDiv = document.getElementsByClassName("registration")[0];
    const form = document.getElementsByTagName("form")[0];
    const name = document.getElementById("name");
    const email = document.getElementById("email");
    const password = document.getElementById("psw");

    form.addEventListener("submit", async (event) => {
        event.preventDefault();

        let inputRegistration = {name: name.value, email: email.value, password: password.value};

        try {
            let response = await fetch('/api/users', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(inputRegistration),
            });
            let data = await response.json();
            if (response.ok) {
                const loginLink = document.createElement("a");
                loginLink.innerText = "Successful registration! Verification email has been sent! Press the link to login!"
                loginLink.setAttribute("href", "http://localhost:8080/login");
                loginLink.setAttribute("id", "loginLink");
                registrationDiv.appendChild(loginLink);
            } else {
                throw new Error(data.errorMessage);
            }
        } catch (error) {
            console.error(error);
        }
    });
});