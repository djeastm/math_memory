package com.davidjeastman.mathmemory;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

/**
 * Created by Dave on 1/10/2016.
 */
public class ConfirmDialogFragment extends DialogFragment {

    public static final String EXTRA_CONFIRM_AUDIO_DELETE_TYPE =
            "com.squeakytree.mathmemory.confirm_audio_delete_type";
    public static final String EXTRA_CONFIRM_AUDIO_DELETE_PATH =
            "com.squeakytree.mathmemory.confirm_audio_delete_path";

    private static final String ARG_CONFIRM_AUDIO_DELETE_BOOLEAN = "confirm_audio_delete_boolean";
    private static final String ARG_CONFIRM_AUDIO_DELETE_SOUND_TYPE = "confirm_audio_delete_sound_type";
    private static final String ARG_CONFIRM_AUDIO_DELETE_SOUND_PATH = "confirm_audio_delete_sound_path";

    public static ConfirmDialogFragment newInstance(Sound sound) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_CONFIRM_AUDIO_DELETE_BOOLEAN, false);
        args.putSerializable(ARG_CONFIRM_AUDIO_DELETE_SOUND_TYPE, sound.getSoundType());
        args.putSerializable(ARG_CONFIRM_AUDIO_DELETE_SOUND_PATH, sound.getAssetPath());

        ConfirmDialogFragment fragment = new ConfirmDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final String soundType = (String) getArguments().getSerializable(ARG_CONFIRM_AUDIO_DELETE_SOUND_TYPE);
        final String assetPath = (String) getArguments().getSerializable(ARG_CONFIRM_AUDIO_DELETE_SOUND_PATH);

        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.confirm_message)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendResult(Activity.RESULT_OK, soundType, assetPath);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ConfirmDialogFragment.this.getDialog().cancel();
                    }
                })
                .create();
    }

    private void sendResult(int resultCode, String soundType, String assetPath) {
        if (getTargetFragment() == null) {
            return;
        }
        Intent intent = new Intent();
        intent.putExtra(EXTRA_CONFIRM_AUDIO_DELETE_TYPE, soundType);
        intent.putExtra(EXTRA_CONFIRM_AUDIO_DELETE_PATH, assetPath);
        getTargetFragment()
                .onActivityResult(getTargetRequestCode(), resultCode, intent);
    }
}

