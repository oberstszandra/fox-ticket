document.addEventListener('DOMContentLoaded', () => {
    const products = document.getElementById("products");

    async function getProducts() {
        try {
            let response = await fetch('/api/products', {
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
            data.products.forEach((product) => {
                const cardWrap = document.createElement("div");
                cardWrap.setAttribute("class", "cardWrap");
                const cardLeft = document.createElement("div");
                cardLeft.classList.add("card");
                cardLeft.classList.add("cardLeft");
                const prodName = document.createElement("h2");
                prodName.textContent = product.name;
                const prodDescr = document.createElement("div");
                prodDescr.setAttribute("class", "prodDescription");
                const description = document.createElement("h2");
                description.textContent = product.description;
                const prodDur = document.createElement("div");
                prodDur.setAttribute("class", "prodDuration");
                const duration = document.createElement("h2");
                duration.textContent = `${product.duration} hours`;
                const durSpan = document.createElement("span");
                durSpan.textContent = "duration";
                const cardRight = document.createElement("div");
                cardRight.classList.add("card");
                cardRight.classList.add("cardRight");
                const prodPrice = document.createElement("div");
                prodPrice.setAttribute("class", "prodprice");
                const price = document.createElement("h3");
                price.textContent = product.price;
                const valuta = document.createElement("span");
                valuta.textContent = "Forint";
                const cartbtn = document.createElement("button");
                cartbtn.setAttribute("class", "cartbtn");
                cartbtn.setAttribute('id', product.id);
                cartbtn.type = "submit";
                cartbtn.textContent = "Add to cart";

                cardWrap.appendChild(cardLeft);
                cardLeft.appendChild(prodName);
                cardLeft.appendChild(prodDescr);
                prodDescr.appendChild(description);
                cardLeft.appendChild(prodDur);
                prodDur.appendChild(duration);
                prodDur.appendChild(durSpan);
                cardWrap.appendChild(cardRight);
                cardRight.appendChild(prodPrice);
                prodPrice.appendChild(price);
                prodPrice.appendChild(valuta);
                products.appendChild(cardWrap);
                cardRight.appendChild(cartbtn);

                cartbtn.addEventListener("click", ev => {
                    ev.preventDefault();

                    async function addProductToCart() {
                        let input = {productId: cartbtn.id};
                        try {
                            const response = await fetch('/api/cart', {
                                method: 'POST',
                                headers: {
                                    'Accept': 'application/json',
                                    'Content-Type': 'application/json',
                                    'Authorization': 'Bearer ' + window.localStorage.getItem("token"),
                                },
                                body: JSON.stringify(input),
                            });

                            if (!response.ok) {
                                throw new Error(response.statusText);
                            }
                            const data = await response.json();
                            alert("The selected product is in the cart!");
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
    getProducts();
});