# Firebase Integration Documentation

## Overview
This gym management system integrates with Firebase Storage to handle file uploads for various media types including user avatars, course images, exercise images, and exercise videos.

## Firebase Configuration

### Project Details
- **Project ID**: `capstone-project-2909`
- **Storage Bucket**: `capstone-project-2909.appspot.com`
- **Service Account Key**: Located at `src/main/resources/config/capstone-project-firebase-key.json`

### Folder Structure
Firebase Storage is organized with the following folder structure:

```
Firebase Storage Root
├── images/
│   ├── Avatar/                    # User profile avatars
│   │   └── avatar_{username}.ext  # Format: avatar_username.jpg
│   ├── Course/                    # Course cover images by category
│   │   ├── 1_Strength/
│   │   ├── 2_Cardiovascular/
│   │   ├── 3_Flexibility/
│   │   ├── 4_StrengthEndur/
│   │   ├── 5_MindBody/
│   │   ├── 6_Functional/
│   │   └── 7_WeightLoss/
│   └── Exercise/                  # Exercise images by muscle group
│       ├── Abs/
│       ├── Back/
│       ├── Biceps/
│       ├── Calves/
│       ├── Chest/
│       ├── Deltoid/
│       ├── Lats/
│       ├── Legs/
│       └── Triceps/
└── videos/
    └── Exercise/                  # Exercise videos by muscle group
        ├── Abs/
        ├── Back/
        ├── Biceps/
        ├── Calves/
        ├── Chest/
        ├── Deltoid/
        ├── Lats/
        ├── Legs/
        └── Triceps/
```

## API Endpoints

### Customer Avatar Upload
**Endpoint**: `POST /api/v1/customer/upload-avatar`
- **Authentication**: User role required
- **File size**: Max 5MB
- **File types**: Images only (JPEG, PNG, GIF, WebP)
- **Naming convention**: `avatar_{username}.ext`
- **Storage location**: `images/Avatar/`

### Admin Customer Avatar Upload
**Endpoint**: `POST /api/v1/admin/customers/upload-avatar`
- **Authentication**: Admin role required
- **Parameters**: `file`, `username`
- **File size**: Max 5MB
- **File types**: Images only

### Course Image Upload
**Endpoint**: `POST /api/v1/admin/courses/upload-image`
- **Authentication**: Admin role required
- **Parameters**: `file`, `courseType`, `courseName`
- **File size**: Max 5MB
- **File types**: Images only
- **Storage location**: `images/Course/{courseType}/`

### Exercise Image Upload
**Endpoint**: `POST /api/v1/admin/exercises/upload-image`
- **Authentication**: Admin role required
- **Parameters**: `file`, `muscleGroup`, `exerciseName`
- **File size**: Max 10MB
- **File types**: Images only
- **Storage location**: `images/Exercise/{muscleGroup}/`

### Exercise Video Upload
**Endpoint**: `POST /api/v1/admin/exercises/upload-video`
- **Authentication**: Admin role required
- **Parameters**: `file`, `muscleGroup`, `exerciseName`
- **File size**: Max 100MB
- **File types**: Videos only (MP4, WebM, AVI, MOV)
- **Storage location**: `videos/Exercise/{muscleGroup}/`

### Get Muscle Groups
**Endpoint**: `GET /api/v1/admin/exercises/muscle-groups`
- **Returns**: List of available muscle groups for categorization

## Course Type Mapping
The system maps course type IDs to folder names:
- 1 → "1_Strength"
- 2 → "2_Cardiovascular"
- 3 → "3_Flexibility"
- 4 → "4_StrengthEndur"
- 5 → "5_MindBody"
- 6 → "6_Functional"
- 7 → "7_WeightLoss"

## Muscle Groups
Available muscle groups for exercise categorization:
- Abs
- Back
- Biceps
- Calves
- Chest
- Deltoid
- Lats
- Legs
- Triceps

## FirebaseStorageService Methods

### Core Upload Methods
1. **uploadAvatar(file, username)**: Uploads user avatar with automatic old file deletion
2. **uploadCourseImage(file, courseType, courseName, oldImageUrl)**: Uploads course image
3. **uploadExerciseImage(file, muscleGroup, exerciseName)**: Uploads exercise image
4. **uploadExerciseVideo(file, muscleGroup, exerciseName)**: Uploads exercise video

### Utility Methods
1. **deleteFile(filePath)**: Delete by Firebase Storage path
2. **deleteFileByUrl(downloadUrl)**: Delete by Firebase download URL
3. **isAvailable()**: Check if Firebase Storage is connected
4. **getCourseTypeFolder(courseTypeId)**: Get folder name for course type

## File Naming Conventions

### Avatars
- Format: `avatar_{username}.{extension}`
- Example: `avatar_john_doe.jpg`

### Course Images
- Format: `{sanitized_course_name}.{extension}`
- Example: `strength_training_basics.jpg`

### Exercise Media
- Format: `{sanitized_exercise_name}.{extension}`
- Example: `push_ups.jpg`, `deadlift_tutorial.mp4`

## Error Handling
All upload endpoints include comprehensive error handling for:
- Empty files
- File size limits
- Invalid file types
- Firebase connection issues
- Upload failures

## Security Features
- File type validation
- File size limits
- Authentication required for all uploads
- Automatic deletion of old files when replacing
- Signed URLs with long expiration (10 years)

## Frontend Integration
The system provides a complete frontend integration with:
- Direct Firebase uploads from React components
- Automatic old file deletion
- Progress tracking
- Error handling
- File validation

## Usage Examples

### Upload Customer Avatar (User)
```javascript
const formData = new FormData();
formData.append('file', avatarFile);

const response = await axiosInstance.post('/customer/upload-avatar', formData, {
  headers: { 'Content-Type': 'multipart/form-data' }
});
```

### Upload Course Image (Admin)
```javascript
const formData = new FormData();
formData.append('file', imageFile);
formData.append('courseType', '1_Strength');
formData.append('courseName', 'Strength Training Basics');

const response = await axiosInstance.post('/admin/courses/upload-image', formData);
```

### Upload Exercise Video (Admin)
```javascript
const formData = new FormData();
formData.append('file', videoFile);
formData.append('muscleGroup', 'Chest');
formData.append('exerciseName', 'Push Ups Tutorial');

const response = await axiosInstance.post('/admin/exercises/upload-video', formData);
```

## Troubleshooting

### Common Issues
1. **Firebase not available**: Check service account key and configuration
2. **Upload fails**: Verify file size and type restrictions
3. **Permission denied**: Ensure proper authentication
4. **File not found**: Check Firebase Storage rules

### Logs
All Firebase operations are logged with detailed information for debugging purposes. 