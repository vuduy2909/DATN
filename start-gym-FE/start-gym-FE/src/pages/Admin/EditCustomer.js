import { Button, Form, Input, Select, message } from 'antd';
import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../../AuthContext';
import { request } from '../../utils/axiosInstance';

const EditCustomer = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const [loading, setLoading] = useState(true);
  const [customer, setCustomer] = useState({
    customerName: '',
    dateOfBirth: '',
    customerAddress: '',
    customerPhone: '',
    customerEmail: '',
    customerGender: '',
    idCard: '',
    customerCode: '',
    customerType: {}
  });
  
  useEffect(() => {
    const fetchCustomerData = async () => {
      try {
        setLoading(true);
        const res = await request.get('/customer/list/' + id);
        console.log('Customer data:', res);
        setCustomer(res);
      } catch (error) {
        console.log('Error fetching customer information', error);
        message.error('Failed to load customer information: ' + (error.message || 'Unknown error'));
      } finally {
        setLoading(false);
      }
    };

    if (user) {
      fetchCustomerData();
    }
  }, [user, id]);

  const handleInputChange = (field, value) => {
    setCustomer({
      ...customer,
      [field]: value
    });
  };

  const onFinish = async () => {
    try {
      if (user) {
        // Create the object in the format expected by the backend
        const customerInfo = {
          customerName: customer.customerName,
          dateOfBirth: customer.dateOfBirth,
          customerAddress: customer.customerAddress,
          customerPhone: customer.customerPhone,
          customerEmail: customer.customerEmail,
          customerGender: customer.customerGender,
          idCard: customer.idCard,
          customerCode: customer.customerCode,
          customerType: customer.customerType,
          customerId: customer.customerId
        };
        
        console.log('Submitting data:', customerInfo);
        
        await request.put('/customer/edit/' + id, customerInfo);
        message.success('Customer information updated successfully! 🎉');
        setTimeout(() => navigate('/admin'), 1500); // Delay để user thấy thông báo
      }
    } catch (error) {
      console.log('Error editing customer', error);
      message.error('Error updating customer information: ' + (error.message || 'Unknown error'));
    }
  };

  if (loading) {
    return <div className="text-center p-10">Loading customer information...</div>;
  }

  return (
    <div>
      <div>
        <div className='pt-10 px-44'>
          <h1 className="text-2xl font-bold mb-4">Edit Customer Information</h1>
          <p className="mb-4">Customer Code: <strong>{customer.customerCode}</strong></p>
          
          <Form
            onFinish={onFinish}
            className='pt-6 max-w-2xl bg-gray-100 p-6 rounded-lg shadow'
            labelCol={{
              span: 6,
            }}
            wrapperCol={{
              span: 16,
            }}
            layout='horizontal'
          >
            <Form.Item label='Full Name' rules={[{ required: true, message: 'Please enter the customer name' }]}>
              <Input
                placeholder='Enter full name'
                value={customer.customerName}
                onChange={(e) => handleInputChange('customerName', e.target.value)}
              />
            </Form.Item>
            
            <Form.Item label='Date of Birth' rules={[{ required: true, message: 'Please select date of birth' }]}>
              <Input
                placeholder='YYYY-MM-DD'
                value={customer.dateOfBirth}
                onChange={(e) => handleInputChange('dateOfBirth', e.target.value)}
              />
            </Form.Item>
            
            <Form.Item label='Gender' rules={[{ required: true, message: 'Please select gender' }]}>
              <Select
                value={customer.customerGender}
                onChange={(value) => handleInputChange('customerGender', value)}
                options={[
                  { value: 'Male', label: 'Male' },
                  { value: 'Female', label: 'Female' }
                ]}
              />
            </Form.Item>
            
            <Form.Item label='ID Card' rules={[{ required: true, message: 'Please enter ID Card number' }]}>
              <Input
                placeholder='Enter ID Card number'
                value={customer.idCard}
                disabled
              />
            </Form.Item>
            
            <Form.Item label='Address' rules={[{ required: true, message: 'Please enter address' }]}>
              <Input.TextArea
                autoSize={{ minRows: 2 }}
                placeholder='Enter address'
                value={customer.customerAddress}
                onChange={(e) => handleInputChange('customerAddress', e.target.value)}
              />
            </Form.Item>
            
            <Form.Item label='Phone Number' rules={[{ required: true, message: 'Please enter phone number' }]}>
              <Input
                placeholder='Enter phone number'
                value={customer.customerPhone}
                onChange={(e) => handleInputChange('customerPhone', e.target.value)}
              />
            </Form.Item>
            
            <Form.Item label='Email'>
              <Input
                placeholder='Enter email'
                value={customer.customerEmail}
                onChange={(e) => handleInputChange('customerEmail', e.target.value)}
              />
            </Form.Item>
            
            <Form.Item label='Customer Type'>
              <Input
                value={customer.customerType ? customer.customerType.customerTypeName : ''}
                disabled
              />
            </Form.Item>
            
            <Form.Item className='text-center' wrapperCol={{ span: 24 }}>
              <Button type='primary' htmlType='submit' className='bg-blue-500 hover:bg-blue-700 text-white px-6'>
                Update Customer
              </Button>
            </Form.Item>
          </Form>
        </div>
      </div>
    </div>
  );
};

export default EditCustomer;
