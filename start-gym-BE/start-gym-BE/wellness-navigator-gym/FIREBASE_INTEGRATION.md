# Firebase Integration Documentation - Updated Implementation

## Overview
This gym management system integrates with Firebase Storage using best practices from Firebase documentation to handle file uploads for various media types including user avatars, course images, exercise images, and exercise videos.

## 🔧 Firebase Configuration Best Practices

### Project Details
- **Project ID**: `capstone-project-2909`
- **Storage Bucket**: `capstone-project-2909.appspot.com` (Standard Firebase format)
- **Service Account Key**: Located at `src/main/resources/config/capstone-project-firebase-key.json`

### Implementation Improvements
Based on Firebase Storage best practices, the current implementation includes:

1. **Unified SDK Approach**: Uses Google Cloud Storage SDK consistently with Firebase Admin SDK initialization
2. **Proper URL Generation**: Follows Firebase Storage URL format with download tokens
3. **Enhanced Error Handling**: Comprehensive error diagnosis with specific Firebase error codes
4. **Connection Resilience**: Retry mechanisms with exponential backoff (1s→2s→4s delays)
5. **Timeout Configuration**: 30s connection, 60s read timeouts with 2-minute total timeout
6. **URL Validation**: Real-time accessibility testing and length validation (max 500 chars)

### URL Generation Strategy
The system now uses a two-tier URL generation approach:

#### Primary Method: Firebase Storage URLs with Download Tokens
```
Format: https://firebasestorage.googleapis.com/v0/b/{bucket}/o/{path}?alt=media&token={uuid}
Example: https://firebasestorage.googleapis.com/v0/b/capstone-project-2909.appspot.com/o/images%2FAvatar%2Favatar_john.jpg?alt=media&token=abc123-def456-ghi789
```
- **Benefits**: Secure, Firebase-native, supports access control
- **Use Case**: Default for all uploads
- **Length**: Typically 150-200 characters

#### Fallback Method: Google Cloud Storage Public URLs
```
Format: https://storage.googleapis.com/{bucket}/{path}
Example: https://storage.googleapis.com/capstone-project-2909.appspot.com/images/Avatar/avatar_john.jpg
```
- **Benefits**: Shorter URLs, permanently accessible
- **Use Case**: When download token method fails
- **Length**: Typically 80-120 characters

## 🗂️ Folder Structure
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

## 🚀 API Endpoints

### Customer Avatar Upload
**Endpoint**: `POST /api/v1/customer/upload-avatar`
- **Authentication**: User role required
- **File size**: Max 5MB
- **File types**: Images only (JPEG, PNG, GIF, WebP)
- **Naming convention**: `avatar_{username}.ext`
- **Storage location**: `images/Avatar/`
- **URL format**: Firebase Storage with download token

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
- **Auto-cleanup**: Deletes old image when uploading new one

### Course Image Upload by ID (Edit Mode)
**Endpoint**: `POST /api/v1/admin/courses/{courseId}/upload-image`
- **Authentication**: Admin role required
- **Parameters**: `file`
- **File size**: Max 5MB
- **File types**: Images only
- **Storage location**: `images/Course/{courseType}/{courseName}.ext`
- **Auto-cleanup**: Deletes ALL existing images for the course (any format)
- **Database update**: Automatically updates course.image field

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

## 🔧 Enhanced FirebaseStorageService Methods

### Core Upload Methods
1. **uploadAvatar(file, username)**: Uploads user avatar with automatic old file deletion
2. **uploadCourseImage(file, courseType, courseName, oldImageUrl)**: Uploads course image
3. **uploadExerciseImage(file, muscleGroup, exerciseName)**: Uploads exercise image
4. **uploadExerciseVideo(file, muscleGroup, exerciseName)**: Uploads exercise video

### New Helper Methods (Best Practices Implementation)
1. **generateFirebaseStorageUrl(filePath, downloadToken)**: Creates proper Firebase Storage URLs
2. **validateUrlAccessibility(url)**: Tests URL accessibility with proper HTTP handling
3. **createFirebaseStorageBlobInfo(filePath, contentType, downloadToken)**: Creates blob with Firebase metadata
4. **executeWithRetry(operation, operationName)**: Retry wrapper with exponential backoff

### Utility Methods
1. **deleteFile(filePath)**: Delete by Firebase Storage path
2. **deleteFileByUrl(downloadUrl)**: Delete by Firebase download URL
3. **isAvailable()**: Check if Firebase Storage is connected
4. **getCourseTypeFolder(courseTypeId)**: Get folder name for course type

## 📊 Enhanced Error Handling & Diagnostics

The improved implementation includes comprehensive error diagnosis:

### Common Error Types & Solutions
1. **Bucket Issues**: 
   - Error: "Bucket does not exist"
   - Solution: Verify bucket name format (`project-id.appspot.com`)

2. **Permission Issues**:
   - Error: "403 Forbidden" or "401 Unauthorized"
   - Solution: Check service account has Storage Admin role

3. **Network Issues**:
   - Error: "Connection timeout" or "Network error"
   - Solution: Check internet connectivity and Firebase endpoints

4. **Authentication Issues**:
   - Error: "Invalid credentials" or "Token expired"
   - Solution: Verify service account key file validity

5. **Quota Issues**:
   - Error: "Quota exceeded"
   - Solution: Check Firebase billing and storage limits

## 🛡️ Security Features

- **File type validation**: Strict MIME type checking
- **File size limits**: Configurable limits by file type
- **Authentication required**: All uploads require proper JWT tokens
- **Automatic cleanup**: Old files deleted when replacing
- **Download tokens**: Secure URLs with UUID tokens
- **Public fallback**: ACL-based public access when needed

## 📈 Performance Optimizations

1. **Connection Pooling**: Reuses HTTP connections for Firebase API calls
2. **Retry Logic**: Exponential backoff prevents system overload
3. **Timeout Management**: Prevents hanging requests
4. **Parallel Processing**: Multiple upload operations can run concurrently
5. **URL Caching**: Generated URLs cached for improved response times

## 🧪 Testing & Validation

### Automatic Validation
- File existence verification after upload
- URL accessibility testing before returning
- File size and type validation
- Connection health checks

### Manual Testing
Use the provided API endpoints to test:
```bash
# Test avatar upload
curl -X POST http://localhost:8080/api/v1/customer/upload-avatar \
  -H "Authorization: Bearer {token}" \
  -F "file=@avatar.jpg"

# Test course image upload  
curl -X POST http://localhost:8080/api/v1/admin/courses/upload-image \
  -H "Authorization: Bearer {admin_token}" \
  -F "file=@course.jpg" \
  -F "courseType=1_Strength" \
  -F "courseName=Basic Training"
```

## 🔍 Monitoring & Debugging

### Enhanced Logging
All Firebase operations include:
- ✅ Detailed operation tracking with unique IDs
- ✅ Performance metrics (upload time, file size, URL generation time)
- ✅ Error stack traces with context
- ✅ Network connectivity status
- ✅ Retry attempt logging with delays

### Health Check Endpoint
```
GET /api/v1/admin/firebase/health
```
Returns Firebase Storage connectivity status and bucket information.

## 🎯 Implementation Status

✅ **Configuration**: Firebase properly configured with standard bucket format  
✅ **URL Generation**: Implements Firebase Storage best practices for URL generation  
✅ **Error Handling**: Comprehensive error diagnosis and recovery  
✅ **Connection Resilience**: Retry mechanisms with exponential backoff  
✅ **Validation**: Real-time URL accessibility testing  
✅ **Security**: Proper authentication and file validation  
✅ **Performance**: Optimized for high-concurrency uploads  
✅ **Monitoring**: Enhanced logging and health checks  

## 🚀 Production Readiness

The Firebase integration is now production-ready with:
1. ✅ **Reliability**: Robust error handling and retry mechanisms
2. ✅ **Security**: Comprehensive validation and authentication
3. ✅ **Performance**: Optimized for scale with proper timeouts
4. ✅ **Monitoring**: Detailed logging for troubleshooting
5. ✅ **Best Practices**: Follows Firebase Storage official guidelines

No additional setup required - the system implements Firebase Storage best practices! 🎉 