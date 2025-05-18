import React from 'react';
import { request } from '../../utils/axiosInstance';
import { useAuth } from '../../AuthContext'
import { useEffect, useState } from 'react'
import { Table } from 'react-bootstrap';
// import { faHourglass1 } from '@fortawesome/free-solid-svg-icons';

const CustomerComponent = () => {
    const { user } = useAuth();
    const [userData, setUserData] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchUserData = async () => {
            try {
                // Check if token exists
                const token = localStorage.getItem('token');
                if (!token) {
                    setError("No authentication token found. Please log in.");
                    setLoading(false);
                    return;
                }

                console.log("Token exists:", !!token);
                const res = await request.get('/customer/list');
                console.log("API response:", res);
                
                if (res && Array.isArray(res)) {
                    setUserData(res);
                } else if (res && typeof res === 'object') {
                    // Try to find an array within the object
                    const possibleArrays = Object.values(res).filter(val => Array.isArray(val));
                    if (possibleArrays.length > 0) {
                        setUserData(possibleArrays[0]);
                    } else {
                        setUserData([]);
                        setError("Received data but couldn't find customer array");
                    }
                } else {
                    setUserData([]);
                    setError("Unexpected response format");
                }
            } catch (error) {
                console.log("Error fetching user information", error);
                setError("Failed to fetch customer data: " + (error.message || "Unknown error"));
            } finally {
                setLoading(false);
            }
        }

        if (user) {
            fetchUserData();
        } else {
            setError("User not authenticated. Please log in first.");
            setLoading(false);
        }
    }, [user]);

    if (loading) {
        return <h1>Loading customers...</h1>;
    }

    if (error) {
        return <h1>{error}</h1>;
    }

    if (!userData || userData.length === 0) {
        return <h1>No customers found</h1>;
    }

    return (
        <div>
            <Table responsive striped bordered hover>
                <thead>
                    <tr>
                        <th>Name</th>
                        <th>Email</th>
                        <th>Phone</th>
                        <th>Address</th>
                    </tr>
                </thead>
                <tbody>
                    {userData.map((customer) => (
                        <tr key={customer.customerId}>
                            <td>{customer.customerName}</td>
                            <td>{customer.customerEmail}</td>
                            <td>{customer.customerPhone}</td>
                            <td>{customer.customerAddress}</td>
                        </tr>
                    ))}
                </tbody>
            </Table>
        </div>
    );
};

export default CustomerComponent;
