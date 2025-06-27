import React, { useEffect, useRef } from 'react'
import styles from './ChartLine.module.css'
import Chart from 'chart.js/auto';

const ChartLine = ({ chartData }) => {

    const chartRef = useRef(null);
    const chartInstance = useRef(null);

    useEffect(() => {
        if (chartInstance.current) {
            chartInstance.current.destroy()
        }
        const myChartRef = chartRef.current.getContext('2d');

        // Dữ liệu mặc định
        let labels = ['Week 1', 'Week 2', 'Week 3', 'Week 4'];
        let data = [65, 75, 45, 80];
        
        // Sử dụng dữ liệu từ API nếu có
        if (chartData && chartData.data && chartData.data.length > 0) {
            labels = chartData.data.map(item => item.day);
            data = chartData.data.map(item => item.activity);
        }

        chartInstance.current = new Chart(myChartRef, {
            type: 'line',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Weekly Activity (%)',
                    data: data,
                    fill: true,
                    backgroundColor: 'rgba(207, 64, 68, 0.1)',
                    borderColor: '#CF4044',
                    pointBackgroundColor: '#CF4044',
                    pointBorderColor: '#fff',
                    pointBorderWidth: 2,
                    pointRadius: 6,
                    pointHoverRadius: 8,
                    tension: 0.4
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: true,
                        position: 'top',
                        labels: {
                            color: '#2c3e50',
                            font: {
                                size: 12,
                                weight: 'bold'
                            }
                        }
                    },
                    tooltip: {
                        backgroundColor: 'rgba(44, 62, 80, 0.9)',
                        titleColor: '#fff',
                        bodyColor: '#fff',
                        borderColor: '#CF4044',
                        borderWidth: 1,
                        callbacks: {
                            label: function(context) {
                                return `Activity: ${context.parsed.y}%`;
                            }
                        }
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        max: 100,
                        ticks: {
                            callback: function(value) {
                                return value + '%';
                            },
                            color: '#64748b',
                            font: {
                                size: 11
                            }
                        },
                        grid: {
                            color: 'rgba(0, 0, 0, 0.1)',
                            drawBorder: false
                        }
                    },
                    x: {
                        ticks: {
                            color: '#64748b',
                            font: {
                                size: 11,
                                weight: 'bold'
                            }
                        },
                        grid: {
                            display: false
                        }
                    }
                },
                interaction: {
                    intersect: false,
                    mode: 'index'
                }
            }
        })
    }, [chartData])

    return (
        <div style={{ height: '300px', position: 'relative' }}>
            <canvas ref={chartRef} className={styles.chart} />
        </div>
    )
}

export default ChartLine