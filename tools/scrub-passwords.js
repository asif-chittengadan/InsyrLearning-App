const admin = require('firebase-admin');
admin.initializeApp({ credential: admin.credential.applicationDefault() });
(async () => {
  const snap = await admin.firestore().collection('users').get();
  let cleared = 0;
  for (const d of snap.docs) {
    if (d.get('password')) {
      await d.ref.update({ password: '' });
      cleared++;
    }
  }
  console.log('cleared', cleared);
})();
