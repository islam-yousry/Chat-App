package com.example.chatapp.Util;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.chatapp.View.MessageActivity;
import com.example.chatapp.View.RequestsActivity;
import com.onesignal.OSNotificationOpenResult;
import com.onesignal.OneSignal;

import org.json.JSONObject;
    public class NotificationOpenedHandler implements OneSignal.NotificationOpenedHandler {
        private final Context context;
        private static final String TAG = "NotificOpenedHandler";

        public NotificationOpenedHandler(Context context){
            this.context = context;
        }

        @Override
        public void notificationOpened(OSNotificationOpenResult result) {
            JSONObject data = result.notification.payload.additionalData;
            Log.i(TAG, "result.notification.payload.toJSONObject().toString(): " + result.notification.payload.toJSONObject().toString());
            if (data != null) {
                String userId = data.optString("userId");
                if (userId.isEmpty()) {// friend request notification
                    goToRequestActivity();
                } else { // message notification.
                    goToMessageActivity(userId);
                }
            }
        }

        private void goToRequestActivity() {
            Intent intent = new Intent(context, RequestsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(intent);

        }

        private void goToMessageActivity(String userId) {
            System.out.println(userId);
            Intent intent = new Intent(context, MessageActivity.class);
            intent.putExtra("userId", userId);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(intent);
        }
    }
