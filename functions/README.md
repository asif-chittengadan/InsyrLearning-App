# Phone sign-in backend

The app calls `loginWithPhone` for phone-number sign-in. The callable function
looks up the phone in Firestore using Admin SDK access and verifies the password
through Firebase Authentication. It returns the mapped email only after the
password is verified; the app then signs in through Firebase Auth's normal
email/password provider. Unknown phones and incorrect passwords receive the
same authentication error. The app never reads the users collection before
authentication.

The endpoint requires Firebase App Check. Debug builds use the App Check debug
provider; register its debug token in Firebase Console. Release builds use Play
Integrity, which must be configured for this Android app. Firebase Authentication
also applies its own abuse protections to password attempts.

## Configure and deploy

1. Use the `insyr-learning-app` Firebase project and ensure Cloud Functions is
   enabled on a billing plan that supports deployment.
2. Set the Secret Manager value to a Firebase Web API key enabled for the
   Identity Toolkit API. If the app's key is restricted to Android package/SHA
   fingerprints, create a separate key restricted to Identity Toolkit and use
   that instead of the `current_key` from `google-services.json`:

   ```shell
   firebase functions:secrets:set IDENTITY_TOOLKIT_API_KEY --project insyr-learning-app
   ```

3. Install the function dependencies and check the source:

   ```shell
   cd functions
   npm install
   npm run check
   cd ..
   ```

4. Deploy only this callable:

   ```shell
   firebase deploy --only functions:loginWithPhone --project insyr-learning-app
   ```

The Firestore rules are not part of the Functions-only deploy. The function's
Admin SDK read does not grant client access to `/users`; existing rules remain
the client security boundary.
