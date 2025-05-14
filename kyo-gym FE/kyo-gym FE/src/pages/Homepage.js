import React, { useState, useEffect } from "react";
import Header from "../components/Header/Header";
import Banner from "../components/Banner/Banner";
import BMICalculator from "../components/BMICalculator/BMICalculator";
import Courses from "../components/Courses/Courses";
import Coaches from "../components/Coaches/Coaches";
import Recipes from "../components/Recipes/Recipes";
import Review from "../components/Review/Review";
import Blog from "../components/Blog/Blog";
import Footer from "../components/Footer/Footer";
import { useParallax } from "react-scroll-parallax";
import { Parallax } from "react-scroll-parallax";
import imgIcon from '../images/Dumbell_02.png'
import imgIconBell from '../images/Kettlebell_03.png'

const Homepage = () => {
    useEffect(() => {
        // Kiểm tra xem có redirect từ trang admin không 
        const navigatingFromAdmin = sessionStorage.getItem('navigatingFromAdmin');
        if (navigatingFromAdmin === 'true') {
            console.log('Phát hiện chuyển hướng từ trang admin, cập nhật trạng thái...');
            // Xóa flag sau khi đã xử lý
            sessionStorage.removeItem('navigatingFromAdmin');

            // Nếu đang để admin=true, tạm thời tắt nó đi khi ở trang Home
            if (localStorage.getItem('isAdmin') === 'true') {
                // Lưu trạng thái admin để có thể khôi phục sau này
                sessionStorage.setItem('adminState', 'true');
                localStorage.setItem('isAdmin', 'false');
            }
        }
    }, []);

    return (
        <div>
            <Header />
            <Parallax
                translateY={[40, 200]}
                speed={30}
                style={{ height: '198px', position: 'absolute', left: '268px' }}
            >
                <img src={imgIcon} style={{ height: '300px', width: '300px' }}></img>
            </Parallax>
            <Banner />
            <BMICalculator />
            <Parallax
                translateY={[0, 200]}
                speed={30}
                style={{ height: '200px', position: 'absolute', right: '150px' }}
            >
                <img src={imgIconBell} style={{ height: '300px', width: '300px' }}></img>
            </Parallax>
            <Courses />
            <Recipes />
            <Coaches />
            <Review />
            <Blog />
            <Footer />
        </div>
    );
};

export default Homepage;
