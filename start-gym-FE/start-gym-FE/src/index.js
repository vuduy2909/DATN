import React from 'react';
import ReactDOM from 'react-dom/client';
import './index.css';
import App from './App';
import 'bootstrap/dist/css/bootstrap.min.css';
import { AuthProvider } from './AuthContext';
import { BrowserRouter } from 'react-router-dom';
import { ParallaxProvider } from "react-scroll-parallax";
import reportWebVitals from './reportWebVitals';

// Intercept alert function to handle recommendation dialog
const originalAlert = window.alert;
window.alert = function(message) {
  if (message && message.includes('thành công')) {
    // Auto-dismiss success messages after a short delay
    setTimeout(() => {
      const button = document.querySelector('button');
      if (button && (button.textContent === 'OK' || button.textContent === 'Đồng ý')) {
        button.click();
      }
    }, 100);
  }
  return originalAlert.call(window, message);
};

const root = ReactDOM.createRoot(document.getElementById('root'));

root.render(
  <BrowserRouter>
    <AuthProvider>
      <ParallaxProvider>
        <App />
      </ParallaxProvider>
    </AuthProvider>
  </BrowserRouter>
);
