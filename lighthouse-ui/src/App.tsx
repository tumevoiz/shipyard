import React from 'react';
import { Outlet, Link } from "react-router-dom";

function App() {
  return (
    <main>
      <div id="app" className="flex flex-col justify-center gap-y-4 md:container md:mx-auto w-full py-4">
        <header id="appNav" className="flex flex-row order-1">
          <div className="wrapper">
            <ul className="flex flex-row gap-4">
              <li><Link to="/">Home</Link></li>
              <li><Link to="/shipments">Shipments</Link></li>
              <li><Link to="/ships">Ships</Link></li>
            </ul>
          </div>
        </header>
        <div id="appContainer" className="flex flex-column order-2">
          <Outlet />
        </div>
      </div>
    </main>
  );
}

export default App;
