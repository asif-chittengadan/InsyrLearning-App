const { createHash } = require("node:crypto");
const { initializeApp } = require("firebase-admin/app");
const { getFirestore } = require("firebase-admin/firestore");
const { defineSecret } = require("firebase-functions/params");
const { HttpsError, onCall } = require("firebase-functions/v2/https");
const logger = require("firebase-functions/logger");

initializeApp();

const identityToolkitApiKey = defineSecret("IDENTITY_TOOLKIT_API_KEY");
const adminEmail = "admin@insyrlearning.com";
const genericAuthError = () => new HttpsError("unauthenticated", "Invalid phone number or password.");

function dummyEmailFor(phone) {
  const digest = createHash("sha256").update(phone).digest("hex").slice(0, 24);
  return `unknown-${digest}@invalid.insyrlearning.invalid`;
}

exports.loginWithPhone = onCall(
  {
    enforceAppCheck: true,
    maxInstances: 10,
    timeoutSeconds: 20,
    secrets: [identityToolkitApiKey]
  },
  async (request) => {
    const phone = typeof request.data?.phone === "string" ? request.data.phone.trim() : "";
    const password = typeof request.data?.password === "string" ? request.data.password : "";
    if (!phone || phone.length > 64 || !password || password.length > 4096) {
      throw genericAuthError();
    }

    try {
      const users = getFirestore().collection("users");
      const [phoneMatches, mobileMatches] = await Promise.all([
        users.where("phone", "==", phone).limit(2).get(),
        users.where("mobile", "==", phone).limit(2).get()
      ]);
      const matchesByUid = new Map();
      for (const doc of [...phoneMatches.docs, ...mobileMatches.docs]) {
        matchesByUid.set(doc.id, doc);
      }

      // Do not reveal whether a phone number exists. Unknown phones still make an
      // Auth password check against a deterministic, unusable address.
      const matchedDoc = matchesByUid.size === 1 ? [...matchesByUid.values()][0] : null;
      const profile = matchedDoc?.data();
      const email = typeof profile?.email === "string" && profile.email.includes("@")
        ? profile.email.trim()
        : dummyEmailFor(phone);
      const authResponse = await fetch(
        `https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=${encodeURIComponent(identityToolkitApiKey.value())}`,
        {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ email, password, returnSecureToken: true })
        }
      );
      const authResult = await authResponse.json().catch(() => ({}));

      const isAdmin = profile?.email?.toLowerCase() === adminEmail || profile?.role === "admin";
      if (!authResponse.ok || !matchedDoc || authResult.localId !== matchedDoc.id || isAdmin) {
        throw genericAuthError();
      }

      // Return the account email only after the user proves knowledge of its password.
      // The app then signs in through Firebase Auth's native email/password provider.
      return { email: authResult.email || email };
    } catch (error) {
      if (error instanceof HttpsError) throw error;
      logger.error("Phone sign-in request failed", { code: error?.code || "unknown" });
      throw new HttpsError("internal", "Sign-in is temporarily unavailable. Please try again.");
    }
  }
);
