import React, { useEffect, useState } from 'react'
import Header from '../components/Header/Header'
import Footer from '../components/Footer/Footer'
import { useAuth } from '../AuthContext'
import { Container, Grid, Card, CardActionArea, CardMedia, CardContent, Typography } from '@mui/material';
import banner from '../images/user_banner.jpg'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faEnvelope } from '@fortawesome/free-regular-svg-icons';
import { faPhone, faLocationDot, faVenusMars, faCakeCandles, faUserGroup } from '@fortawesome/free-solid-svg-icons';
import { request } from '../utils/axiosInstance';
import styles from '../components/UserInformation/UserInformation.module.css'

const Profile = () => {
    const { user } = useAuth();
    const [userData, setUserData] = useState([]);

    useEffect(() => {
        const fetchUserData = async () => {
            try {
                const res = await request.get('/customer/detail')
                console.log("Gender from API:", res.customerGender, typeof res.customerGender)
                
                // Convert gender to proper boolean - handle string case specifically
                if (typeof res.customerGender === 'string') {
                    // Check for "male" string directly
                    const genderLower = res.customerGender.toLowerCase()
                    res.customerGender = (genderLower === 'male') // Will be true for "male", false otherwise
                }
                
                setUserData(res)
            } catch (error) {
                console.log("Error fetching user information", error);
            }
        }

        if (user) {
            fetchUserData();
        }
    }, [user])

    return (
        <div>
            <Header />
            <div className={styles.container}>
                <Container component="main" maxWidth="100%">
                    <Grid container spacing={2} justifyContent="center">
                        <Grid item xs={8}>
                            <Card className={styles.banner}>
                                <CardActionArea>
                                    <CardMedia
                                        className={styles.img_cover}
                                        component="img"
                                        height="185"
                                        image={banner || ''}
                                        alt="banner"
                                    />
                                </CardActionArea>
                                <CardContent className={styles.under_banner}>
                                    <CardMedia
                                        className={styles.img_ava}
                                        component="img"
                                        image={userData.customerImg || ''}
                                        height='auto'
                                        alt="avatar"
                                    />
                                    <Typography variant="h5" component="div" className={styles.customer_name}>
                                        {userData.customerName || ''}
                                    </Typography>
                                </CardContent>
                            </Card>
                        </Grid>
                        
                        <Grid item xs={8}>
                            <Card className={styles.information}>
                                <CardContent style={{ width: "100%", maxHeight: "450px", overflow: "auto" }}>
                                    <Typography gutterBottom variant="h5" component="div" className={styles.info_title}>
                                        Personal Information
                                    </Typography>
                                    
                                    <div variant="body2" style={{ display: 'flex', marginTop: '5px' }}>
                                        <FontAwesomeIcon icon={faEnvelope} className={styles.info_title_icon} />
                                        <div component="div" variant="body2">
                                            <h2 className={styles.info_title_tag}>Email</h2>
                                            <h1 className={styles.info_title_item}>{userData.accountEmail || ''}</h1>
                                        </div>
                                    </div>
                                    
                                    <div variant="body2" style={{ display: 'flex', marginTop: '5px' }}>
                                        <FontAwesomeIcon icon={faPhone} className={styles.info_title_icon} />
                                        <div component="div" variant="body2">
                                            <h2 className={styles.info_title_tag}>Phone</h2>
                                            <h1 className={styles.info_title_item}>{userData.customerPhone || ''}</h1>
                                        </div>
                                    </div>
                                    
                                    <div variant="body2" style={{ display: 'flex', marginTop: '5px' }}>
                                        <FontAwesomeIcon icon={faLocationDot} className={styles.info_title_icon} />
                                        <div component="div" variant="body2">
                                            <h2 className={styles.info_title_tag}>Location</h2>
                                            <h1 className={styles.info_title_item}>{userData.customerAddress || ''}</h1>
                                        </div>
                                    </div>

                                    <Typography gutterBottom variant="h5" component="div" className={styles.info_title} style={{marginTop: '30px'}}>
                                        Personal Details
                                    </Typography>
                                    
                                    <div variant="body2" style={{ display: 'flex', marginTop: '5px' }}>
                                        <FontAwesomeIcon icon={faVenusMars} className={styles.info_title_icon} />
                                        <div component="div" variant="body2">
                                            <h2 className={styles.info_title_tag}>Gender</h2>
                                            <h1 className={styles.info_title_item}>
                                                {typeof userData.customerGender === 'string' ? 
                                                    userData.customerGender === 'Male' || userData.customerGender === 'male' ? 'Male' : 'Female' 
                                                    : userData.customerGender === true ? 'Male' : 'Female'}
                                            </h1>
                                        </div>
                                    </div>
                                    
                                    <div variant="body2" style={{ display: 'flex', marginTop: '5px' }}>
                                        <FontAwesomeIcon icon={faCakeCandles} className={styles.info_title_icon} />
                                        <div component="div" variant="body2">
                                            <h2 className={styles.info_title_tag}>Date of birth</h2>
                                            <h1 className={styles.info_title_item}>{userData.dateOfBirth || ''}</h1>
                                        </div>
                                    </div>
                                    
                                    <div variant="body2" style={{ display: 'flex', marginTop: '5px' }}>
                                        <FontAwesomeIcon icon={faUserGroup} className={styles.info_title_icon} />
                                        <div component="div" variant="body2">
                                            <h2 className={styles.info_title_tag}>Account Type</h2>
                                            <h1 className={styles.info_title_item}>{userData.customerTypeName || ''}</h1>
                                        </div>
                                    </div>
                                </CardContent>
                            </Card>
                        </Grid>
                    </Grid>
                </Container>
            </div>
            <Footer />
        </div>
    )
}

export default Profile 