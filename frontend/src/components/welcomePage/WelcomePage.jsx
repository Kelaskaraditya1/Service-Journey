import DashboardContent from './DashboardContent';
import Header from './Header';
import Sidebar from './SideBar';
import './WelcomePage'

let WelcomePage = ()=>{

  return <div>

  <Header />

  <div style={{ display: "flex" }}>

    <Sidebar />

    <DashboardContent />

  </div>


  </div>

}

export default WelcomePage;