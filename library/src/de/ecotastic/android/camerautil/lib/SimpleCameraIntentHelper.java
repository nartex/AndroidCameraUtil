package de.ecotastic.android.camerautil.lib;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;

import java.io.File;
import java.util.Date;

import de.ecotastic.android.camerautil.R;

/**
 * Created by Jean on 14/09/15.
 */
public class SimpleCameraIntentHelper implements CameraIntentHelperCallback {
    public final static String TAG = "SimpleCameraIntentHelper";
    private final static int REQUEST_CODE_PICK_PHOTO = 200;

    private Activity mActivity;
    private Fragment mFragment;

    private CameraIntentHelper mCameraIntentHelper;
    private SimpleCameraIntentHelperListener mSimpleCameraIntentHelperListener;

    public SimpleCameraIntentHelper(Activity activity, SimpleCameraIntentHelperListener simpleCameraIntentHelperListener){
        mActivity = activity;
        mSimpleCameraIntentHelperListener = simpleCameraIntentHelperListener;
        mCameraIntentHelper = new CameraIntentHelper(activity, this);
    }

    public SimpleCameraIntentHelper(Fragment fragment, SimpleCameraIntentHelperListener simpleCameraIntentHelperListener){
        mFragment = fragment;
        mSimpleCameraIntentHelperListener = simpleCameraIntentHelperListener;
        mCameraIntentHelper = new CameraIntentHelper(fragment, this);
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        mCameraIntentHelper.onSaveInstanceState(savedInstanceState);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        mCameraIntentHelper.onRestoreInstanceState(savedInstanceState);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PICK_PHOTO){
            Uri uri = data != null ? data.getData() : null;
            if(uri != null && resultCode == Activity.RESULT_OK){
                mSimpleCameraIntentHelperListener.onSimpleCameraIntentHelperReceive(uri, 0);
            }
        }else if (mCameraIntentHelper != null){
            mCameraIntentHelper.onActivityResult(requestCode, resultCode, data);
        }
    }

    private Context getContext(){
        return mActivity != null ? mActivity : mFragment.getActivity();
    }

    public void showChooserDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.androidCameraUtil_chooseSource);
        builder.setItems(R.array.androidCameraUtil_choicePhoto, new AlertDialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        mCameraIntentHelper.startCameraIntent();
                        break;
                    case 1:
                        Intent mediaChooser = new Intent(Intent.ACTION_GET_CONTENT);
                        mediaChooser.setType("image/*");
                        mCameraIntentHelper.startActivityForResult(mediaChooser, REQUEST_CODE_PICK_PHOTO);
                        break;
                }
            }
        });
        builder.show();
    }

    @Override
    public void onPhotoUriFound(Date dateCameraIntentStarted, Uri photoUri, int rotateXDegrees) {
        mSimpleCameraIntentHelperListener.onSimpleCameraIntentHelperReceive(photoUri, rotateXDegrees);
    }

    @Override
    public void deletePhotoWithUri(Uri photoUri) {
        deleteImageWithUriIfExists(photoUri, getContext());
    }

    @Override
    public void onSdCardNotMounted() {}

    @Override
    public void onCanceled() {}

    @Override
    public void onCouldNotTakePhoto() {}

    @Override
    public void onPhotoUriNotFound() {}

    @Override
    public void logException(Exception e) {}

    public interface SimpleCameraIntentHelperListener{
        void onSimpleCameraIntentHelperReceive(Uri uri, int rotate);
    }

    /**
     * Deletes an image given its Uri and refreshes the gallery thumbnails.
     * @param cameraPicUri
     * @param context
     * @return true if it was deleted successfully, false otherwise.
     */
    public static boolean deleteImageWithUriIfExists(Uri cameraPicUri, Context context) {
        try {
            if (cameraPicUri != null) {
                File fdelete = new File(cameraPicUri.getPath());
                if (fdelete.exists()) {
                    if (fdelete.delete()) {
                        refreshGalleryImages(context, fdelete);
                        return true;
                    }
                }
            }
        } catch (Exception e) {
        }
        return false;
    }

    /**
     * Forces the Android gallery to  refresh its thumbnail images.
     * @param context
     * @param fdelete
     */
    private static void refreshGalleryImages(Context context, File fdelete) {
        try {
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" +  Environment.getExternalStorageDirectory())));
        } catch (Exception e1) {
            try {
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contentUri = Uri.fromFile(fdelete);
                mediaScanIntent.setData(contentUri);
                context.sendBroadcast(mediaScanIntent);
            } catch (Exception e2) {
            }
        }
    }
}
