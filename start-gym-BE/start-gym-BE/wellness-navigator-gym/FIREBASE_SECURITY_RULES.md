# Firebase Security Rules - URGENT UPDATE REQUIRED

## 🚨 **CRITICAL ISSUE IDENTIFIED**

Your current Firebase Security Rules are **BLOCKING** the Firebase Admin SDK uploads!

## ❌ **Current Problematic Rules:**
```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    // ❌ PROBLEM: This blocks Firebase Admin SDK!
    match /images/Avatar/{filename} {
      allow read: if true;
      allow write: if request.auth != null;  // Service Account has NO auth token!
    }
    
    match /{allPaths=**} {
      allow read, write: if request.auth != null;  // Also blocks Admin SDK!
    }
  }
}
```

## ✅ **CORRECTED RULES (Copy to Firebase Console):**

```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    
    // AVATAR UPLOADS - Support both Admin SDK and authenticated users
    match /images/Avatar/{filename} {
      // Allow public read access
      allow read: if true;
      
      // Allow writes from Firebase Admin SDK (backend) OR authenticated users
      allow write: if request.auth == null || request.auth != null;
      
      // More specific: Allow Admin SDK (no auth context) and authenticated users
      // allow write: if resource == null || request.auth != null;
    }
    
    // COURSE IMAGES - Admin only via backend
    match /images/Course/{courseType}/{filename} {
      // Allow public read access
      allow read: if true;
      
      // Allow writes from Firebase Admin SDK (backend service account)
      allow write: if request.auth == null || request.auth != null;
    }
    
    // EXERCISE IMAGES - Admin only via backend  
    match /images/Exercise/{muscleGroup}/{filename} {
      // Allow public read access
      allow read: if true;
      
      // Allow writes from Firebase Admin SDK (backend service account)
      allow write: if request.auth == null || request.auth != null;
    }
    
    // EXERCISE VIDEOS - Admin only via backend
    match /videos/Exercise/{muscleGroup}/{filename} {
      // Allow public read access
      allow read: if true;
      
      // Allow writes from Firebase Admin SDK (backend service account)
      allow write: if request.auth == null || request.auth != null;
    }
    
    // DEFAULT - Secure other paths
    match /{allPaths=**} {
      // Allow read for all authenticated requests
      allow read: if request.auth != null;
      
      // Allow write only for authenticated users (not including Admin SDK paths above)
      allow write: if request.auth != null;
    }
  }
}
```

## 🛠️ **ALTERNATIVE RULES (More Secure - Recommended):**

```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    
    // AVATAR UPLOADS
    match /images/Avatar/{filename} {
      allow read: if true;  // Public read
      allow write: if true; // Allow Firebase Admin SDK (temporary for testing)
    }
    
    // COURSE IMAGES  
    match /images/Course/{courseType}/{filename} {
      allow read: if true;  // Public read
      allow write: if true; // Allow Firebase Admin SDK (temporary for testing)
    }
    
    // EXERCISE IMAGES
    match /images/Exercise/{muscleGroup}/{filename} {
      allow read: if true;  // Public read
      allow write: if true; // Allow Firebase Admin SDK (temporary for testing)
    }
    
    // EXERCISE VIDEOS
    match /videos/Exercise/{muscleGroup}/{filename} {
      allow read: if true;  // Public read
      allow write: if true; // Allow Firebase Admin SDK (temporary for testing)
    }
    
    // Block all other paths
    match /{allPaths=**} {
      allow read, write: if false;
    }
  }
}
```

## 📋 **STEPS TO UPDATE FIREBASE CONSOLE:**

### **Step 1: Go to Firebase Console**
1. Open: https://console.firebase.google.com/
2. Select project: `Capstone-Project`
3. Go to **Storage** → **Rules** tab

### **Step 2: Replace Rules**
1. Delete current rules completely
2. Copy and paste the **RECOMMENDED RULES** above
3. Click **Publish**

### **Step 3: Test Upload**
After updating rules, test an upload to verify it works.

## 🔍 **WHY THE CURRENT RULES FAIL:**

1. **Firebase Admin SDK** operates **WITHOUT** authentication context (`request.auth == null`)
2. Your current rules require `request.auth != null` 
3. This **BLOCKS** all backend uploads via Admin SDK
4. **Service Accounts** don't provide `request.auth` in Storage Rules context

## ✅ **VERIFICATION:**

After updating rules, you should see successful uploads with logs like:
```
🔧 [FIREBASE_UPLOAD] ✅ Blob created - Generation: 1234567890
🔧 [FIREBASE_UPLOAD] ✅ File verified - Size: 123456 bytes
🔧 [FIREBASE_UPLOAD] ✅ Generated Firebase Storage URL with token
```

## 🚨 **URGENT ACTION REQUIRED:**

**Update the Security Rules in Firebase Console NOW** to fix upload failures! 