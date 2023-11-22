document.addEventListener('DOMContentLoaded', () => {
    async function verificateUser() {
        let urlString = window.location.href;
        let urlArray = urlString.split('/');
        console.log('/email-verification/' + urlArray[urlArray.length - 1])
        console.log(window.localStorage.getItem("token"));
        try {
            let response = await fetch('/api/email-verification/' + urlArray[urlArray.length - 1], {
                method: 'GET',
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + window.localStorage.getItem("token"),
                }
            });
            let data = await response.json();
            if (!response.ok) {
                throw new Error(response.statusText);
            }
        } catch (error) {
            console.log(error);
        }
    };
    verificateUser();
});