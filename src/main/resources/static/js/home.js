document.addEventListener('DOMContentLoaded', () => {
    const toggleElement1 = document.getElementById("toggle1");
    const toggleElement2 = document.getElementById("toggle2");
    const articleContainer = document.getElementById("article-container");
    toggleElement1.addEventListener("click", () => {
        let sec = document.getElementById('sec');
        let nav = document.getElementById('navigation');
        sec.classList.add('active');
        nav.classList.add('active');
        toggleElement1.style.display = 'none';
        toggleElement2.style.display = 'block';

    });
    toggleElement2.addEventListener("click", () => {
        let sec = document.getElementById('sec');
        let nav = document.getElementById('navigation');
        sec.classList.remove('active');
        nav.classList.remove('active');
        toggleElement1.style.display = 'block';
        toggleElement2.style.display = 'none';
    });

    async function getArticles() {
        try {
            const response = await fetch('/api/news');

            if (!response.ok) {
                throw new Error(response.statusText);
            }
            const data = await response.json();
            data.articles.forEach((article) => {
                const newArticle = document.createElement("div");
                newArticle.setAttribute('class', 'article');
                const articleText = document.createElement("div");
                articleText.setAttribute("class", "article-text");
                const title = document.createElement("h3");
                title.textContent = article.title;
                const content = document.createElement("p");
                content.setAttribute('class', 'article-content');
                content.textContent = article.content;
                const publishDate = document.createElement("p");
                publishDate.setAttribute('class', 'date');
                publishDate.textContent = article.publish_date;
                const greenBox = document.createElement("div");
                greenBox.setAttribute("class", "greenBox");
                const readMore = document.createElement("button");
                readMore.setAttribute("class", "readbtn");
                readMore.textContent = "Read more"

                articleText.appendChild(publishDate);
                articleText.appendChild(title);
                articleText.appendChild(content);
                newArticle.appendChild(articleText);
                newArticle.appendChild(greenBox);
                newArticle.appendChild(readMore);

                articleContainer.appendChild(newArticle);
            });
        } catch (error) {
            console.log('ERROR!', error);
        }
    };
    getArticles();
});