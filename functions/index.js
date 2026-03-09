/**
 * Import function triggers from their respective submodules:
 *
 * const {onCall} = require("firebase-functions/v2/https");
 * const {onDocumentWritten} = require("firebase-functions/v2/firestore");
 *
 * See a full list of supported triggers at https://firebase.google.com/docs/functions
 */

const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const admin = require("firebase-admin");

// For cost control, you can set the maximum number of containers that can be
// running at the same time. This helps mitigate the impact of unexpected
// traffic spikes by instead downgrading performance. This limit is a
// per-function limit. You can override the limit for each function using the
// `maxInstances` option in the function's options, e.g.
// `onRequest({ maxInstances: 5 }, (req, res) => { ... })`.
// NOTE: setGlobalOptions does not apply to functions using the v1 API. V1
// functions should each use functions.runWith({ maxInstances: 10 }) instead.
// In the v1 API, each function can only serve one request per container, so
// this will be the maximum concurrent request count.
// setGlobalOptions({ maxInstances: 10 });

admin.initializeApp();

// Create and deploy your first functions
// https://firebase.google.com/docs/functions/get-started

// exports.helloWorld = onRequest((request, response) => {
//   logger.info("Hello logs!", {structuredData: true});
//   response.send("Hello from Firebase!");
// });

exports.sendChatNotification = onDocumentCreated(
  "chatRooms/{roomId}/messages/{messageId}",
  async (event) => {
    const snapshot = event.data;
    if (!snapshot) return;

    const messageData = snapshot.data();

    const senderUid = messageData.myUid;
    const receiverUid = messageData.otherUid;
    const message = messageData.message;

    if (!senderUid || !receiverUid || !message) {
      console.log("필드 누락", messageData);
      return;
    }

    const db = admin.firestore();

    const receiverDoc = await db.collection("users").doc(receiverUid).get();
    if (!receiverDoc.exists) {
      console.log("receiverDoc 없음", receiverUid);
      return;
    }

    const receiverData = receiverDoc.data();
    const token = receiverData?.fcmToken;
    if (!token) {
      console.log("fcmToken 없음", receiverUid);
      return;
    }

    const senderDoc = await db.collection("users").doc(senderUid).get();
    const senderName = senderDoc.exists
      ? senderDoc.data()?.name || "새 메시지"
      : "새 메시지";

    await admin.messaging().send({
      token,
      notification: {
        title: senderName,
        body: message,
      },
      data: {
        roomId: event.params.roomId,
        senderUid,
        receiverUid,
      },
      android: {
        priority: "high",
      },
    });

    console.log("FCM 전송 완료", receiverUid);
  }
);