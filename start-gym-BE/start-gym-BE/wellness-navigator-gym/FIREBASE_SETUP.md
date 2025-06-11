# Firebase Setup Guide

## 🔥 Firebase Integration with Gym Management System

This project integrates with Firebase Storage for file upload management including avatars, course images, exercise images, and videos.

## ✅ What's Already Configured

### 1. Firebase Configuration
- Firebase project: `capstone-project-2909`
- Service account key: `src/main/resources/config/capstone-project-firebase-key.json`
- Storage bucket: `capstone-project-2909.appspot.com`

### 2. Backend Services
- `FirebaseStorageService`: Complete implementation for file uploads
- API endpoints for all upload types
- Automatic file deletion and replacement
- File validation and size limits

### 3. Folder Structure
The system automatically organizes files in Firebase Storage:
```
📁 images/
   📁 Avatar/ (avatar_username.ext)
   📁 Course/ (organized by course type)
   📁 Exercise/ (organized by muscle group)
📁 videos/
   📁 Exercise/ (organized by muscle group)
```

## 🚀 How to Use

### For Users (Customer Avatar Upload)
```
Endpoint: POST /api/v1/customer/upload-avatar
Authentication: User token required
File limit: 5MB
Supported: Images only
```

### For Admins (Course Images)
```
Endpoint: POST /api/v1/admin/courses/upload-image
Parameters: file, courseType, courseName
File limit: 5MB
Supported: Images only
```

### For Admins (Exercise Media)
```
Images: POST /api/v1/admin/exercises/upload-image
Videos: POST /api/v1/admin/exercises/upload-video
Parameters: file, muscleGroup, exerciseName
File limits: 10MB (images), 100MB (videos)
```

## 🔧 Technical Implementation

### Service Class
`FirebaseStorageService` handles all Firebase operations:
- ✅ Upload with automatic naming
- ✅ Delete old files when replacing
- ✅ Generate signed URLs (10-year expiry)
- ✅ Error handling and logging

### File Naming Conventions
- **Avatars**: `avatar_{username}.ext`
- **Courses**: `{course_name}.ext`
- **Exercises**: `{exercise_name}.ext`

### Course Type Mapping
- 1 → "1_Strength"
- 2 → "2_Cardiovascular"
- 3 → "3_Flexibility"
- 4 → "4_StrengthEndur"
- 5 → "5_MindBody"
- 6 → "6_Functional"
- 7 → "7_WeightLoss"

### Muscle Groups
Abs, Back, Biceps, Calves, Chest, Deltoid, Lats, Legs, Triceps

## 📝 API Usage Examples

### Upload Avatar (Customer)
```bash
curl -X POST \
  http://localhost:8080/api/v1/customer/upload-avatar \
  -H "Authorization: Bearer {user_token}" \
  -F "file=@avatar.jpg"
```

### Upload Course Image (Admin)
```bash
curl -X POST \
  http://localhost:8080/api/v1/admin/courses/upload-image \
  -H "Authorization: Bearer {admin_token}" \
  -F "file=@course_image.jpg" \
  -F "courseType=1_Strength" \
  -F "courseName=Basic Strength Training"
```

### Upload Exercise Video (Admin)
```bash
curl -X POST \
  http://localhost:8080/api/v1/admin/exercises/upload-video \
  -H "Authorization: Bearer {admin_token}" \
  -F "file=@exercise_video.mp4" \
  -F "muscleGroup=Chest" \
  -F "exerciseName=Push Up Tutorial"
```

## 🛡️ Security & Validation

### File Validation
- ✅ File type checking (images/videos only)
- ✅ File size limits (5MB-100MB depending on type)
- ✅ Content type validation
- ✅ Empty file rejection

### Authentication
- ✅ User role for avatar uploads
- ✅ Admin role for course/exercise uploads
- ✅ JWT token validation

### Auto-cleanup
- ✅ Old files deleted when replacing
- ✅ Handles Firebase URL extraction
- ✅ Graceful error handling if deletion fails

## 📊 Response Format

### Success Response
```json
{
  "imageUrl": "https://firebasestorage.googleapis.com/...",
  "message": "File uploaded successfully to Firebase Storage",
  "status": "success",
  "timestamp": "2025-01-11T14:54:39"
}
```

### Error Response
```json
{
  "message": "File size exceeds limit",
  "status": "error",
  "timestamp": "2025-01-11T14:54:39"
}
```

## 🔍 Monitoring & Debugging

### Logging
All Firebase operations are logged with:
- ✅ Upload success/failure
- ✅ File deletion operations
- ✅ Error details and stack traces
- ✅ Firebase connectivity status

### Health Check
```
GET /api/v1/admin/test-connection
```

## 🎯 Integration Status

✅ **Backend**: Fully implemented and ready  
✅ **Firebase**: Connected and configured  
✅ **File Upload**: All endpoints functional  
✅ **Validation**: Complete with size/type limits  
✅ **Security**: Authentication and authorization in place  
✅ **Auto-cleanup**: Old file deletion working  
✅ **Error Handling**: Comprehensive error responses  

## 🚦 Next Steps

The Firebase integration is complete and ready for use. You can now:
1. Upload customer avatars via the user interface
2. Upload course images via admin interface
3. Upload exercise images and videos via admin interface
4. All files are automatically organized in the proper folder structure
5. Old files are automatically deleted when replaced

No additional setup required - the system is production ready! 🎉 