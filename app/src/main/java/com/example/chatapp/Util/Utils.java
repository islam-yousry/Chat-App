package com.example.chatapp.Util;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.example.chatapp.R;
import com.example.chatapp.View.WelcomeActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.onesignal.OneSignal;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropActivity;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {

    /**
     * FRIEND STATES
     * BLOCK --> I BLOCKED THAT FRIEND
     * BLOCKED --> I AM BLOCKED BY THAT FRIEND
     * SENT --> I SENT A FRIEND REQUEST
     * PENDING --> I HAVE A FRIEND REQUEST FROM THAT USER
     * WITH_CHAT --> THERE IS A CHAT DATA BETWEEN THE TWO USERS
     * WITHOUT_CHAT --> THERE IS NO CHAT DATA BETWEEN THE TWO USERS
     */
    public enum FriendState { BLOCK,BLOCKED,SENT,PENDING,WITH_CHAT,WITHOUT_CHAT}


    /**
     * MESSAGES AVAILABLE BACKGROUND COLOR THAT THE USER CAN CHOOSE BETWEEN
     */
    public enum Colors { GREEN,PURPLE,RED,AMBER}

    private static final String TAG = "Utils";

    // convert time to format "hh:mm am"
    public static String convertTime(long time) {
        Date date = new Date(time);
        DateFormat format = new SimpleDateFormat("hh:mm a");
        return format.format(date);
    }


    /**
     * return one to one chat id which equal max(user1id,user2id).append(min(user1id,user2id))
     */
    public static String getChatId(String user1id, String user2id) {
        if (user1id.compareTo(user2id) > 0) return user1id + user2id;
        else return user2id + user1id;
    }


    /**
     * send notification
     */

    public static void sendNotification(String title, String body, String notification_key, String user_id) {
        try {
            JSONObject jsonObject = new JSONObject(
                    "{'contents':{'en':'" + body + "'}," +
                            "'include_player_ids':['" + notification_key + "']," +
                            "'headings':{'en': '" + title + "'}," +
                            "'data': {'userId': '" + user_id + "'}}"
            );

            OneSignal.postNotification(jsonObject, new OneSignal.PostNotificationResponseHandler() {
                @Override
                public void onSuccess(JSONObject response) {
                    Log.d(TAG, "senNotification: successful");
                }

                @Override
                public void onFailure(JSONObject response) {
                    Log.e(TAG, "sendNotification: failure");
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "JsonObject error: " + e.getCause());
        }

    }

    /**
     * resend password process
     */

    public static void resendPassword(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final View dialogView = ((Activity)context).getLayoutInflater().inflate(R.layout.custom_dialog, null);
        builder.setView(dialogView)
                .setTitle("Forgot Password?")
                .setMessage("Enter your Email");
        final AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.drawable_green_gradient);
        alertDialog.show();
        Button okButton = dialogView.findViewById(R.id.ok_button);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mail = ((EditText) dialogView.findViewById(R.id.email_edit_text)).getText().toString();
                FirebaseAuth.getInstance().sendPasswordResetEmail(mail)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "resetPassword: Successful");
                                    Toast.makeText(context, "Sent Password Reset Link to Email", Toast.LENGTH_SHORT).show();
                                } else {
                                    Log.w(TAG, "resetPassword: Failed");
                                    Toast.makeText(context, "No user associated with that email", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }

        });

        Button cancelButton = dialogView.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.cancel();
            }
        });
    }

    /**
     * images utils.
     */

    public static class ImageUtils {
        public static final int RC_PHOTO_PICKER = 1;
        public static final int REQUEST_IMAGE_CAPTURE = 2;


        // convert Uri to bitmap.
        public static Bitmap getBitmap(Uri profilePhotoUri, ContentResolver contentResolver) {
            Bitmap bitmap = null;
            try {
                if (Build.VERSION.SDK_INT > 28) {
                    ImageDecoder.Source source = ImageDecoder.createSource(contentResolver, profilePhotoUri);
                    bitmap = ImageDecoder.decodeBitmap(source);
                } else
                    bitmap = MediaStore.Images.Media.getBitmap(contentResolver, profilePhotoUri);
            } catch (Exception e) {
                Log.e(TAG, "getBitmap: Failure ", e.getCause());
            }
            return bitmap;
        }

        // show image in full screen mode.
        public static void showPhoto(Context context, Uri uri) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "image/*");
            try {
                context.startActivity(intent);
                Log.d(TAG, "showPhoto: success " + uri);
            } catch (Exception e) {
                Toast.makeText(context, "No application can handle this request", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "showPhoto: failure " + e.getCause());
            }
        }


        // perform circular crop using UCrop library.
        public static void performCrop(Uri uri, File cacheDir, Activity activity) {
            File tempCropped = new File(cacheDir, "profileImage.png");
            Uri destinationUri = Uri.fromFile(tempCropped);
            UCrop crop = UCrop.of(uri, destinationUri).withMaxResultSize(1080, 768);
            UCrop.Options options = new UCrop.Options();
            //Set the gesture for cutting pictures
            options.setAllowedGestures(UCropActivity.SCALE, UCropActivity.ROTATE, UCropActivity.ALL);
            options.setFreeStyleCropEnabled(true);
            options.setCircleDimmedLayer(true);
            crop.withOptions(options);
            crop.start(activity);
        }

        // compress image
        private static final double MB_THRESHOLD = 0.5;
        private static final double MB = 1000000.0;

        private static byte[] getBytesFromBitmap(Bitmap bitmap, int quality) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
            return stream.toByteArray();
        }


        public static void pickAPhoto(final Context context) {
            String[] options = {"Choose from gallery", "Take a Photo"};
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Choose an option");
            builder.setItems(options, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == 0) { // Pick Image from Gallery.
                        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        ((Activity) context).startActivityForResult(Intent.createChooser(intent, "Complete Action using"), RC_PHOTO_PICKER);
                    } else if (which == 1) { // capture an Image.
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        ((Activity) context).startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
                    }
                }
            });
            builder.create().show();
        }


        public static byte[] compressImage(Context context, Bitmap imageBitmap) {
            byte[] bytes = null;
            for (int i = 1; i < 11; i++) {
                if (i == 10) {
                    Toast.makeText(context, "Profile Photo is too large.", Toast.LENGTH_SHORT).show();
                    break;
                }
                bytes = getBytesFromBitmap(imageBitmap, 100 / i);
                Log.d(TAG, "doInBackground: megabytes: (" + (11 - i) + "0%)" + bytes.length / MB + " MB");
                if (bytes.length / MB < MB_THRESHOLD)
                    return bytes;
            }
            return bytes;
        }


        public static void uploadProfileImage(final Context context, byte[] compressedImage) {
            FirebaseUser user = Utils.getUser(context);
            assert user != null;
            final StorageReference photoReference = FirebaseStorage.getInstance().getReference()
                    .child("images/users/" + user.getUid() + "/profile_image");
            final UploadTask uploadTask = photoReference.putBytes(compressedImage);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "uploadProfileImage: failure", e.getCause());
                    Toast.makeText(context, "Photo couldn't be uploaded", Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.d(TAG, "uploadProfileImage: success");
                    photoReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            FirebaseDatabase.getInstance().getReference("users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .child("profile_image").setValue(uri.toString());
                        }
                    });
                }
            });
        }


    }

    /**
     * check authentication
     * if the user isn't authenticated welcome activity is launched.
     */
    public static boolean checkAuthenticationState(Context context) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            OneSignal.setSubscription(false);
            Log.d(TAG, "checkAuthenticationState: user is not authenticated.");
            Intent intent = new Intent(context, WelcomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
            return false;
        }

        Log.d(TAG, "checkAuthenticationState: user is authenticated.");
        return true;
    }

    // get the firebase user and check that he is authenticated.
    public static FirebaseUser getUser(Context context) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) checkAuthenticationState(context);
        return firebaseUser;
    }

    /**
     * share app apk
     */

    public static void shareApplication(Context context) {
        ApplicationInfo app = context.getApplicationInfo();
        String filePath = app.sourceDir;

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("*/*");

        // Append file and send Intent
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(filePath)));
        context.startActivity(Intent.createChooser(intent, "Share app via"));
    }

}
