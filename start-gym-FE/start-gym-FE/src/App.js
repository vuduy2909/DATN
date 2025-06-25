import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom'
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
import NewCustomer from './pages/Admin/NewCustomer';
import NewCourse from './pages/Admin/NewCourse';
import NewCustomerType from './pages/Admin/NewCustomerType';
import EditCustomerType from './pages/Admin/EditCustomerType';
import ViewCourse from './pages/Admin/ViewCourse';
import EditCourse from './pages/Admin/EditCourse';
import CustomerCourses from './pages/Admin/CustomerCourses';
import AdminRoute from './components/AdminRoute';
import NotFound from './pages/NotFound';
import ScrollToTop from './components/ScrollToTop';
import Contact from './pages/Contact/Contact';
import ForgotPassword from './components/ForgotPassword/ForgotPassword';

function App() {
  return (
    <>
      <ScrollToTop />
    <Routes>
        {/* Trang chính */}
      <Route path="/" element={<Homepage />} />
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      <Route path="/forgot-password" element={<ForgotPassword />} />
      <Route path="/user" element={<User />} />
      <Route path="/profile" element={<Profile />} />
      <Route path="/account" element={<Account />} />
      <Route path="/about" element={<About />} />
      <Route path="/recipe" element={<Recipe />} />
      <Route path="/blog" element={<Blog />} />
      <Route path="/contact" element={<Contact />} />
      <Route path="/collections" element={<Collections />} />
      <Route path="/collections/:courseId" element={<CollectionsDetail />} />
      <Route path="/collections/:courseId/days/:dayId" element={<Workout />} />
      <Route path='/recommend' element={<Recommend />} />
      <Route path='/payment/result' element={<PaymentResult />} />
      
      {/* Protected Admin Routes */}
      <Route path='/admin' element={<AdminRoute><AdminPage /></AdminRoute>} />
        <Route path='/admin/dashboard' element={<AdminRoute><AdminPage defaultTab="dashboard" /></AdminRoute>} />
        <Route path='/admin/courses' element={<AdminRoute><AdminPage defaultTab="courses" /></AdminRoute>} />
        <Route path='/admin/exercises' element={<AdminRoute><AdminPage defaultTab="exercises" /></AdminRoute>} />
        <Route path='/admin/customers' element={<AdminRoute><AdminPage defaultTab="customers" /></AdminRoute>} />
        <Route path='/admin/customer-types' element={<AdminRoute><AdminPage defaultTab="customerType" /></AdminRoute>} />
        <Route path='/admin/comments' element={<AdminRoute><AdminPage defaultTab="comments" /></AdminRoute>} />
        <Route path='/admin/revenue' element={<AdminRoute><AdminPage defaultTab="revenue" /></AdminRoute>} />
        
        {/* Admin Action Routes */}
        <Route path='/admin/customers/new' element={<AdminRoute><NewCustomer /></AdminRoute>} />
        <Route path='/admin/courses/new' element={<AdminRoute><NewCourse /></AdminRoute>} />
      <Route path='/admin/customer-types/new' element={<AdminRoute><NewCustomerType /></AdminRoute>} />
      <Route path='/admin/customer-types/edit/:id' element={<AdminRoute><EditCustomerType /></AdminRoute>} />
      
        {/* Course Management Routes */}
        <Route path='/admin/courses/:courseId/view' element={<AdminRoute><ViewCourse /></AdminRoute>} />
        <Route path='/admin/courses/:courseId/edit' element={<AdminRoute><EditCourse /></AdminRoute>} />
        
        {/* Exercise and Customer Edit Routes */}
        <Route path='/exercise/new' element={<AdminRoute><NewExercise /></AdminRoute>} />
        <Route path='/admin/exercise/:id/edit' element={<AdminRoute><EditExercise /></AdminRoute>} />
        <Route path='/admin/customers/edit/:id' element={<AdminRoute><EditCustomer /></AdminRoute>} />
        
        {/* Customer Courses Route */}
        <Route path='/admin/customers/:customerId/courses' element={<AdminRoute><CustomerCourses /></AdminRoute>} />
        
        {/* 404 Not Found - Catch all route */}
        <Route path="/404" element={<NotFound />} />
        <Route path="*" element={<Navigate replace to="/404" />} />
    </Routes>
    </>
  );
}

export default App;
