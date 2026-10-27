const admin = require('firebase-admin');
admin.initializeApp({
  credential: admin.credential.applicationDefault(),
  projectId: process.env.GCLOUD_PROJECT || 'insyr-learning-app'
});
(async () => {
  const apply = process.argv.includes('--apply');
  const firestore = admin.firestore();
  const snap = await firestore.collection('users').get();
  const docsToScrub = snap.docs.filter((doc) => 'password' in doc.data());
  if (!apply) {
    console.log(`Dry run: ${docsToScrub.length} user documents contain a password field.`);
    console.log('Review the target Firebase project, then rerun with --apply to delete the field.');
    return;
  }

  let batch = firestore.batch();
  let pending = 0;
  let scrubbed = 0;
  for (const doc of docsToScrub) {
    batch.update(doc.ref, { password: admin.firestore.FieldValue.delete() });
    pending++;
    if (pending === 450) {
      await batch.commit();
      scrubbed += pending;
      batch = firestore.batch();
      pending = 0;
    }
  }
  if (pending > 0) {
    await batch.commit();
    scrubbed += pending;
  }
  console.log(`Deleted password fields from ${scrubbed} user documents.`);
})().catch((error) => {
  console.error('Password-field migration failed:', error);
  process.exitCode = 1;
});
