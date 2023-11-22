document.addEventListener('DOMContentLoaded', () => {
    const table = document.getElementsByTagName("table")[0];
    const buyButton = document.getElementById("buybtn");

    async function getProductsInCart() {
        try {
            let response = await fetch('/api/cart', {
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

            let total = 0;

            data.cart.forEach((cartItem) => {
                const newRow = document.createElement("tr");
                const rowData = document.createElement("td");
                const rowDiv = document.createElement("div");
                rowDiv.setAttribute("class", "cart-info");
                const busIcon = document.createElement("img");
                busIcon.src = "/jpeg/bus-icon.jpeg";
                const rowDataDiv = document.createElement("div");
                const prodName = document.createElement("p");
                prodName.textContent = cartItem.name;
                const prodPrice = document.createElement("small");
                prodPrice.textContent = `${cartItem.price} Ft`;
                const rowBreak = document.createElement("br");
                const remove = document.createElement("a");
                remove.textContent = "Remove";
                const quantityData = document.createElement("td");
                const quantityField = document.createElement("input");
                quantityField.type = "number";
                quantityField.value = "1";
                const subTotal = document.createElement("td");
                subTotal.textContent = `${cartItem.price} Ft`;

                total += cartItem.price;

                rowDataDiv.appendChild(prodName);
                rowDataDiv.appendChild(prodPrice);
                rowDataDiv.appendChild(rowBreak);
                rowDataDiv.appendChild(remove);
                rowDiv.appendChild(busIcon);
                rowDiv.appendChild(rowDataDiv);
                rowData.appendChild(rowDiv);
                newRow.appendChild(rowData);
                quantityData.appendChild(quantityField);
                newRow.appendChild(quantityData);
                newRow.appendChild(subTotal);
                table.appendChild(newRow);

            });

            const priceResult = document.querySelector("#total-price-cell");
            priceResult.textContent = `${total} Ft`;

            buyButton.addEventListener("click", ev => {
                ev.preventDefault();

                async function createOrders() {
                    try {
                        const response = await fetch('/api/orders', {
                            method: 'POST',
                            headers: {
                                'Accept': 'application/json',
                                'Content-Type': 'application/json',
                                'Authorization': 'Bearer ' + window.localStorage.getItem("token"),
                            }
                        });
                        const data = await response.json();
                        if (!response.ok) {
                            alert(data.errorMessage)
                            throw new Error(response.statusText);
                        } else {
                            alert("Thank you for your purchase!");
                        }
                    } catch (error) {
                        console.log('ERROR!', error);
                    }
                };
                createOrders();
            });
        } catch (error) {
            console.log(error);
        }
    };
    getProductsInCart();
});