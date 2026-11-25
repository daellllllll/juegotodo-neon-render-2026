import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import App from './App.jsx'
import { BrowserRouter } from 'react-router-dom';
import { AuthProvider } from './auth/AuthContext.jsx';
import 'bootstrap/dist/css/bootstrap.min.css';
import './styles/global.css';


createRoot(document.getElementById('root')).render(
 <StrictMode>
   <AuthProvider>
     <BrowserRouter>
       <App />
     </BrowserRouter>
   </AuthProvider>
 </StrictMode>,
)
