package com.davidjeastman.mathmemory;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;

import java.util.UUID;

import static com.davidjeastman.mathmemory.PictureUtils.getScaledBitmap;

/**
 * Created by David Eastman on 6/28/2017.
 */

public class EntryPhotoDialogFragment extends DialogFragment {
    private static final String TAG = "EntryPhotoDialogFrgmnt";
    private static final String ARG_ENTRY_PHOTO_ID = "entry_photo";

    public static EntryPhotoDialogFragment newInstance(String path) {
        Bundle args = new Bundle();
        args.putString(ARG_ENTRY_PHOTO_ID, path);
        EntryPhotoDialogFragment fragment = new EntryPhotoDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //use this to modify dialog characteristics
//        Log.d("CriminalIntent", "CrimePhotoDialogFragment.onCreateDialog()");
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.requestWindowFeature(Window.FEATURE_SWIPE_TO_DISMISS);
        return dialog;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_entry_photo_dialog, container, false);

        String path = getArguments().getString(ARG_ENTRY_PHOTO_ID, null);
        Bitmap bitmap = getScaledBitmap(path, getActivity());
        ImageView imageView = v.findViewById(R.id.entry_photo_dialog_imageview);
        imageView.setImageBitmap(bitmap);

        return v;
    }
}
