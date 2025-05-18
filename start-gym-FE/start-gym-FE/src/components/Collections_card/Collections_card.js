import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import styles from "./Collections_card.module.css";
import Card from "react-bootstrap/Card";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { collectionService } from "../../service/collectionService";
import { faStar } from "@fortawesome/free-solid-svg-icons";
import Button from "@mui/material/Button";
import { CircularProgress } from "@mui/material";

const CollectionCard = ({ id, title, image, duration }) => {
  const renderRating = () => {
    const stars = [];
    const ratingValue = parseInt(duration) || 3; // Default to 3 if can't parse
    
    for (let i = 1; i <= 5; i++) {
      if (i <= ratingValue) {
        stars.push(
          <span key={i}>
            <FontAwesomeIcon icon={faStar} key={i} className={styles.star} />
          </span>
        );
      } else {
        stars.push(
          <span key={i}>
            <FontAwesomeIcon
              icon={faStar}
              key={stars.length}
              className={styles.star}
            />
          </span>
        );
      }
    }

    return stars;
  };

  return (
    <Card key={id} className={styles.collection_card}>
      <Card.Img variant="top" src={image || 'https://via.placeholder.com/300x180?text=Course+Image'} className={styles.img} />
      <Card.Body className={styles.card_body}>
        <Card.Title>{title}</Card.Title>
        <div className={styles.rating}>
          {renderRating()}
          <p style={{ marginLeft: "10px" }}>5 rating</p>
        </div>
        <svg
          className={styles.line}
          xmlns="http://www.w3.org/2000/svg"
          width="414"
          height="2"
          viewBox="0 0 414 2"
          fill="none"
        >
          <path d="M0 1H413.5" stroke="#6A6B6C" strokeWidth="0.5" />
        </svg>
        <div className={styles.footer}>
          <p className={styles.collection_duration}>Duration: {duration || 'Unknown'}</p>
          <Link to={`/collections/${id}/days/1`} className={styles.link}>
            <button className={styles.btn}>Join now</button>
          </Link>
        </div>
      </Card.Body>
    </Card>
  );
};

const Collections_card = ({ selectedCategory, recommendedCourses, loading }) => {
  const [dataCourse, setDataCourse] = useState([]);
  const [loadingCourses, setLoadingCourses] = useState(true);
  const [error, setError] = useState(null);
  const [filteredCards, setFilteredCards] = useState([]);

  const getCardsFunction = async () => {
    try {
      setLoadingCourses(true);
      setError(null);
      console.log("Starting to fetch courses...");
      const cards = await collectionService.getCards();
      console.log("API Response:", cards);
      
      if (!cards) {
        console.error("No cards data received from API");
        setError("No courses data received");
        return;
      }
      
      if (!Array.isArray(cards)) {
        console.error("Received data is not an array:", cards);
        setError("Invalid courses data format");
        return;
      }

      if (cards.length === 0) {
        console.log("Received empty courses array");
        setError("No courses available");
        return;
      }

      console.log("Successfully loaded", cards.length, "courses");
      setDataCourse(cards);
    } catch (error) {
      console.error("Error details:", {
        message: error.message,
        stack: error.stack,
        response: error.response
      });
      setError(error.message || "Error loading courses");
      setDataCourse([]);
    } finally {
      setLoadingCourses(false);
    }
  };

  // Load courses when component mounts
  useEffect(() => {
    getCardsFunction();
  }, []);

  // Filter courses whenever the category changes or courses/recommended courses update
  useEffect(() => {
    if (!dataCourse || dataCourse.length === 0) return;
    
    console.log("Filtering with category:", selectedCategory);
    console.log("Available courses:", dataCourse);
    console.log("Recommended courses:", recommendedCourses);
    
    let filtered;
    
    if (selectedCategory === "Recommend") {
      // Force include course with ID 5 (Core Power Development)
      const course5 = dataCourse.find(course => course.courseId === 5);
      if (course5) {
        console.log("Found Core Power Development course:", course5);
        filtered = [course5];
      } else {
        console.log("Course ID 5 not found in available courses");
        filtered = [];
      }
      
      // Also include any other recommended courses if available
      if (recommendedCourses && recommendedCourses.length > 0) {
        const otherRecommendedCourses = dataCourse.filter(course => 
          recommendedCourses.some(rec => rec.courseId === course.courseId) && course.courseId !== 5
        );
        filtered = [...filtered, ...otherRecommendedCourses];
      }
      
      console.log("Final recommended courses:", filtered);
    } else if (selectedCategory === "All") {
      filtered = dataCourse;
    } else {
      filtered = dataCourse.filter(course => {
        const typeName = course.courseType?.courseTypeName;
        return typeName === selectedCategory;
      });
    }
    
    setFilteredCards(filtered || []);
  }, [selectedCategory, dataCourse, recommendedCourses]);

  if (loading || loadingCourses) {
    return (
      <div className={styles.loading_container}>
        <CircularProgress color="error" />
        <p>Đang tải khóa học...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className={styles.error_container}>
        <p className={styles.error_message}>{error}</p>
        <Button 
          variant="contained" 
          color="error" 
          onClick={getCardsFunction}
          style={{ marginTop: '10px' }}
        >
          Thử lại
        </Button>
      </div>
    );
  }

  const isRecommendWithNoData = selectedCategory === "Recommend" && filteredCards.length === 0;

  return (
    <div className={styles.collection}>
      {filteredCards.length > 0 ? (
        filteredCards.map((course) => (
          <CollectionCard
            key={course.courseId}
            id={course.courseId}
            title={course.courseName}
            description={course.description}
            duration={course.duration}
            image={course.image}
          />
        ))
      ) : (
        <div className={styles.bn_left}>
          <div className={styles.bn_left_1}>
            <p className={styles.typo}>
              <span className={styles.typo_red}>No</span> {isRecommendWithNoData ? "recommended" : ""} courses found
            </p>
            <p className={styles.typo_des}>
              <Button variant="contained" className={styles.bn_left_btn}>
                <Link to="/recommend" style={{ color: "#fff" }}>
                  Start now
                </Link>
              </Button>
              <span>to receive personalized recommendations.</span>
            </p>
          </div>
        </div>
      )}
    </div>
  );
};

export default Collections_card;
