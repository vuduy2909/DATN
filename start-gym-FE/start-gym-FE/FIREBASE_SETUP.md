# Firebase Setup Instructions

## Bước 1: Lấy thông tin cấu hình Firebase

1. Truy cập [Firebase Console](https://console.firebase.google.com/)
2. Chọn project `capstone-project-2909`
3. Vào **Project Settings** (bánh răng cạnh Project Overview)
4. Scroll xuống phần **Your apps**
5. Chọn **Web app** hoặc tạo mới nếu chưa có
6. Copy các thông tin sau:
   - **API Key**
   - **App ID**

## Bước 2: Cập nhật Firebase Config

Sửa file `src/config/firebaseConfig.js`:

```javascript
const firebaseConfig = {
  apiKey: "YOUR_ACTUAL_API_KEY_HERE",           // Thay bằng API key thực tế
  authDomain: "capstone-project-2909.firebaseapp.com",
  projectId: "capstone-project-2909",
  storageBucket: "capstone-project-2909.appspot.com",
  messagingSenderId: "101205198065409096792",
  appId: "YOUR_ACTUAL_APP_ID_HERE"              // Thay bằng App ID thực tế
};
```

## Bước 3: Cấu hình Firebase Storage Rules

Vào **Storage** trong Firebase Console và cập nhật rules:

```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    // Allow read access to all users
    match /{allPaths=**} {
      allow read: if true;
    }
    
    // Allow write access for authenticated users
    match /{allPaths=**} {
      allow write: if request.auth != null;
    }
    
    // For admin uploads (temporary - should use backend)
    match /images/{allPaths=**} {
      allow write: if true;
    }
    
    match /videos/{allPaths=**} {
      allow write: if true;
    }
  }
}
```

## Bước 4: Cấu trúc thư mục Firebase Storage

```
images/
  Avatar/                   # Customer avatars
    avatar_username.jpg
  Course/                   # Course images
    1_Strength/
    2_Cardiovascular/
    3_Flexibility/
    4_StrengthEndur/
    5_MindBody/
    6_Functional/
    7_WeightLoss/
  Exercise/                 # Exercise images (if needed)
    Abs/
    Back/
    Biceps/
    Calves/
    Chest/
    Deltoid/
    Lats/
    Legs/
    Triceps/

videos/
  Exercise/                 # Exercise videos
    Abs/
    Back/
    Biceps/
    Calves/
    Chest/
    Deltoid/
    Lats/
    Legs/
    Triceps/
```

## Bước 5: Test Upload

1. Chạy ứng dụng
2. Vào Admin > Edit Customer
3. Thử upload avatar
4. Vào Admin > Edit Exercise  
5. Thử upload video

## Lưu ý bảo mật

- API Key và App ID nên được lưu trong file `.env`
- Không commit file `.env` lên git
- Cấu hình Storage Rules chặt chẽ hơn cho production
- Sử dụng Firebase Admin SDK từ backend để upload an toàn hơn 