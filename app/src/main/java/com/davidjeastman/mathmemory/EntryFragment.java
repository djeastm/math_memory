package com.davidjeastman.mathmemory;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Dave on 12/19/2015.
 */
public class EntryFragment extends Fragment {

    private static final String TAG = "EntryFragment";
    private static final String ARG_ENTRY_ID = "entry_id";
    private static final String DIALOG_CONFIRM = "DialogConfirm";

    private static final int REQUEST_CONFIRM_AUDIO_DELETE = 0;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static final int REQUEST_PHOTO = 2;

    String mFileName;
    private Entry mEntry;
    private File mPhotoFile;

    // Image-related
    private ImageButton mPhotoButton;
    private ImageView mPhotoView;

    // Audio-Related
    private Button mRecordButton;
    private MediaRecorder mRecorder;
    private RecyclerView mRecyclerView;
    private SoundAdapter mAdapter;
    private List<Sound> mSounds = new ArrayList<>();
    private MediaPlayer mPlayer;

    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};

    public static EntryFragment newInstance(UUID entryId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_ENTRY_ID, entryId);
        EntryFragment fragment = new EntryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted) getActivity().finish();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        UUID entryId = (UUID) getArguments().getSerializable(ARG_ENTRY_ID);

        mEntry = Library.get(getActivity()).getEntry(entryId);
        mPhotoFile = Library.get(getActivity()).getPhotoFile(mEntry);

        ActivityCompat.requestPermissions(this.getActivity(), permissions, REQUEST_RECORD_AUDIO_PERMISSION);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_entry, container, false);

        PackageManager packageManager = getActivity().getPackageManager();

        mPhotoButton = (ImageButton) v.findViewById(R.id.entry_camera);

        final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        boolean canTakePhoto = mPhotoFile != null && captureImage.resolveActivity(packageManager) != null;
        mPhotoButton.setEnabled(canTakePhoto);
        mPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = FileProvider.getUriForFile(getActivity(),
                        "com.davidjeastman.mathmemory.fileprovider",
                        mPhotoFile);
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                List<ResolveInfo> cameraActivities = getActivity()
                        .getPackageManager().queryIntentActivities(captureImage,
                                PackageManager.MATCH_DEFAULT_ONLY);
                for (ResolveInfo activity : cameraActivities) {
                    getActivity().grantUriPermission(activity.activityInfo.toString(),
                            uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                }
                startActivityForResult(captureImage, REQUEST_PHOTO);
            }
        });

        final ViewSwitcher switcher = (ViewSwitcher) v.findViewById(R.id.entry_title_switcher);

        if (mEntry.getTitle() == null) {
            SetUpTitleEditText(switcher, null);
        } else {
            TextView titleTextView = (TextView) switcher.findViewById(R.id.entry_title_text_view);
            titleTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SetUpTitleEditText(switcher, mEntry.getTitle());
                }
            });
            titleTextView.setText(mEntry.getTitle());
        }

        Spinner typeSpinner = (Spinner) v.findViewById(R.id.entry_type_spinner);
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(v.getContext(), R.array.entry_type_array, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(typeAdapter);
        typeSpinner.setSelection(getIndex(typeSpinner, mEntry.getTypeEntry()));
        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mEntry.setTypeEntry((String) parent.getItemAtPosition(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Spinner audioTypeSpinner = (Spinner) v.findViewById(R.id.audio_type_spinner);
        ArrayAdapter<CharSequence> recordingAdapter = ArrayAdapter.createFromResource(v.getContext(), R.array.audio_type_array, android.R.layout.simple_spinner_item);
        recordingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        audioTypeSpinner.setAdapter(recordingAdapter);
        audioTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mEntry.setTypeAudio((String) parent.getItemAtPosition(position));

                updateSoundUI();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mRecordButton = (Button) v.findViewById(R.id.entry_record_button);
        mRecordButton.setOnClickListener(new View.OnClickListener() {
            boolean mStartRecording = true;

            @Override
            public void onClick(View v) {
                onRecord(mStartRecording);
                if (mStartRecording) {
                    mRecordButton.setText(R.string.entry_stop_record_button_label);
                    mRecordButton.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.red));
                } else {
                    mRecordButton.setText(R.string.entry_start_record_button_label);
                    mRecordButton.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.green));
                }
                mStartRecording = !mStartRecording;
            }
        });

        if (isExternalStorageWritable()) {
            mRecordButton.setEnabled(true);
        } else {
            mRecordButton.setEnabled(false);
            Log.e(TAG, "Not writable; Can't record");
        }

        mRecyclerView = (RecyclerView) v
                .findViewById(R.id.fragment_play_manager_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        final boolean DO_DIALOG = true;
        mPhotoView = (ImageView) v.findViewById(R.id.entry_photo);
        mPhotoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("CrimeFragment", "click on photo");
                if (mPhotoFile==null || !mPhotoFile.exists()) return;   //no photo
                FragmentManager fm = getFragmentManager();
                EntryPhotoDialogFragment dialogFragment = EntryPhotoDialogFragment.newInstance(mPhotoFile.getPath());
                if (DO_DIALOG) {
                    dialogFragment.show(fm, mEntry.getPhotoFilename());
                } else {
                    //show in full screen
                    android.support.v4.app.FragmentTransaction transaction = fm.beginTransaction();
                    transaction.setTransition(android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    transaction.add(android.R.id.content, dialogFragment).addToBackStack(null).commit();
                }
            }
        });

        updatePhotoView();
        updateSoundUI();
        return v;
    }

    private void SetUpTitleEditText(ViewSwitcher switcher, String startTitle) {
        switcher.showNext();
        EditText titleTextView = (EditText) switcher.findViewById(R.id.entry_title_edit_text);
        titleTextView.setText(startTitle);
        titleTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(
                    CharSequence s, int start, int count, int after) {
                // This space intentionally left blank
            }

            @Override
            public void onTextChanged(
                    CharSequence s, int start, int before, int count) {
                mEntry.setTitle(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // This one too
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_CONFIRM_AUDIO_DELETE) {
            String soundType = (String) data
                    .getSerializableExtra(ConfirmDialogFragment.EXTRA_CONFIRM_AUDIO_DELETE_TYPE);

            String assetPath = (String) data
                    .getSerializableExtra(ConfirmDialogFragment.EXTRA_CONFIRM_AUDIO_DELETE_PATH);

            String toastString = soundType + " " + getResources().getString(R.string.toast_audio_type_deleted);
            mEntry.deleteAudio(soundType, assetPath);
            Toast.makeText(getActivity(), toastString, Toast.LENGTH_SHORT).show();
            updateSoundUI();
        } else if (requestCode == REQUEST_PHOTO) {
            Uri uri = FileProvider.getUriForFile(getActivity(),
                    "com.davidjeastman.mathmemory.fileprovider", mPhotoFile);
            getActivity().revokeUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            updatePhotoView();
        }
    }

    private int getIndex(Spinner spinner, String myString) {

        int index = 0;

        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).equals(myString)) {
                index = i;
            }
        }
        return index;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateSoundUI();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }

        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }

        Library.get(getActivity())
                .updateEntry(mEntry);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    private void getSounds() {
        mSounds.clear();

        List<String> filesToLoad = mEntry.getAllSounds();

        for (String filename : filesToLoad) {
            Sound sound = new Sound(filename);
            mSounds.add(sound);
        }
    }

    private void updateSoundUI() {
        getSounds();

        if (mAdapter == null) {
            mAdapter = new SoundAdapter(mSounds);
            mRecyclerView.setAdapter(mAdapter);
        } else {
            mRecyclerView.setAdapter(mAdapter);
            mAdapter.setSounds(mSounds);
            mAdapter.notifyDataSetChanged();
        }

    }

    public void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void startRecording() {
        //mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName = this.getContext().getExternalCacheDir().getAbsolutePath();
        //mFileName += "/" + Library.APP_DIRECTORY + "/" + mEntry.buildSoundFilePathName();
        mFileName += "/" + mEntry.buildSoundFilePathName();

        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(TAG, "Recording prepare() failed");
        }

        mRecorder.start();
    }

    private void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;

        mEntry.addAudio(mFileName);

        updateSoundUI();
    }

    public void onPlay(boolean start, String fileName) {
        if (start) {
            startPlaying(fileName);
        } else {
            stopPlaying();
        }
    }

    private void startPlaying(String fileName) {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(fileName);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e(TAG, "Playing prepare() failed");
        }

    }

    private void stopPlaying() {
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }

    private void updatePhotoView() {
        if (mPhotoFile == null || !mPhotoFile.exists()) {
            mPhotoView.setImageDrawable(null);
        } else {
            Bitmap bitmap = PictureUtils.getScaledBitmap(mPhotoFile.getPath(), getActivity());
            mPhotoView.setImageBitmap(bitmap);
        }
    }

    private class SoundHolder extends RecyclerView.ViewHolder {
        Sound thisSound;
        private Button mDeleteButton;
        private Button mPlayButton;
        private boolean mStartPlaying = true;

        public SoundHolder(LayoutInflater inflater, ViewGroup container) {
            super(inflater.inflate(R.layout.list_item_sound, container, false));
            mDeleteButton = (Button) itemView.findViewById(R.id.list_item_sound_delete_button);
            mPlayButton = (Button) itemView.findViewById(R.id.list_item_sound_play_button);
        }

        public void bindSound(Sound sound) {
            thisSound = sound;
            mPlayButton.setText(sound.getSoundType());
            mPlayButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onPlay(mStartPlaying, thisSound.getAssetPath());
                    if (mStartPlaying) {
                        mPlayButton.setText(R.string.entry_stop_play_button_label);
                    } else {
                        mPlayButton.setText(thisSound.getSoundType());
                    }
                    mStartPlaying = !mStartPlaying;
                }
            });

            mDeleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentManager manager = getFragmentManager();
                    ConfirmDialogFragment dialog = ConfirmDialogFragment.newInstance(thisSound);
                    dialog.setTargetFragment(EntryFragment.this, REQUEST_CONFIRM_AUDIO_DELETE);
                    dialog.show(manager, DIALOG_CONFIRM);
                }
            });
        }


    }

    private class SoundAdapter extends RecyclerView.Adapter<SoundHolder> {
        private List<Sound> mSounds;

        public SoundAdapter(List<Sound> sounds) {
            mSounds = sounds;
        }

        @Override
        public SoundHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            return new SoundHolder(inflater, parent);
        }

        @Override
        public void onBindViewHolder(SoundHolder soundHolder, int position) {
            Sound sound = mSounds.get(position);
            soundHolder.bindSound(sound);
        }

        @Override
        public int getItemCount() {
            return mSounds.size();
        }

        public void setSounds(List<Sound> sounds) {
            mSounds = sounds;
        }
    }
}
