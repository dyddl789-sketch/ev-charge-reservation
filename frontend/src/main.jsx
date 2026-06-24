import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
// import './index.css'
import App from './App.jsx'

import "./styles/reset.css";
import "./styles/common.css";
import "./styles/main.css";

console.log("main.jsx 실행");

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
