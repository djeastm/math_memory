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
    private static final int REQUEST_TAKE_PHOTO = 2;
    private static final int REQUEST_VIEW_PHOTO = 3;

    String mSoundFileName;
    private Entry mEntry;
    private RecyclerView mImageRecyclerView;
    private ImageAdapter mImageAdapter;
    private List<File> mImageFiles = new ArrayList<>();
    private Uri mUriToRevoke;
    private Button mRecordButton;
    private MediaRecorder mRecorder;
    private RecyclerView mSoundRecyclerView;
    private SoundAdapter mSoundAdapter;
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

        ActivityCompat.requestPermissions(this.getActivity(), permissions, REQUEST_RECORD_AUDIO_PERMISSION);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_entry, container, false);

        final ViewSwitcher switcher = v.findViewById(R.id.entry_title_switcher);

        if (mEntry.getTitle() == null) {
            SetUpTitleEditText(switcher, null);
        } else {
            TextView titleTextView = switcher.findViewById(R.id.entry_title_text_view);
            titleTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SetUpTitleEditText(switcher, mEntry.getTitle());
                }
            });
            titleTextView.setText(mEntry.getTitle());
        }

        Button addImageButton = v.findViewById(R.id.fragment_image_add_button);

        addImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File filesDir = getContext().getFilesDir();
                Uri uri = FileProvider.getUriForFile(getActivity(),
                        "com.davidjeastman.mathmemory.fileprovider",
                        new File(filesDir, mEntry.getPhotoFilename(mEntry.getImageCount())));
                mEntry.addImage(uri);
                updateImageUI();
            }
        });

        Spinner typeSpinner = v.findViewById(R.id.entry_type_spinner);
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

        Spinner audioTypeSpinner = v.findViewById(R.id.audio_type_spinner);
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

        mRecordButton = v.findViewById(R.id.entry_record_button);
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

//        if (isExternalStorageWritable()) {
//            mRecordButton.setEnabled(true);
//        } else {
//            mRecordButton.setEnabled(false);
//            Log.e(TAG, "Not writable; Can't record");
//        }
        mImageRecyclerView = v.findViewById(R.id.fragment_image_recycler_view);
        mImageRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));

        mSoundRecyclerView = v.findViewById(R.id.fragment_play_manager_recycler_view);
        mSoundRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        updateImageUI();
        updateSoundUI();
        return v;
    }

    private void SetUpTitleEditText(ViewSwitcher switcher, String startTitle) {
        switcher.showNext();
        EditText titleTextView = switcher.findViewById(R.id.entry_title_edit_text);
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
        } else if (requestCode == REQUEST_TAKE_PHOTO) {
            getActivity().revokeUriPermission(mUriToRevoke, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            updateImageUI();
        } else if (requestCode == REQUEST_VIEW_PHOTO) {
            //getActivity().revokeUriPermission(mUriToRevoke, Intent.FLAG_GRANT_READ_URI_PERMISSION);
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

    private void updateImageUI() {
        mImageFiles = Library.get(getActivity()).getPhotoFiles(mEntry);

        if (mImageAdapter == null) {
            mImageAdapter = new ImageAdapter(mImageFiles);
            mImageRecyclerView.setAdapter(mImageAdapter);
        } else {
            mImageRecyclerView.setAdapter(mImageAdapter);
            mImageAdapter.setImages(mImageFiles);
            mImageAdapter.notifyDataSetChanged();
        }

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

        if (mSoundAdapter == null) {
            mSoundAdapter = new SoundAdapter(mSounds);
            mSoundRecyclerView.setAdapter(mSoundAdapter);
        } else {
            mSoundRecyclerView.setAdapter(mSoundAdapter);
            mSoundAdapter.setSounds(mSounds);
            mSoundAdapter.notifyDataSetChanged();
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
        //mSoundFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        //mSoundFileName += "/" + Library.APP_DIRECTORY + "/" + mEntry.buildSoundFilePathName();
        mSoundFileName = getContext().getFilesDir().getAbsolutePath();
        mSoundFileName += "/" + mEntry.buildSoundFilePathName();

        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mSoundFileName);
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

        mEntry.addAudio(mSoundFileName);

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

    private class ImageHolder extends RecyclerView.ViewHolder {
        PackageManager packageManager;
        private File photoFile;
        private ImageButton photoButton;
        private ImageView photoView;

        public ImageHolder(LayoutInflater inflater, ViewGroup container) {
            super(inflater.inflate(R.layout.photo_frame, container, false));
            packageManager = getActivity().getPackageManager();
            photoButton = itemView.findViewById(R.id.entry_camera);
            photoView = itemView.findViewById(R.id.entry_photo);
        }

        public void bindImage(File imageFile) {
            photoFile = imageFile;
            final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            boolean canTakePhoto = photoFile != null && captureImage.resolveActivity(packageManager) != null;

            photoButton.setEnabled(canTakePhoto);
            photoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri uri = FileProvider.getUriForFile(getActivity(),
                            "com.davidjeastman.mathmemory.fileprovider",
                            photoFile);
                    mUriToRevoke = uri;
                    captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                    List<ResolveInfo> cameraActivities = getActivity()
                            .getPackageManager().queryIntentActivities(captureImage,
                                    PackageManager.MATCH_DEFAULT_ONLY);
                    for (ResolveInfo activity : cameraActivities) {
                        getActivity().grantUriPermission(activity.activityInfo.toString(),
                                uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    }
                    startActivityForResult(captureImage, REQUEST_TAKE_PHOTO);
                }
            });


            final boolean DO_DIALOG = false;
            photoView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (photoFile == null || !photoFile.exists()) return;   //no photo
                    int imageId = mImageRecyclerView.getChildLayoutPosition(itemView);
                    FragmentManager fm = getFragmentManager();
                    EntryPhotoDialogFragment dialogFragment = EntryPhotoDialogFragment.newInstance(photoFile.getPath());
                    if (DO_DIALOG) {
                        dialogFragment.show(fm, mEntry.getPhotoFilename(imageId));
                    } else {
                        // Show with default viewer
//                        Uri uri = FileProvider.getUriForFile(getActivity(),
//                                "com.davidjeastman.mathmemory.fileprovider",
//                                photoFile);
//                        mUriToRevoke = uri;
//                        Log.i(TAG, "Uri: "+uri.toString());
//                        Intent viewImage = new Intent(Intent.ACTION_VIEW, uri);
//                        viewImage.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                        startActivityForResult(viewImage, REQUEST_VIEW_PHOTO);

                        //show in full screen stretched viewpager
                        Intent intent = ImagePagerActivity.newIntent(getActivity(), mEntry.getId(), imageId);
                        startActivity(intent);
//                      //show just one image stretched
//                        android.support.v4.app.FragmentTransaction transaction = fm.beginTransaction();
//                        transaction.setTransition(android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
//                        transaction.add(android.R.id.content, dialogFragment).addToBackStack(null).commit();
                    }
                }
            });

            updatePhotoView();
        }

        private void updatePhotoView() {
            if (photoFile == null || !photoFile.exists()) {
                photoView.setImageDrawable(null);
            } else {
                Bitmap bitmap = PictureUtils.getScaledBitmap(photoFile.getPath(), getActivity());
                photoView.setImageBitmap(bitmap);
            }
        }
    }

    private class ImageAdapter extends RecyclerView.Adapter<ImageHolder> {
        private List<File> mImages;

        public ImageAdapter(List<File> images) {
            mImages = images;
        }

        @Override
        public ImageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            return new ImageHolder(inflater, parent);
        }

        @Override
        public void onBindViewHolder(ImageHolder imageHolder, int position) {
            File image = mImages.get(position);
            imageHolder.bindImage(image);
        }

        @Override
        public int getItemCount() {
            return mImages.size();
        }

        public void setImages(List<File> images) {
            mImages = images;
        }
    }

    private class SoundHolder extends RecyclerView.ViewHolder {
        Sound thisSound;
        private Button mDeleteButton;
        private Button mPlayButton;
        private boolean mStartPlaying = true;

        public SoundHolder(LayoutInflater inflater, ViewGroup container) {
            super(inflater.inflate(R.layout.list_item_sound, container, false));
            mDeleteButton = itemView.findViewById(R.id.list_item_sound_delete_button);
            mPlayButton = itemView.findViewById(R.id.list_item_sound_play_button);
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
