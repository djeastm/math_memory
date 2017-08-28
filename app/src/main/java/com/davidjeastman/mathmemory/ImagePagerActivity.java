package com.davidjeastman.mathmemory;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.io.File;
import java.util.List;
import java.util.UUID;

/**
 * Created by Dave on 8/27/2017.
 */
public class ImagePagerActivity extends AppCompatActivity {

    private static final String EXTRA_ENTRY_ID =
            "com.davidjeastman.mathmemory.entry_id";
    private static final String EXTRA_IMAGE_ID =
            "com.davidjeastman.mathmemory.image_id";

    private ViewPager mViewPager;
    private List<File> mImages;

    public static Intent newIntent(Context packageContext, UUID entryId, int imageId) {
        Intent intent = new Intent(packageContext, ImagePagerActivity.class);
        intent.putExtra(EXTRA_ENTRY_ID, entryId);
        intent.putExtra(EXTRA_IMAGE_ID, imageId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_pager);

        UUID entryId = (UUID) getIntent().getSerializableExtra(EXTRA_ENTRY_ID);
        int imageId = (int) getIntent().getSerializableExtra(EXTRA_IMAGE_ID);

        mViewPager = (ViewPager) findViewById(R.id.activity_image_pager_view_pager);
        mImages = Library.get(this).getPhotoFiles(Library.get(this).getEntry(entryId));
        FragmentManager fragmentManager = getSupportFragmentManager();
        mViewPager.setAdapter(new FragmentStatePagerAdapter(fragmentManager) {
            @Override
            public Fragment getItem(int position) {
                File image = mImages.get(position);
                return EntryPhotoDialogFragment.newInstance(image.getPath());
            }

            @Override
            public int getCount() {
                return mImages.size();
            }
        });

        mViewPager.setCurrentItem(imageId);

    }
}
