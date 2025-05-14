import React from 'react';
import styles from './Banner.module.css';
import { useAuth } from '../../AuthContext'
import runner from '../../images/runner.png'
import eclipse from '../../images/Ellipse_Banner.png'
import star from '../../images/Star.png'
import cal from '../../images/circle_cal.png'
import { Link } from 'react-router-dom';

import ChartCircle from '../ChartCircle/ChartCircle';

const Banner = () => {
    const { user } = useAuth();
    var isLogin = false;
    if (user) {
        isLogin = true;
    }

    return (
        <div className={styles.banner_container}>
            <div className={styles.banner_content}>
                <div className={styles.bn_left}>
                    <div className={styles.bn_left_1}>
                        <p className={styles.typo}>WORKOUT MAKE YOUR</p>
                        <p className={styles.typo}><span className={styles.typo_red}>BODY </span>SHAPE</p>
                    </div>

                    <div className={styles.bn_left_2}>
                        <p className={styles.typo_des}>Discover your strongest self through the power of fitness. Join us and unlock a stronger, healthier you.</p>
                    </div>

                    <Link to="/recommend" style={{ textDecoration: 'none', color: '#fff' }} className={styles.bn_left_btn}>
                        RECOMMEND
                    </Link>
                </div>
                
                <div className={styles.bn_center}>
                    <img alt="eclipse" src={eclipse} className={styles.eclipse} />
                    <img alt="runner" src={runner} className={styles.runner} />
                </div>
                
                <div className={styles.bn_right}>
                    <div className={styles.rating_container}>
                        <div className={styles.rating}>
                            <div className={styles.rt}>
                                <img alt="star" src={star} className={styles.star} />
                                <p className={styles.number_rating}>4.8</p>
                            </div>
                            <p className={styles.rating_title}>Average customer rating</p>
                        </div>
                    </div>
                    
                    <div className={styles.chart_container}>
                        <p className={styles.chart_title}>Track progress by day</p>
                        <div className={styles.circle}>
                            {isLogin ? (
                                <ChartCircle />
                            ) : (
                                <img alt="calendar" src={cal} className={styles.cal_img} />
                            )}
                        </div>

                        <div className={styles.day_labels}>
                            <div className={styles.day_label}>
                                <span className={styles.day_color_1}></span>
                                <span className={styles.day_text}>Day 1-10</span>
                            </div>
                            <div className={styles.day_label}>
                                <span className={styles.day_color_2}></span>
                                <span className={styles.day_text}>Day 10-20</span>
                            </div>
                            <div className={styles.day_label}>
                                <span className={styles.day_color_3}></span>
                                <span className={styles.day_text}>Day 20-30</span>
                            </div>
                        </div>
                    </div>
                    
                    <div className={styles.stats_container}>
                        <div className={styles.stats_icons}>
                            <div className={styles.icon_runner}></div>
                            <div className={styles.icon_smile}></div>
                            <div className={styles.icon_weight}></div>
                        </div>
                        <div className={styles.stats_row}>
                            <div className={styles.stats_item}>
                                <div className={styles.stats_text}>
                                    <p className={styles.stats_number}>235</p>
                                    <p className={styles.stats_label}>CUSTOMER</p>
                                </div>
                            </div>
                            <div className={styles.stats_item}>
                                <div className={styles.stats_text}>
                                    <p className={styles.stats_number}>44</p>
                                    <p className={styles.stats_label}>HAPPY CLIENTS</p>
                                </div>
                            </div>
                            <div className={styles.stats_item}>
                                <div className={styles.stats_text}>
                                    <p className={styles.stats_number}>745</p>
                                    <p className={styles.stats_label}>EXPERIENCE</p>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    )
}

export default Banner