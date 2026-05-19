import { createBrowserRouter, RouterProvider } from "react-router-dom";
import App from "../App.jsx";
import Card from "../components/letsBegin/Card.jsx";
import Lower from "../components/letsBegin/Lower.jsx";
import Authentication from "../components/letsBegin/Authentication.jsx";
import Otp from "../components/otp/Otp.jsx";
import ConfirmYou from "../components/confirmYou/ConfirmYou.jsx";
import DebitCard from "../components/verification/DebitCard.jsx";
import NetBanking from "../components/verification/NetBanking.jsx";
import WelcomePage from "../components/welcomePage/WelcomePage.jsx";
import OtpErrorPage from "../components/errorPages/OtpErrorPage.jsx";

const router = createBrowserRouter([
  {
    path: "/",
    element: <App />,
    children: [
      {
        path: "/",
        element: <Card />,
        children: [
          { path: "/", element: <Lower /> },
          { path: "/authentication", element: <Authentication /> },
        ],
      },
    ],
  },
  {
    path: "/otp",
    element: <App />,
    children: [
      { path: "/otp", element: <Otp /> },
      {path:"error-page", element:<OtpErrorPage/>}
    ],
  },
  {
    path: "/confirm-you",
    element: <App />,
    children: [{ path: "/confirm-you", element: <ConfirmYou /> }],
  },
  {
    path: "/verification",
    element: <App />,
    children: [
      { path: "debit", element: <DebitCard /> },
      { path: "net", element: <NetBanking /> },
    ],
  },
  {
    path: "/welcome",
    element: <WelcomePage />,
  },
]);

export default router;
