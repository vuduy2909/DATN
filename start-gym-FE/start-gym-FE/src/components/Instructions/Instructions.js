import React from 'react';
import styles from './Instructions.module.css';

const Instructions = ({ exercise }) => {
    // Hiển thị instructions thay vì exerciseDescription
    const separatedInstructions = exercise && exercise.instructions ? exercise.instructions.split('.').filter(sentence => sentence.trim() !== '') : [];

    return (
        <div className={styles.tab_info}>
            <div>
                <p><span className={styles.video_author}>Body target: </span><span>{exercise.target}</span></p>
                <p><span className={styles.video_author}>Body part: </span><span>{exercise.bodyPart}</span></p>
                <p><span className={styles.video_author}>Equipment: </span><span>{exercise.equipment}</span></p>
                <p>How you can do that:</p>
                <ol>
                    {separatedInstructions?.map((sentence, index) => (
                        <li key={index}>{sentence.trim()}</li>
                    ))}
                </ol>
            </div>
        </div>
    );
};

export default Instructions;
