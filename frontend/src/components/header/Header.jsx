import hdfc_logo from "../../assets/hdfc_logo.png";
import Upper from "../letsBegin/Upper";
import "./Header.css";

let Header = () => {
  return (
    <>
      <header className="p-3  header">
        <div className="container">
          <div className="">
            <img src={hdfc_logo} alt="hdfc_logo" className="logo" />
          </div>

          <div className="headerButtons">
            <div className="d-flex gap-4">
              <div className="buttonContainer">
                <i className="bi bi-geo-alt headerButtonIcon"></i>
                <button
                  type="button"
                  className="btn light me-2 headerButtonTitle"
                >
                  Locate us
                </button>
              </div>

              <div className="buttonContainer">
                <i class="bi bi-question-lg headerButtonIcon"></i>
                <button
                  type="button"
                  className="btn light me-2 headerButtonTitle"
                >
                  Help
                </button>
              </div>
            </div>
          </div>
        </div>
      </header>
    </>
  );
};

export default Header;
