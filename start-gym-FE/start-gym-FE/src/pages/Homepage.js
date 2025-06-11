import React, { useEffect } from "react";
import Header from "../components/Header/Header";
import Banner from "../components/Banner/Banner";
import BMICalculator from "../components/BMICalculator/BMICalculator";
import Courses from "../components/Courses/Courses";

import Recipes from "../components/Recipes/Recipes";
import Review from "../components/Review/Review";
import Blog from "../components/Blog/Blog";
import Footer from "../components/Footer/Footer";
import { Parallax } from "react-scroll-parallax";
import imgIcon from '../images/Dumbell_02.png'
import imgIconBell from '../images/Kettlebell_03.png'
import { useLocation, Link } from 'react-router-dom';
import { useAuth } from '../AuthContext'
import './Homepage.css';

const Homepage = () => {
    const location = useLocation();
    const { user } = useAuth();
    
    useEffect(() => {
        // Kiểm tra xem có redirect từ trang admin không (từ state hoặc sessionStorage)
        const navigatingFromAdmin = sessionStorage.getItem('navigatingFromAdmin');
        const fromAdminState = location.state?.fromAdmin;
        
        if (navigatingFromAdmin === 'true' || fromAdminState) {
            console.log('Phát hiện chuyển hướng từ trang admin, cập nhật trạng thái...');
            // Xóa flag sau khi đã xử lý
            sessionStorage.removeItem('navigatingFromAdmin');
            
            // Xóa state để tránh ảnh hưởng lần sau
            if (location.state?.fromAdmin) {
                window.history.replaceState({}, document.title);
            }
        }
    }, [location]);

    return (
        <div className="homepage">
            <Header />
            
            {/* Hero Section */}
            <section className="hero-section">
                <Parallax
                    translateY={[40, 200]}
                    speed={30}
                    className="parallax-icon-left"
                >
                    <img src={imgIcon} alt="Dumbbell icon" className="floating-icon" />
                </Parallax>
                <Banner />
                <Parallax
                    translateY={[0, 200]}
                    speed={30}
                    className="parallax-icon-right"
                >
                    <img src={imgIconBell} alt="Kettlebell icon" className="floating-icon" />
                </Parallax>
            </section>

            {/* Features Section */}
            <section className="features-section">
                <div className="container">
                    <div className="section-header">
                        <h2>Why Choose Start Gym?</h2>
                        <p>Transform your fitness journey with our comprehensive platform</p>
                    </div>
                    <div className="features-grid">
                        <div className="feature-card">
                            <div className="feature-icon">
                                <i className="fas fa-dumbbell"></i>
                            </div>
                            <h3>Personalized Workouts</h3>
                            <p>AI-powered workout recommendations tailored to your fitness level and goals</p>
                        </div>
                        <div className="feature-card">
                            <div className="feature-icon">
                                <i className="fas fa-chart-line"></i>
                            </div>
                            <h3>Progress Tracking</h3>
                            <p>Monitor your fitness journey with detailed analytics and progress charts</p>
                        </div>

                        <div className="feature-card">
                            <div className="feature-icon">
                                <i className="fas fa-utensils"></i>
                            </div>
                            <h3>Nutrition Plans</h3>
                            <p>Healthy recipes and meal plans to complement your fitness routine</p>
                        </div>
                    </div>
                </div>
            </section>

            {/* BMI Calculator Section */}
            <section className="bmi-section">
                <BMICalculator />
            </section>

            {/* Courses Section */}
            <section className="courses-section">
                <Courses />
            </section>

            {/* Quick Actions Section */}
            <section className="quick-actions-section">
                <div className="container">
                    <div className="section-header">
                        <h2>Quick Start</h2>
                        <p>Jump into your fitness journey right away</p>
                    </div>
                    <div className="quick-actions-grid">
                        <Link to="/recommend" className="action-card">
                            <div className="action-icon">
                                <i className="fas fa-magic"></i>
                            </div>
                            <h3>Get Recommendations</h3>
                            <p>Get personalized workout and nutrition recommendations</p>
                            <span className="action-arrow">→</span>
                        </Link>
                        <Link to="/collections" className="action-card">
                            <div className="action-icon">
                                <i className="fas fa-play-circle"></i>
                            </div>
                            <h3>Browse Workouts</h3>
                            <p>Explore our extensive library of workout videos</p>
                            <span className="action-arrow">→</span>
                        </Link>
                        <Link to="/recipe" className="action-card">
                            <div className="action-icon">
                                <i className="fas fa-apple-alt"></i>
                            </div>
                            <h3>Healthy Recipes</h3>
                            <p>Discover nutritious recipes for your fitness goals</p>
                            <span className="action-arrow">→</span>
                        </Link>
                        {user && (
                            <Link to="/profile" className="action-card">
                                <div className="action-icon">
                                    <i className="fas fa-user-circle"></i>
                                </div>
                                <h3>My Profile</h3>
                                <p>Track your progress and manage your profile</p>
                                <span className="action-arrow">→</span>
                            </Link>
                        )}
                    </div>
                </div>
            </section>

            {/* Recipes Section */}
            <section className="recipes-section">
                <Recipes />
            </section>



            {/* Statistics Section */}
            <section className="stats-section">
                <div className="container">
                    <div className="stats-grid">
                        <div className="stat-item">
                            <div className="stat-number">1000+</div>
                            <div className="stat-label">Active Members</div>
                        </div>
                        <div className="stat-item">
                            <div className="stat-number">500+</div>
                            <div className="stat-label">Workout Videos</div>
                        </div>
                        <div className="stat-item">
                            <div className="stat-number">50+</div>
                            <div className="stat-label">Expert Trainers</div>
                        </div>
                        <div className="stat-item">
                            <div className="stat-number">95%</div>
                            <div className="stat-label">Success Rate</div>
                        </div>
                    </div>
                </div>
            </section>

            {/* Reviews Section */}
            <section className="reviews-section">
                <Review />
            </section>

            {/* Blog Section */}
            <section className="blog-section">
                <Blog />
            </section>



            <Footer />
        </div>
    );
};

export default Homepage;
