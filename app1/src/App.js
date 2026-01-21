import { Routes, Route, Link } from "react-router-dom";

import TicTacToe from "./tic-tac-toe";
import HelloWorld from "./hello-world"

export default function App () {
    return (
        <>
            <nav>
                <ul>
                    <li><Link to="hello">Hello</Link></li>
                    <li><Link to="tic-tac-toe">Tic Tac Toe</Link></li>
                    <li><a href="/app2">Dashboard</a></li>
                </ul>
            </nav>

            <h2>Home page</h2>

            <Routes>
                <Route path="tic-tac-toe" element={<TicTacToe />} />
                <Route path="hello" element={<HelloWorld />} />
            </Routes>
        </>
    );
}
