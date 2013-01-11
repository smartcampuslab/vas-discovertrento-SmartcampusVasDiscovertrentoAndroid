/*******************************************************************************
 * Copyright 2012-2013 Trento RISE
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either   express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package eu.trentorise.smartcampus.dt.fragments.stories;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.GridView;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import eu.trentorise.smartcampus.dt.R;
import eu.trentorise.smartcampus.dt.custom.SearchHelper;
import eu.trentorise.smartcampus.dt.custom.StoriesCategoriesAdapter;

/*
 * build the grid with the stories' categories
 */
public class AllStoriesFragment extends SherlockFragment {
	private GridView gridview;
	private FragmentManager fragmentManager;
    private static final String TAG = "AllStoriesFragment";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "eu.trentorise.smartcampus.dt.fragments.stories.AllStoriesFragment.onCreate ");
        }
 
		fragmentManager = getSherlockActivity().getSupportFragmentManager();
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "eu.trentorise.smartcampus.dt.fragments.stories.AllStoriesFragment.onCreateView ");
        }
 		
		return inflater.inflate(R.layout.stories_categories, container, false);
	}

	@Override
	public void onStart() {
		super.onStart();
		
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "eu.trentorise.smartcampus.dt.fragments.stories.AllStoriesFragment.onStart ");
        }
 
		gridview = (GridView) getView().findViewById(R.id.stories_gridview);
		gridview.setAdapter(new StoriesCategoriesAdapter(getSherlockActivity().getApplicationContext(), fragmentManager));
		//hide keyboard if it is still open
		InputMethodManager imm = (InputMethodManager)getSherlockActivity().getSystemService(
			      Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(gridview.getWindowToken(), 0);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "eu.trentorise.smartcampus.dt.fragments.stories.AllStoriesFragment.onPrepareOptionsMenu ");
        }
 
		menu.clear();
		getSherlockActivity().getSupportMenuInflater().inflate(R.menu.gripmenu, menu);
		SubMenu submenu = menu.getItem(0).getSubMenu();
		submenu.clear();

		submenu.add(Menu.CATEGORY_SYSTEM, R.id.menu_item_addstory, Menu.NONE,
				R.string.menu_item_addstory_text);
		submenu.add(Menu.CATEGORY_SYSTEM, R.id.menu_item_mystory, Menu.NONE,
				R.string.menu_item_mystories_text);
		SearchHelper.createSearchMenu(submenu, getActivity(), new SearchHelper.OnSearchListener() {
			@Override
			public void onSearch(String query) {
				FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
				StoriesListingFragment fragment = new StoriesListingFragment();
				Bundle args = new Bundle();
				args.putString(StoriesListingFragment.ARG_QUERY, query);
				fragment.setArguments(args);
				fragmentTransaction
						.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
				fragmentTransaction.replace(android.R.id.content, fragment,"stories");
				fragmentTransaction.addToBackStack(fragment.getTag());
				fragmentTransaction.commit();
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "eu.trentorise.smartcampus.dt.fragments.stories.AllStoriesFragment.onOptionsItemSelected ");
        }
 
		switch (item.getItemId()) {
		case R.id.menu_item_addstory:
			FragmentTransaction fragmentTransaction = fragmentManager
					.beginTransaction();
			Fragment fragment = new CreateStoryFragment();
			fragmentTransaction
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			fragmentTransaction.replace(android.R.id.content, fragment, "stories");
			fragmentTransaction.addToBackStack(fragment.getTag());
			fragmentTransaction.commit();
			return true;
		case R.id.menu_item_mystory:
			fragmentTransaction = fragmentManager.beginTransaction();
			fragment = new StoriesListingFragment();
			Bundle args = new Bundle();
			args.putBoolean(StoriesListingFragment.ARG_MY, true);
			fragment.setArguments(args);
			fragmentTransaction
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			fragmentTransaction.replace(android.R.id.content, fragment, "stories");
			fragmentTransaction.addToBackStack(fragment.getTag());
			fragmentTransaction.commit();
			return true;

		case R.id.search:
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
