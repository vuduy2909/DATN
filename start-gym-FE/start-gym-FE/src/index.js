import React from 'react';
import ReactDOM from 'react-dom/client';
import './index.css';
import App from './App';
import 'bootstrap/dist/css/bootstrap.min.css';
import { AuthProvider } from './AuthContext';
import { BrowserRouter } from 'react-router-dom';
import { ParallaxProvider } from "react-scroll-parallax";

// Add auto-dismiss functionality for specific alerts
const setupAlertAutoClose = () => {
  // Save the original alert function
  const originalAlert = window.alert;
  
  // Override the alert function
  window.alert = function(message) {
    console.log('Alert intercepted:', message);
    
    // Check if this is the success message about recommendations
    if (message && typeof message === 'string' && 
        message.includes('Course recommendations generated successfully')) {
      console.log('Auto-dismissing recommendation success message');
      
      // Delay to allow any state updates to complete
      setTimeout(() => {
        // Find any OK buttons in dialogs and click them
        const buttons = document.querySelectorAll('button');
        for (let button of buttons) {
          if (button.textContent === 'OK') {
            console.log('Found OK button, clicking it');
            button.click();
            break;
          }
        }
        
        // Dispatch a custom event that our components can listen for
        const event = new CustomEvent('recommendationComplete');
        document.dispatchEvent(event);
      }, 100);
      
      // Don't show the actual alert
      return;
    }
    
    // For other alerts, use the original behavior
    return originalAlert.apply(this, arguments);
  };
};

// Set up the alert interceptor
setupAlertAutoClose();

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
