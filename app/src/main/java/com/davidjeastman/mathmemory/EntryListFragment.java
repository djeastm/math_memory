package com.davidjeastman.mathmemory;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dave on 12/21/2015.
 */
public class EntryListFragment extends Fragment {
    public static final String TAG = "EntryListFragment";

    private RecyclerView mEntryRecyclerView;
    private EntryAdapter mAdapter;

    private List<Entry> markedForDelete;
    private boolean directionReversed = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        markedForDelete = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_entry_list, container, false);

        mEntryRecyclerView = (RecyclerView) view
                .findViewById(R.id.entry_recycler_view);
        mEntryRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        updateUI();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_entry_list, menu);

        //MenuItemCompat searchItem = MenuItemCompat.getActionView(menu.findItem(R.id.menu_item_search));
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.menu_item_search));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                Log.d(TAG, "QueryTextSubmit: " + s);
                QueryPreferences.setStoredQuery(getActivity(), s);
                searchView.clearFocus();
                updateUI();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                //Log.d(TAG, "QueryTextChange: " + s);
                if (s.equals("")) {
                    QueryPreferences.setStoredQuery(getActivity(), null);
                    updateUI();
                }
                return false;
            }
        });

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = QueryPreferences.getStoredQuery(getActivity());
                searchView.setQuery(query, false);
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_new_entry:
                Entry entry = new Entry();
                Library.get(getActivity()).addEntry(entry);
                Intent intent = EntryPagerActivity
                        .newIntent(getActivity(), entry.getId());
                startActivity(intent);
                return true;
            case R.id.menu_item_delete_entry:
                int numMarked = markedForDelete.size();
                String toastString = getResources()
                        .getQuantityString(R.plurals.toast_entries_deleted_plural, numMarked, numMarked);
                for (int i = 0; i < numMarked; i++) {
                    Library.get(getActivity()).deleteEntry(markedForDelete.get(i));
                }

                Toast.makeText(getActivity(), toastString, Toast.LENGTH_SHORT).show();
                markedForDelete.clear();
                updateUI();
                return true;
            case R.id.menu_item_toggle_scroll_position:
                directionReversed = !directionReversed;
                updateUI();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateSubtitle(int entryCount) {
        String subtitle = getResources()
                .getQuantityString(R.plurals.subtitle_plural, entryCount, entryCount);

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity.getSupportActionBar() != null)
            activity.getSupportActionBar().setSubtitle(subtitle);
    }

    private void updateUI() {
        String searchTerm = QueryPreferences.getStoredQuery(getActivity());
        Library library = Library.get(getActivity());
        List<Entry> entries;

        if (searchTerm == null) {
            entries = library.getEntries();
        } else {
            entries = library.getEntries(searchTerm);
        }

        if (mAdapter == null) {
            mAdapter = new EntryAdapter(entries);
            mEntryRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setEntries(entries);
            mAdapter.notifyDataSetChanged();

            if (directionReversed) mEntryRecyclerView.scrollToPosition(entries.size() - 1);
            else mEntryRecyclerView.scrollToPosition(0);
        }

        int entryCount = entries.size();
        updateSubtitle(entryCount);
    }

    private class EntryHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private Entry mEntry;

        private TextView mTitleTextView;
        private TextView mTypeTextView;
        private CheckBox mDeleteCheckBox;

        public EntryHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            mTitleTextView = (TextView) itemView.findViewById(R.id.list_item_entry_title_text_view);
            mTypeTextView = (TextView) itemView.findViewById(R.id.list_item_entry_type_text_view);
            mDeleteCheckBox = (CheckBox) itemView.findViewById(R.id.list_item_entry_delete_check_box);

            mDeleteCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        markedForDelete.add(mEntry);
                    }
                }
            });
        }

        public void bindEntry(Entry entry) {
            mEntry = entry;
            mTitleTextView.setText(mEntry.getTitle());
            mTypeTextView.setText(mEntry.getTypeEntry());
            mDeleteCheckBox.setChecked(false);
        }

        @Override
        public void onClick(View v) {
            Intent intent = EntryPagerActivity.newIntent(getActivity(), mEntry.getId());
            startActivity(intent);
        }
    }

    private class EntryAdapter extends RecyclerView.Adapter<EntryHolder> {
        private List<Entry> mEntries;

        public EntryAdapter(List<Entry> entries) {
            mEntries = entries;
        }

        @Override
        public EntryHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater
                    .inflate(R.layout.list_item_entry, parent, false);
            return new EntryHolder(view);
        }

        @Override
        public void onBindViewHolder(EntryHolder holder, int position) {
            Entry entry = mEntries.get(position);
            holder.bindEntry(entry);
        }

        @Override
        public int getItemCount() {
            return mEntries.size();
        }

        public void setEntries(List<Entry> entries) {
            mEntries = entries;
        }
    }
}
