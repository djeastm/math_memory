package squeakytree.com.mathmemory;

import android.util.Log;

/**
 * Created by Dave on 12/20/2015.
 */
public class Sound {
    private static final String TAG = "Sound";
    private String mAssetPath;
    private String mName;
    private String mSoundType;
    private Integer mSoundId;

    public Sound(String assetPath) {
        mAssetPath = assetPath;
        String[] components = assetPath.split("/");
        String filename = components[components.length - 1];
        mName = filename.replace(".3gp", "");
        String[] nameComponents = mName.split("_");
        mSoundType = nameComponents[0];
        Log.i(TAG, "Sound: SoundType: " + mSoundType);
    }
    public String getAssetPath() {
        return mAssetPath;
    }
    public String getName() {
        return mName;
    }

    public String getSoundType() {
        return mSoundType;
    }

    public Integer getSoundId() {
        return mSoundId;
    }
    public void setSoundId(Integer soundId) {
        mSoundId = soundId;
    }
}
