package squeakytree.com.mathmemory;

import android.support.v4.app.Fragment;

/**
 * Created by Dave on 12/21/2015.
 */
public class EntryListActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new EntryListFragment();
    }
}
