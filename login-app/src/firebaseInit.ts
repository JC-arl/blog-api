// Import the functions you need from the SDKs you need
import { initializeApp } from "firebase/app";
import { getAuth } from "firebase/auth";

// TODO: Add SDKs for Firebase products that you want to use
// https://firebase.google.com/docs/web/setup#available-libraries

// Your web app's Firebase configuration
const firebaseConfig = {
    apiKey: "AIzaSyCp2_JsoeXkQTwhylN7gfmewbN1fiLIbTc",
    authDomain: "refdgsgdgf.firebaseapp.com",
    projectId: "refdgsgdgf",
    storageBucket: "refdgsgdgf.firebasestorage.app",
    messagingSenderId: "71135300094",
    appId: "1:71135300094:web:df01191f593173ee4898af"
};

// Initialize Firebase
const firebaseApp = initializeApp(firebaseConfig);

const firebaseAuth = getAuth(firebaseApp);

export default  firebaseAuth;
