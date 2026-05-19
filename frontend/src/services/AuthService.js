const baseUrl = "http://localhost:8080/api/v1";

let login = async (loginRequest) => {


  let response = await fetch(
    `${baseUrl}/auth/login`,{
      method:"POST",
      headers:{
        "Content-Type":"application/json"
      },
      body:JSON.stringify(loginRequest)
    }
  );

  let data = await response.json();

  return data;
}

export default login;
