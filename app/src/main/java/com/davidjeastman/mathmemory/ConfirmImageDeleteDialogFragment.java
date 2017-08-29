package com.davidjeastman.mathmemory;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import java.io.File;

/**
 * Created by Dave on 1/10/2016.
 */
public class ConfirmImageDeleteDialogFragment extends DialogFragment {

    private static final String TAG = "ConfirmImageDeleteDlg";
    public static final String EXTRA_CONFIRM_IMAGE_DELETE_PATH =
            "com.davidjeastman.mathmemory.confirm_image_delete_path";

    private static final String ARG_CONFIRM_IMAGE_DELETE_BOOLEAN = "confirm_image_delete_boolean";
    private static final String ARG_CONFIRM_IMAGE_DELETE_FILENAME = "confirm_image_delete_filename";

    public static ConfirmImageDeleteDialogFragment newInstance(File file) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_CONFIRM_IMAGE_DELETE_BOOLEAN, false);
        args.putSerializable(ARG_CONFIRM_IMAGE_DELETE_FILENAME, file.getName());

        ConfirmImageDeleteDialogFragment fragment = new ConfirmImageDeleteDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final String fileName = (String) getArguments().getSerializable(ARG_CONFIRM_IMAGE_DELETE_FILENAME);

        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.confirm_message)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendResult(Activity.RESULT_OK, fileName);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ConfirmImageDeleteDialogFragment.this.getDialog().cancel();
                    }
                })
                .create();
    }

    private void sendResult(int resultCode, String fileName) {
        if (getTargetFragment() == null) {
            return;
        }
        Intent intent = new Intent();
        intent.putExtra(EXTRA_CONFIRM_IMAGE_DELETE_PATH, fileName);
        getTargetFragment()
                .onActivityResult(getTargetRequestCode(), resultCode, intent);
    }
}

