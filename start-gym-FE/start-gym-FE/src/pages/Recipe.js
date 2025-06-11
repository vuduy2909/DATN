import React, { useState, useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import Header from '../components/Header/Header';
import Footer from '../components/Footer/Footer';

const Recipe = () => {
    const [searchTerm, setSearchTerm] = useState('');
    const location = useLocation();

    // Extract search term from URL if present
    useEffect(() => {
        const searchParams = new URLSearchParams(location.search);
        const search = searchParams.get('search');
        if (search) {
            setSearchTerm(search);
        } else {
            setSearchTerm('');
        }
    }, [location.search]);

    return (
        <div style={{ backgroundColor: "#f0ece8", minHeight: "100vh" }}>
            <Header />
            <div style={{ padding: '20px' }}>
                <h1>Recipe</h1>
                {searchTerm && (
                    <div style={{ margin: '20px 0', padding: '15px', backgroundColor: 'white', borderRadius: '8px' }}>
                        <p>Showing results for: <strong>{searchTerm}</strong></p>
                        {/* Recipe search results would go here */}
                    </div>
                )}
                {/* Recipe content would go here */}
            </div>
            <Footer />
        </div>
    );
};

export default Recipe;