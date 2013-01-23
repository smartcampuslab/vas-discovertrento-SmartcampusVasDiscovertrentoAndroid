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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;

import eu.trentorise.smartcampus.dt.R;
import eu.trentorise.smartcampus.android.common.SCAsyncTask;
import eu.trentorise.smartcampus.android.common.SCAsyncTask.SCAsyncTaskProcessor;
import eu.trentorise.smartcampus.android.common.follow.FollowEntityObject;
import eu.trentorise.smartcampus.android.common.follow.FollowHelper;
import eu.trentorise.smartcampus.android.common.listing.AbstractLstingFragment;
import eu.trentorise.smartcampus.android.common.tagging.SemanticSuggestion;
import eu.trentorise.smartcampus.android.common.tagging.TaggingDialog;
import eu.trentorise.smartcampus.android.common.tagging.TaggingDialog.TagProvider;
import eu.trentorise.smartcampus.dt.custom.AbstractAsyncTaskProcessor;
import eu.trentorise.smartcampus.dt.custom.CategoryHelper;
import eu.trentorise.smartcampus.dt.custom.SearchHelper;
import eu.trentorise.smartcampus.dt.custom.StoryAdapter;
import eu.trentorise.smartcampus.dt.custom.StoryPlaceholder;
import eu.trentorise.smartcampus.dt.custom.data.DTHelper;
import eu.trentorise.smartcampus.dt.model.Concept;
import eu.trentorise.smartcampus.dt.model.DTConstants;
import eu.trentorise.smartcampus.dt.model.StoryObject;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;


/*
 * Fragment lists the stories of a category, my stories or the searched ones
 */
public class StoriesListingFragment extends AbstractLstingFragment<StoryObject> implements TagProvider {
	public static final String ARG_CATEGORY = "story_category";
	public static final String ARG_QUERY = "story_query";
	public static final String ARG_MY = "story_my";
	public static final String ARG_CATEGORY_SEARCH = "category_search";

	private String category;

	private ListView list;
	private Context context;
	private View clickedElement;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.context = this.getSherlockActivity();
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.stories_list, container, false);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		getSherlockActivity().getSupportMenuInflater().inflate(R.menu.gripmenu, menu);

		SubMenu submenu = menu.getItem(0).getSubMenu();
		submenu.clear();
		SearchHelper.createSearchMenu(submenu, getActivity(), new SearchHelper.OnSearchListener() {
			@Override
			public void onSearch(String query) {
				FragmentTransaction fragmentTransaction = getSherlockActivity().getSupportFragmentManager().beginTransaction();
				StoriesListingFragment fragment = new StoriesListingFragment();
				Bundle args = new Bundle();
				args.putString(StoriesListingFragment.ARG_QUERY, query);
				String category = (getArguments() != null) ? getArguments().getString(ARG_CATEGORY) : null;
				args.putString(StoriesListingFragment.ARG_CATEGORY_SEARCH, category);
				fragment.setArguments(args);
				fragmentTransaction
						.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
				fragmentTransaction.replace(android.R.id.content, fragment,"stories");
				fragmentTransaction.addToBackStack(fragment.getTag());
				fragmentTransaction.commit();
			}
		});	
		
		if (category==null)
			category = (getArguments() != null) ? getArguments().getString(ARG_CATEGORY) : null;
		if (category!=null)
			submenu.add(Menu.CATEGORY_SYSTEM, R.id.menu_item_addstory, Menu.NONE,getString(R.string.add)+" "+category+" "+getString(R.string.story));

		super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_item_addstory:
			FragmentTransaction fragmentTransaction = getSherlockActivity().getSupportFragmentManager().beginTransaction();
			Fragment fragment = new CreateStoryFragment();
			Bundle args = new Bundle();
			args.putString(ARG_CATEGORY, category);
			fragment.setArguments(args);
			fragmentTransaction
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		//	fragmentTransaction.detach(this);
			fragmentTransaction.replace(android.R.id.content, fragment, "stories");
			fragmentTransaction.addToBackStack(fragment.getTag());
			fragmentTransaction.commit();
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/*
	 * try to load the arguments, change the story for every case 
	 */
	@Override
	public void onStart() {
		Bundle bundle = this.getArguments();
		String category = (bundle != null) ? bundle.getString(ARG_CATEGORY) : null;
		list = (ListView) getSherlockActivity().findViewById(R.id.stories_list);
		StoryAdapter storyAdapter = new StoryAdapter(context, R.layout.stories_row);
		setAdapter(storyAdapter);
		// set title
		TextView title = (TextView) getView().findViewById(R.id.list_title);
		if (category != null) {
			title.setText(category);
		} else if (bundle != null && bundle.containsKey(ARG_MY)) {
			title.setText(R.string.mystory);
		} else if (bundle != null && bundle.containsKey(ARG_QUERY)) {
			String query = bundle.getString(ARG_QUERY);
			title.setText("Search for '" + query + "'");
			if (bundle.containsKey(ARG_CATEGORY_SEARCH)){
				category =  bundle.getString(ARG_CATEGORY_SEARCH) ;
				if (category!=null)
					title.append(" in "+category+" category");
				}
		}

		// close items menus if open
		((View) list.getParent())
				.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						hideListItemsMenu(v);
					}
				});
		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				hideListItemsMenu(view);
			}
		});

		// open items menu for that entry
		list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				ViewSwitcher vs = (ViewSwitcher) view;
				setupOptionsListeners(vs, position);
				vs.showNext();
				return true;
			}
		});

		super.onStart();
	}

	/*
	 * the contextual menu for every item in the list
	 */
	
	protected void setupOptionsListeners(ViewSwitcher vs, final int position) {
		final StoryObject story = ((StoryPlaceholder) vs.getTag()).story;

		ImageButton b = (ImageButton) vs.findViewById(R.id.story_delete_btn);
		if (story.createdByUser()) {
			b.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					new SCAsyncTask<StoryObject, Void, Boolean>(getActivity(),
							new StoryDeleteProcessor(getActivity())).execute(story);
				}
			});
		}

		b = (ImageButton) vs.findViewById(R.id.story_edit_btn);
		b.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				//load pois of the story
				FragmentTransaction fragmentTransaction = getSherlockActivity()
						.getSupportFragmentManager().beginTransaction();
				Fragment fragment = new CreateStoryFragment();
				Bundle args = new Bundle();
				args.putSerializable(CreateStoryFragment.ARG_STORY, story);
				fragment.setArguments(args);
				fragmentTransaction
						.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
				fragmentTransaction.replace(android.R.id.content, fragment,
						"stories");
				fragmentTransaction.addToBackStack(fragment.getTag());
				fragmentTransaction.commit();
			}
		});

		b = (ImageButton) vs.findViewById(R.id.story_tag_btn);
		b.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				TaggingDialog taggingDialog = new TaggingDialog(getActivity(), new TaggingDialog.OnTagsSelectedListener() {
					
					@SuppressWarnings("unchecked")
					@Override
					public void onTagsSelected(Collection<SemanticSuggestion> suggestions) {
						new TaggingAsyncTask(story).execute(Concept.convertSS(suggestions));
					}
				}, StoriesListingFragment.this, Concept.convertToSS(story.getCommunityData().getTags()));
				taggingDialog.show();

			}
		});
		b = (ImageButton) vs.findViewById(R.id.story_follow_btn);
		b.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				FollowEntityObject obj = new FollowEntityObject(story
						.getEntityId(), story.getTitle(),
						DTConstants.ENTITY_TYPE_STORY);
				FollowHelper.follow(getActivity(), obj);
			}
		});
	}


	private void hideListItemsMenu(View v) {
		boolean toBeHidden = false;
		for (int index = 0; index < list.getChildCount(); index++) {
			View view = list.getChildAt(index);
			if (view instanceof ViewSwitcher
					&& ((ViewSwitcher) view).getDisplayedChild() == 1) {
				((ViewSwitcher) view).showPrevious();
				toBeHidden = true;
			}
		}
		if (!toBeHidden && v != null && v.getTag() != null) {
			// no items needed to be flipped, fill and open details page
			FragmentTransaction fragmentTransaction = getSherlockActivity()
					.getSupportFragmentManager().beginTransaction();
			StoryDetailsFragment fragment = new StoryDetailsFragment();

			Bundle args = new Bundle();
			args.putSerializable(StoryDetailsFragment.ARG_STORY,
					((StoryPlaceholder) v.getTag()).story);
			fragment.setArguments(args);

			fragmentTransaction
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			fragmentTransaction.replace(android.R.id.content, fragment, "stories");
			fragmentTransaction.addToBackStack(fragment.getTag());
			fragmentTransaction.commit();
		}
	}

	/*
	 * get all the stories of a category. Used in combination with a asynch task
	 */
	private List<StoryObject> getStories(AbstractLstingFragment.ListingRequest... params) {
		try {
			Collection<StoryObject> result = null;
			Bundle bundle = getArguments();
			if (bundle == null) {
				return Collections.emptyList();
			} else if (bundle.containsKey(ARG_CATEGORY)) {
				HashSet<String> set = new HashSet<String>(1);
				set.add(bundle.getString(ARG_CATEGORY));
				
				result = DTHelper
						.getStoryByCategory(params[0].position, params[0].size, CategoryHelper.getAllCategories(set));
			}  else if (bundle.containsKey(ARG_MY)) {
			result = DTHelper.getMyStories(params[0].position, params[0].size);
		}else if (bundle.containsKey(ARG_QUERY)) {
				if (bundle.containsKey(ARG_CATEGORY_SEARCH))
				{
					HashSet<String> set = new HashSet<String>(1);
					set.add(bundle.getString(ARG_CATEGORY_SEARCH));
					result = DTHelper.searchStoriesByCategory(params[0].position, params[0].size, bundle.getString(ARG_QUERY),CategoryHelper.getAllCategories(set));
				}
				else
					result = DTHelper.searchStories(params[0].position, params[0].size, bundle.getString(ARG_QUERY));
			} else {
				return Collections.emptyList();
			}

			
			List<StoryObject> sorted = new ArrayList<StoryObject>(result);

			return sorted;
		} catch (Exception e) {
			Log.e(StoriesListingFragment.class.getName(), e.getMessage());
			e.printStackTrace();
			return Collections.emptyList();
		}
	}

	/*
	 * Asynchtask that get all the stories
	 */
	private class StoryLoader extends AbstractAsyncTaskProcessor<AbstractLstingFragment.ListingRequest, List<StoryObject>> {

		public StoryLoader(Activity activity) {
			super(activity);
		}

		@Override
		public List<StoryObject> performAction(AbstractLstingFragment.ListingRequest... params) throws SecurityException, Exception {
			return getStories(params);
		}

		@Override
		public void handleResult(List<StoryObject> result) {
			if (result == null || result.isEmpty()) {
				eu.trentorise.smartcampus.dt.custom.ViewHelper.addEmptyListView((LinearLayout)getView().findViewById(R.id.storylistcontainer));
				
			} else {
				eu.trentorise.smartcampus.dt.custom.ViewHelper.removeEmptyListView((LinearLayout)getView().findViewById(R.id.storylistcontainer));
			}

		}

	}
	
	@Override
	public List<SemanticSuggestion> getTags(CharSequence text) {
		try {
			return DTHelper.getSuggestions(text);
		} catch (Exception e) {
			return Collections.emptyList();
		}
	}


	private class TaggingAsyncTask extends SCAsyncTask<List<Concept>, Void, Void> {
		
		public TaggingAsyncTask(final StoryObject s) {
			super(getSherlockActivity(), new AbstractAsyncTaskProcessor<List<Concept>, Void>(getSherlockActivity()) {
				@Override
				public Void performAction(List<Concept>... params) throws SecurityException, Exception {
					s.getCommunityData().setTags(params[0]);
					DTHelper.saveStory(s);
					return null;
				}
				@Override
				public void handleResult(Void result) {
					Toast.makeText(getSherlockActivity(), getString(R.string.tags_successfully_added), Toast.LENGTH_SHORT).show();
				}
			});
		}
	}

	private class StoryDeleteProcessor extends
			AbstractAsyncTaskProcessor<StoryObject, Boolean> {
		private StoryObject object = null;

		public StoryDeleteProcessor(Activity activity) {
			super(activity);
		}

		@Override
		public Boolean performAction(StoryObject... params)
				throws SecurityException, Exception {
			object = params[0];
			return DTHelper.deleteStory(params[0]);
		}

		@Override
		public void handleResult(Boolean result) {
			if (result) {
				((StoryAdapter) list.getAdapter()).remove(object);
				((StoryAdapter) list.getAdapter()).notifyDataSetChanged();
				hideListItemsMenu(clickedElement);
			} else {
				Toast.makeText(
						getActivity(),
						getActivity().getString(
								R.string.app_failure_cannot_delete),
						Toast.LENGTH_LONG).show();
			}
		}

	}

	@Override
	protected SCAsyncTaskProcessor<AbstractLstingFragment.ListingRequest, List<StoryObject>> getLoader() {
		return new StoryLoader(getActivity());
	}

	@Override
	protected ListView getListView() {
		return list;
	}

	
}
