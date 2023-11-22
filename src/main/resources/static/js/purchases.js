document.addEventListener('DOMContentLoaded', () => {
    const purchases = document.getElementsByClassName("purchases-table")[0];

    async function getPurchases() {
        try {
            let response = await fetch('/api/orders', {
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
            data.orders.forEach((ticket) => {
                console.log(ticket);
                const ticketCard = document.createElement("div");
                ticketCard.setAttribute("class", "ticket-purchase");
                const foxHead = document.createElement("div");
                foxHead.setAttribute("class", "foxhead");
                const fox = document.createElement("img");
                fox.setAttribute("class", "foxheadimg");
                fox.src = "/jpeg/foxhead.png";
                const prodName = document.createElement("h1");
                prodName.textContent = ticket.product_name;
                const prodStatus = document.createElement("p");
                prodStatus.textContent = ticket.status;
                const usebtn = document.createElement("button");
                usebtn.setAttribute("class", "usebtn")
                usebtn.setAttribute("id", ticket.id)
                usebtn.textContent = "use";

                foxHead.appendChild(fox);
                ticketCard.appendChild(foxHead);
                ticketCard.appendChild(prodName);
                ticketCard.appendChild(prodStatus);
                ticketCard.appendChild(usebtn);

                purchases.appendChild(ticketCard);

                usebtn.addEventListener("click", ev => {
                    ev.preventDefault();

                    async function addProductToCart() {
                        try {
                            const response = await fetch(`/api/orders/${usebtn.id}`, {
                                method: 'PATCH',
                                headers: {
                                    'Accept': 'application/json',
                                    'Content-Type': 'application/json',
                                    'Authorization': 'Bearer ' + window.localStorage.getItem("token"),
                                },
                            });
                            const data = await response.json();
                            if (!response.ok) {
                                throw new Error(response.statusText);
                            }
                            prodStatus.textContent = data.status;
                            prodStatus.style.color = "white";
                            prodName.style.color = "white";
                            usebtn.style.display = "none";
                            ticketCard.style.background = "#3cb878";
                        } catch (error) {
                            console.log('ERROR!', error);
                        }
                    };
                    addProductToCart();
                });
            });
        } catch (error) {
            console.log(error);
        }
    };
    getPurchases();
});