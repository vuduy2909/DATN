import React, { useState, useEffect, useRef } from 'react';
import { useLocation } from 'react-router-dom';
import styles from './ChartCircle.module.css';
import Chart from 'chart.js/auto';

const ChartCircle = ({ chartData }) => {
    const chartRefCircle = useRef(null);
    const chartInstanceCircle = useRef(null);
    const location = useLocation();
    const path = location.pathname;
    const [currentPage, setCurrentPage] = useState('');

    useEffect(() => {
        setCurrentPage(path);

        if (chartInstanceCircle.current) {
            chartInstanceCircle.current.destroy();
        }

        const myChartCircle = chartRefCircle.current.getContext('2d');
        
        // Dữ liệu mặc định
        let labels = ['Day 1-10', 'Day 10-20', 'Day 20-30'];
        let data = [40, 80, 60];
        
        // Sử dụng dữ liệu từ API nếu có
        if (chartData && chartData.data && chartData.data.length > 0) {
            labels = chartData.data.map(item => item.label);
            data = chartData.data.map(item => item.value);
        }

        chartInstanceCircle.current = new Chart(myChartCircle, {
            type: 'doughnut',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Table of customer exercise levels',
                    data: data,
                    fill: false,
                    backgroundColor: [
                        '#dd7969',
                        '#8fbed1',
                        '#edc45b',
                    ],
                    hoverOffset: 4
                }]
            },
            options: {
                cutout: '80%',
                plugins: {
                    legend: {
                        display: true,
                        position: 'bottom',
                    }
                }
            }
        });
    }, [path, chartData]);

    return (
        <div className={`${styles.chart} ${currentPage === '/user' ? styles.userChart : ''}`}>
            Track progress by day
            <canvas ref={chartRefCircle} />
        </div>
    );
};

export default ChartCircle;
