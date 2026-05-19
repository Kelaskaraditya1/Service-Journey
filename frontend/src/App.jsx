import { Outlet } from "react-router-dom";
import Footer from "./components/footer/Footer";
import Header from "./components/header/Header";
import LetsBegin from "./components/letsBegin/LetsBegin";
import ConfirmYou from "./components/confirmYou/ConfirmYou";
import DebitCard from "./components/verification/DebitCard";

let App = () => {
  return (
    <>
      <Header />
      <Outlet />
      <Footer />
    </>
  );
};

export default App;
