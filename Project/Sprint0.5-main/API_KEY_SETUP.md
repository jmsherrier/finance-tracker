# API Key Setup Instructions

## Getting Your Hugging Face API Key (FREE)

1. **Go to Hugging Face**: https://huggingface.co/
2. **Create an account** (if you don't have one) - it's free!
3. **Go to Settings**: Click on your profile → Settings
4. **Access Tokens**: Navigate to "Access Tokens" in the left sidebar
5. **Create New Token**: 
   - Click "New token"
   - Give it a name (e.g., "Android Chatbot")
   - Select "Read" permissions (that's all we need)
   - Click "Generate token"
6. **Copy the token** - You'll see something like: `hf_xxxxxxxxxxxxxxxxxxxxxxxxxxxxx`

## Quick Start: Hardcode for Testing (Easiest)

**For quick testing**, you can temporarily hardcode the API key:

1. Open `Project/Sprint0.5-main/app/src/main/java/com/example/sprintproject/view/ChatbotFragment.java`
2. Find the `getApiKeyFromProperties()` method (around line 70)
3. Uncomment and modify this line:
   ```java
   return "hf_your_actual_api_key_here";
   ```
   Replace `hf_your_actual_api_key_here` with your actual token.

4. The method should look like:
   ```java
   private String getApiKeyFromProperties() {
       return "hf_xxxxxxxxxxxxxxxxxxxxxxxxxxxxx"; // Your actual key
   }
   ```

**⚠️ IMPORTANT**: Remove the hardcoded key before committing to git!

## Proper Setup: Using local.properties (Recommended)

### Step 1: Add to local.properties

1. Open `Project/Sprint0.5-main/local.properties`
2. Add this line:
   ```
   HUGGING_FACE_API_KEY=your_api_key_here
   ```
   Replace `your_api_key_here` with your actual token.

3. The file should look like:
   ```
   sdk.dir=C\:\\Users\\nehal\\AppData\\Local\\Android\\Sdk
   HUGGING_FACE_API_KEY=hf_xxxxxxxxxxxxxxxxxxxxxxxxxxxxx
   ```

**Note**: `local.properties` is already in `.gitignore`, so your API key won't be committed to git.

### Step 2: Update build.gradle to Read from local.properties

Add this to `Project/Sprint0.5-main/app/build.gradle` (in the `android` block, before `buildTypes`):

```gradle
android {
    // ... existing code ...
    
    // Read API key from local.properties
    def localProperties = new Properties()
    def localPropertiesFile = rootProject.file('local.properties')
    if (localPropertiesFile.exists()) {
        localPropertiesFile.withInputStream { localProperties.load(it) }
    }
    
    def huggingFaceApiKey = localProperties.getProperty('HUGGING_FACE_API_KEY', '')
    
    defaultConfig {
        // ... existing code ...
        buildConfigField "String", "HUGGING_FACE_API_KEY", "\"${huggingFaceApiKey}\""
    }
    
    // ... rest of code ...
}
```

### Step 3: Update ChatbotFragment to Use BuildConfig

In `ChatbotFragment.java`, update `getApiKeyFromProperties()`:

```java
private String getApiKeyFromProperties() {
    // Read from BuildConfig (set up in build.gradle)
    String apiKey = BuildConfig.HUGGING_FACE_API_KEY;
    if (apiKey != null && !apiKey.isEmpty() && !apiKey.equals("")) {
        return apiKey;
    }
    return null;
}
```

## Testing Without API Key

The app will still work, but you'll see error messages when trying to send messages. The error handling is already implemented, so the app **won't crash**.

You'll see a message like: "API key not configured. Please set API key."

## Rate Limits (Free Tier)

- Hugging Face free tier is generous for development
- If you hit rate limits, you'll see an error message
- The app handles this gracefully without crashing

## Summary

**For quick testing**: Just hardcode it in `ChatbotFragment.getApiKeyFromProperties()` (remember to remove before committing!)

**For proper setup**: Follow the "Proper Setup" section above to use `local.properties` + BuildConfig.

