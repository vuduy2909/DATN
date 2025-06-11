import React from 'react';
import { Routes, Route } from 'react-router-dom'
import './App.css';
import Homepage from './pages/Homepage';
import Workout from './pages/Workout';
import Collections from './pages/Collections'
import CollectionsDetail from './pages/Collections_detail';
import Login from './components/Login/Login';
import About from './pages/About';
import Recipe from './pages/Recipe';
import Blog from './pages/Blog';
import Register from './components/Register/Register';
import User from './pages/User';
import Profile from './pages/Profile';
import Account from './pages/Account';
import Recommend from './pages/Recommend';
import PaymentResult from './pages/PaymentResult';
import AdminPage from './pages/Admin/AdminPage'
import NewExercise from './pages/Admin/NewExercise';
import EditExercise from './pages/Admin/EditExercise';
import EditCustomer from './pages/Admin/EditCustomer';
import NewCustomerType from './pages/Admin/NewCustomerType';
import EditCustomerType from './pages/Admin/EditCustomerType';
import AdminRoute from './components/AdminRoute';
import NotFound from './pages/NotFound';
import ScrollToTop from './components/ScrollToTop';

function App() {

  return (
    <Routes>
      {/* ScrollToTop component to automatically scroll to top on route change */}
      <Route path="*" element={<ScrollToTop />} />
      
      <Route path="/" element={<Homepage />} />
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      <Route path="/user" element={<User />} />
      <Route path="/profile" element={<Profile />} />
      <Route path="/account" element={<Account />} />
      <Route path="/about" element={<About />} />
      <Route path="/recipe" element={<Recipe />} />
      <Route path="/blog" element={<Blog />} />
      <Route path="/collections" element={<Collections />} />
      <Route path="/collections/:courseId" element={<CollectionsDetail />} />
      <Route path="/collections/:courseId/days/:dayId" element={<Workout />} />
      <Route path='/recommend' element={<Recommend />} />
      <Route path='/payment/result' element={<PaymentResult />} />
      
      {/* Protected Admin Routes */}
      <Route path='/admin' element={<AdminRoute><AdminPage /></AdminRoute>} />
      <Route path='/newexercise' element={<AdminRoute><NewExercise /></AdminRoute>} />
      <Route path='/editexercise/:id' element={<AdminRoute><EditExercise /></AdminRoute>} />
      <Route path='/editcustomer/:id' element={<AdminRoute><EditCustomer /></AdminRoute>} />
      <Route path='/admin/customer-types/new' element={<AdminRoute><NewCustomerType /></AdminRoute>} />
      <Route path='/admin/customer-types/edit/:id' element={<AdminRoute><EditCustomerType /></AdminRoute>} />
      
      {/* 404 Not Found - Must be last route */}
      <Route path="*" element={<NotFound />} />
    </Routes>
  );
}

export default App;
