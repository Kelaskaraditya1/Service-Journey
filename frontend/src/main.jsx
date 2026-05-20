import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import "bootstrap/dist/css/bootstrap.min.css";
import "bootstrap-icons/font/bootstrap-icons.css";
import { createBrowserRouter, RouterProvider } from "react-router-dom";
import router from "./routes/AppRouter.jsx";
import App from "./App.jsx";
import { SessionProvider } from "./context/SessionContext.jsx";

createRoot(document.getElementById("root")).render(
  <StrictMode>
    <SessionProvider>
      <RouterProvider router={router}>
        <App />
      </RouterProvider>
    </SessionProvider>
  </StrictMode>,
);
