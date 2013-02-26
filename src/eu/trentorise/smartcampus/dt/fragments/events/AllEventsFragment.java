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
package eu.trentorise.smartcampus.dt.fragments.events;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.GridView;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;

import eu.trentorise.smartcampus.android.feedback.fragment.SlidingFragment;
import eu.trentorise.smartcampus.dt.R;
import eu.trentorise.smartcampus.dt.custom.EventsCategoriesAdapter;
import eu.trentorise.smartcampus.dt.custom.SearchHelper;

public class AllEventsFragment extends SherlockFragment {
	private FragmentManager fragmentManager;
	private GridView gridview;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		fragmentManager = getSherlockActivity().getSupportFragmentManager();
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.eventscategories, container, false);
	}

	@Override
	public void onStart() {
		super.onStart();
		gridview = (GridView) getView().findViewById(R.id.events_gridview);
		gridview.setAdapter(new EventsCategoriesAdapter(getSherlockActivity().getApplicationContext(), fragmentManager));
		// hide keyboard if it is still open
		InputMethodManager imm = (InputMethodManager) getSherlockActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(gridview.getWindowToken(), 0);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		
		menu.clear();
		getSherlockActivity().getSupportMenuInflater().inflate(R.menu.gripmenu, menu);
		SubMenu submenu = menu.getItem(0).getSubMenu();
		submenu.clear();

		submenu.add(Menu.CATEGORY_SYSTEM, R.id.menu_item_addevent, Menu.NONE, R.string.menu_item_addevent_text);
		submenu.add(Menu.CATEGORY_SYSTEM, R.id.menu_item_todayevent, Menu.NONE, R.string.menu_item_todayevent_text);
		submenu.add(Menu.CATEGORY_SYSTEM, R.id.menu_item_myevents, Menu.NONE, R.string.menu_item_myevents_text);

		SearchHelper.createSearchMenu(submenu, getActivity(), new SearchHelper.OnSearchListener() {
			@Override
			public void onSearch(String query) {
				SlidingFragment sl = (SlidingFragment) fragmentManager
						.findFragmentById(R.id.feedback_fragment_container);
				EventsListingFragment fragment = new EventsListingFragment();
				Bundle args = new Bundle();
				args.putString(EventsListingFragment.ARG_QUERY, query);
				fragment.setArguments(args);
				/*
				 * FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
				 * fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
				// fragmentTransaction.detach(currentFragment);
				fragmentTransaction.replace(android.R.id.content, fragment, "events");
				fragmentTransaction.addToBackStack(fragment.getTag());
				fragmentTransaction.commit();*/
				sl.replaceFragmentWithTransition(fragment, 
						FragmentTransaction.TRANSIT_FRAGMENT_FADE, true, "Events");
			}
		});

		// super.onCreateOptionsMenu(menu, inflater);
	}

	/*
	 * @Override public void onPrepareOptionsMenu(Menu menu) { menu.clear();
	 * MenuItem item = menu.add(Menu.CATEGORY_SYSTEM, R.id.menu_item_addevent,
	 * 1, R.string.menu_item_addevent_text);
	 * item.setIcon(R.drawable.ic_event_add);
	 * item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
	 * 
	 * item = menu.add(Menu.CATEGORY_SYSTEM, R.id.menu_item_myevents, 2,
	 * R.string.menu_item_myevents_text); item.setIcon(R.drawable.ic_myevents);
	 * item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
	 * 
	 * super.onPrepareOptionsMenu(menu); }
	 */

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		SlidingFragment sl = (SlidingFragment) fragmentManager
				.findFragmentById(R.id.feedback_fragment_container);
		//FragmentTransaction fragmentTransaction;
		Fragment fragment;
		Bundle args;
		switch (item.getItemId()) {

		case R.id.menu_item_addevent:
			/*Gio Comment
			 * fragmentTransaction = fragmentManager.beginTransaction();
			fragment = new CreateEventFragment();
			fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			// fragmentTransaction.detach(this);
			fragmentTransaction.replace(android.R.id.content, fragment, "events");
			fragmentTransaction.addToBackStack(fragment.getTag());
			fragmentTransaction.commit();*/
			fragment = new CreateEventFragment();
			sl.replaceFragmentWithTransition(fragment, 
					FragmentTransaction.TRANSIT_FRAGMENT_FADE, true, "Events");
			return true;

		case R.id.menu_item_todayevent:
			
			fragment = new EventsListingFragment();
			args = new Bundle();
			args.putString(EventsListingFragment.ARG_QUERY_TODAY, "");
			fragment.setArguments(args);
			/*gio comment
			 * fragmentTransaction = fragmentManager.beginTransaction();
			args = new Bundle();
			args.putString(EventsListingFragment.ARG_QUERY_TODAY, "");
			fragment.setArguments(args);
			fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			// fragmentTransaction.detach(currentFragment);
			fragmentTransaction.replace(android.R.id.content, fragment, "events");
			fragmentTransaction.addToBackStack(fragment.getTag());
			fragmentTransaction.commit();*/
			sl.replaceFragmentWithTransition(fragment, 
					FragmentTransaction.TRANSIT_FRAGMENT_FADE, true, "Events");
			return true;
		case R.id.menu_item_myevents:
			
			fragment = new EventsListingFragment();
			args = new Bundle();
			args.putBoolean(EventsListingFragment.ARG_MY, true);
			fragment.setArguments(args);
			/*fragmentTransaction = fragmentManager.beginTransaction();
			fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			// fragmentTransaction.detach(this);
			fragmentTransaction.replace(android.R.id.content, fragment, "events");
			fragmentTransaction.addToBackStack(fragment.getTag());
			fragmentTransaction.commit();*/
			sl.replaceFragmentWithTransition(fragment, 
					FragmentTransaction.TRANSIT_FRAGMENT_FADE, true, "Events");
			return true;
		case R.id.search:
			// getSherlockActivity().onSearchRequested();
			// Toast.makeText(getSherlockActivity(), "Opening search...",
			// Toast.LENGTH_SHORT).show();
			return true;

			/*
			 * case R.id.menu_item_import:
			 * fragmentTransaction=fragmentManager.beginTransaction();
			 * fragment=new ImportFragment();
			 * fragmentTransaction.setTransition(FragmentTransaction
			 * .TRANSIT_FRAGMENT_FADE);
			 * fragmentTransaction.replace(android.R.id.content, fragment,
			 * "import"); fragmentTransaction.addToBackStack(fragment.getTag());
			 * fragmentTransaction.commit(); return true;
			 */
		default:
			return super.onOptionsItemSelected(item);
		}
		
	}
}
