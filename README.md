# cordova-plugin-dataleon

Cordova plugin exposing the Dataleon SDK – Smart and scalable identity verification.

## Requirements

- Android 5.1 or newer
- iOS 11.0 or newer
- An active Dataleon account and a valid session URL

## Installation

Within your Cordova project:

```bash
cordova plugin add @dataleon/cordova-plugin-dataleon
```

## Usage

The plugin exposes the `Dataleon` JavaScript class for end-to-end verification.

### In your Cordova or Ionic app

#### 1. Declare the global (if using TypeScript)

```typescript
declare var Dataleon: any;
```

#### 2. Start a verification session

You need a valid Dataleon session URL to start the process.

```javascript
document.addEventListener("deviceready", function () {
  const sessionUrl = "https://id.dataleon.ai/w/123";
  const dataleon = new Dataleon(sessionUrl);
  // Start the verification session
  dataleon.openSession(function (result) {
    console.log("Dataleon result:", result);
  },
    function (err) {
      // Error callback
      console.error("Dataleon error:", err);
    },
  );

  // Close session
  dataleon.closeSession();
});
```

## Building

Within the root plugin path:

```bash
npm run build
```

## Notes

- The plugin opens a native WebView for the verification process.
- Camera and microphone permissions are handled automatically.
- Compatible with Cordova, Ionic, and PhoneGap.

---

For more information, see the official [Dataleon documentation](https://docs.dataleon.ai).
