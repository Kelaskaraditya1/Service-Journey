import { Outlet } from "react-router-dom";
import "./LetsBegin.css";
import Lower from "./Lower";
import Upper from "./Upper";

function Card() {
  return (
    <div className="card">
      <div className="card-upper">
        <Upper />
      </div>

      <div className="card-lower">
        <Outlet />
      </div>
    </div>
  );
}

export default Card;
