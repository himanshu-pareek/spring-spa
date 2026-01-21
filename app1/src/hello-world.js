import { useState } from "react";

export default function HelloWorld () {

    const [username, setUsername] = useState("there");

    const sayHello = async () => {
        const details = await fetch('/resources/me');
        const me = await details.json();
        console.log({me});
        setUsername(me.username);
    };

    const addBook = async () => {
        const bookToAdd = {
            title: "Book Title",
            author: "Book Author",
            pages: 300
        };
        const csrfCookie = await cookieStore.get("XSRF-TOKEN");
        const csrfValue = csrfCookie.value;
        const result = await fetch(
            "/resources/books",
            {
                method: "POST",
                headers: {
                    "X-XSRF-TOKEN": csrfValue,
                    "Content-Type": "application/json",
                },
                body: JSON.stringify(bookToAdd)
            }
        );
        const addedBook = await result.json();
        console.log({ addedBook });
    };

    return <div>
        <div>Hi, <pre>{username}</pre></div>

        <button onClick={sayHello}>Who am I?</button>

        <div>
            <button onClick={addBook}>Add book</button>
        </div>
    </div>
}