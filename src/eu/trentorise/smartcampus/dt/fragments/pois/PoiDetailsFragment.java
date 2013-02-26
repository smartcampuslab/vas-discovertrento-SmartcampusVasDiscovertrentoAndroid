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
package eu.trentorise.smartcampus.dt.fragments.pois;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.location.Address;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.google.android.maps.GeoPoint;

import eu.trentorise.smartcampus.dt.R;
import eu.trentorise.smartcampus.android.common.SCAsyncTask;
import eu.trentorise.smartcampus.android.common.follow.FollowEntityObject;
import eu.trentorise.smartcampus.android.common.follow.FollowHelper;
import eu.trentorise.smartcampus.android.common.navigation.NavigationHelper;
import eu.trentorise.smartcampus.android.feedback.fragment.SlidingFragment;
import eu.trentorise.smartcampus.dt.custom.AbstractAsyncTaskProcessor;
import eu.trentorise.smartcampus.dt.custom.RatingHelper;
import eu.trentorise.smartcampus.dt.custom.RatingHelper.RatingHandler;
import eu.trentorise.smartcampus.dt.custom.data.DTHelper;
import eu.trentorise.smartcampus.dt.custom.map.MapManager;
import eu.trentorise.smartcampus.dt.fragments.events.EventsListingFragment;
import eu.trentorise.smartcampus.dt.model.BaseDTObject;
import eu.trentorise.smartcampus.dt.model.Concept;
import eu.trentorise.smartcampus.dt.model.DTConstants;
import eu.trentorise.smartcampus.dt.model.POIObject;
import eu.trentorise.smartcampus.dt.model.TmpComment;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;

public class PoiDetailsFragment extends SherlockFragment {

	public static final String ARG_POI = "poi_object";
	POIObject poi = null;
	private TmpComment tmp_comments[];

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setHasOptionsMenu(true);
		if (getArguments() != null)
			poi = (POIObject) getArguments().getSerializable(ARG_POI);

		tmp_comments = new TmpComment[0];
//		tmp_comments = new TmpComment[5];
		for (int i = 0; i < tmp_comments.length; i++)
			tmp_comments[i] = new TmpComment("This is a comment about the POI",
					"student", new Date());

	}

	private POIObject getPOI() {
		if (poi == null) {
			poi = (POIObject) getArguments().getSerializable(ARG_POI);
		}
		return poi;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.poidetails, container, false);
	}

	private void updateRating(Integer result) {
		getPOI().getCommunityData().setAverageRating(result);
		RatingBar rating = (RatingBar) getView().findViewById(R.id.poi_rating);
		rating.setRating(getPOI().getCommunityData().getAverageRating());
	}

	@Override
	public void onStart() {
		super.onStart();
		if (getPOI() != null) {
			// title
			TextView tv = (TextView) this.getView().findViewById(
					R.id.poi_details_title);
			tv.setText(getPOI().getTitle());

			// description, optional
			tv = (TextView) this.getView().findViewById(R.id.poi_details_descr);
			if (poi.getDescription() != null
					&& poi.getDescription().length() > 0) {
				tv.setText(poi.getDescription());
			} else {
				((LinearLayout) this.getView().findViewById(R.id.poidetails))
						.removeView(tv);
			}

			// notes
			tv = (TextView) this.getView().findViewById(R.id.poi_details_notes);
			if (poi.getCommunityData() != null
					&& poi.getCommunityData().getNotes() != null
					&& poi.getCommunityData().getNotes().length() > 0) {
				tv.setText(poi.getCommunityData().getNotes());
			} else {
				((LinearLayout) this.getView().findViewById(R.id.poidetails))
						.removeView(tv);
			}

			// location
			tv = (TextView) this.getView().findViewById(R.id.poi_details_loc);
			tv.setText(Html.fromHtml("<a href=\"\">"+poi.shortAddress()+"</a> "));	
			tv.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					ArrayList<BaseDTObject> list = new ArrayList<BaseDTObject>();
					list.add(poi);
					MapManager.switchToMapView(list, PoiDetailsFragment.this);					
				}
			});

			// tags
			tv = (TextView) this.getView().findViewById(R.id.poi_details_tags);
			if (poi.getCommunityData() != null
					&& poi.getCommunityData().getTags() != null
					&& poi.getCommunityData().getTags().size() > 0) {
				tv.setText(Concept.toSimpleString(poi.getCommunityData()
						.getTags()));
			} else {
				((LinearLayout) this.getView().findViewById(R.id.poidetails))
						.removeView(tv);
			}

			// multimedia
			((TableRow) getView().findViewById(R.id.tablerow)).removeView(getView().findViewById(R.id.gallery_btn));

	/*		ImageButton b = (ImageButton) getView().findViewById(
					R.id.gallery_btn);
			if (hasMultimediaAttached())
				b.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						FragmentTransaction fragmentTransaction = getSherlockActivity()
								.getSupportFragmentManager().beginTransaction();
						GalleryFragment fragment = new GalleryFragment();
						Bundle args = new Bundle();
						// add args
						args.putString("title", poi.getTitle());
						fragment.setArguments(args);
						fragmentTransaction
								.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
						fragmentTransaction.replace(android.R.id.content,
								fragment, "gallery");
						fragmentTransaction.addToBackStack(fragment.getTag());
						fragmentTransaction.commit();
					}
				});
			else
				((LinearLayout) this.getView().findViewById(R.id.tablerow))
						.removeView(b);
*/
			// source
			tv = (TextView) this.getView()
					.findViewById(R.id.poi_details_source);
			if (poi.getSource() != null && poi.getSource().length() > 0) {
				/* Source is "ou" sometimes O_o */
				tv.setText(poi.getSource());
			} else if (poi.createdByUser()) {
				tv.setText(getString(R.string.source_smartcampus));
			} else {
				((LinearLayout) this.getView().findViewById(R.id.poidetails))
						.removeView(tv);
			}

			// rating
			RatingBar rating = (RatingBar) getView().findViewById(
					R.id.poi_rating);
			rating.setOnTouchListener(new View.OnTouchListener() {
				
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_UP) {
						ratingDialog();
					}
					return true;
				}
			});
			if (poi.getCommunityData() != null) {
				rating.setRating(poi.getCommunityData().getAverageRating());
			}

			if (tmp_comments.length > 0) {
				// Comments
				LinearLayout commentsList = (LinearLayout) getView().findViewById(
						R.id.comments_list);
				for (int i = 0; i < tmp_comments.length; i++) {
					View entry = getLayoutInflater(this.getArguments()).inflate(
							R.layout.comment_row, null);

					TextView tmp = (TextView) entry.findViewById(R.id.comment_text);
					tmp.setText(tmp_comments[i].getText());
					tmp = (TextView) entry.findViewById(R.id.comment_author);
					tmp.setText(tmp_comments[i].getAuthor());
					tmp = (TextView) entry.findViewById(R.id.comment_date);
					tmp.setText(tmp_comments[i].getDate());
					commentsList.addView(entry);
				}
			} else {
				((LinearLayout) getView().findViewById(R.id.poidetails)).removeView(getView().findViewById(R.id.poi_comments));
				((LinearLayout) getView().findViewById(R.id.poidetails)).removeView(getView().findViewById(R.id.comments_list));
				((LinearLayout) getView().findViewById(R.id.poidetails)).removeView(getView().findViewById(R.id.poi_comments_separator));
			}
			
		}
	}

	private boolean hasMultimediaAttached() {
		return true;
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		getSherlockActivity().getSupportMenuInflater().inflate(R.menu.gripmenu, menu);

		SubMenu submenu = menu.getItem(0).getSubMenu();
		submenu.clear();
		submenu.add(Menu.CATEGORY_SYSTEM, R.id.rate, Menu.NONE, R.string.rate);
		submenu.add(Menu.CATEGORY_SYSTEM, R.id.follow, Menu.NONE,
				R.string.follow);
		submenu.add(Menu.NONE, R.id.show_related_events, Menu.NONE,
				R.string.related_events);
		submenu.add(Menu.NONE, R.id.get_dir, Menu.NONE, R.string.getdir);
		submenu.add(Menu.NONE, R.id.see_on_map, Menu.NONE, R.string.onmap);
		submenu.add(Menu.CATEGORY_SYSTEM, R.id.edit_btn, Menu.NONE,
				R.string.edit);
		// CAN DELETE ONLY OWN OBJECTS
		if (DTHelper.isOwnedObject(getPOI())) {
			submenu.add(Menu.CATEGORY_SYSTEM, R.id.delete_btn, Menu.NONE,
					R.string.delete);
		}

		super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		SlidingFragment sl = (SlidingFragment) getActivity().getSupportFragmentManager()
				.findFragmentById(R.id.feedback_fragment_container);
		
		switch (item.getItemId()) {
		case R.id.show_related_events: {
			/*
			 * It would be great if we could already know whether there are any
			 * events related to this POI: in case there were not, this entry in
			 * the menu could be omitted
			 */
			/*Gio comment
			 * FragmentTransaction fragmentTransaction = getFragmentManager()
					.beginTransaction();
			EventsListingFragment fragment = new EventsListingFragment();
			Bundle args = new Bundle();
			args.putString(EventsListingFragment.ARG_POI, poi.getId());
			args.putString(EventsListingFragment.ARG_POI_NAME, poi.getTitle());
			fragment.setArguments(args);
			fragmentTransaction
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			// fragmentTransaction.detach(this);
			fragmentTransaction.replace(android.R.id.content, fragment, "pois");
			fragmentTransaction.addToBackStack(fragment.getTag());
			fragmentTransaction.commit();*/
			EventsListingFragment fragment = new EventsListingFragment();
			Bundle args = new Bundle();
			args.putString(EventsListingFragment.ARG_POI, poi.getId());
			args.putString(EventsListingFragment.ARG_POI_NAME, poi.getTitle());
			fragment.setArguments(args);
			sl.replaceFragmentWithTransition(fragment,
					FragmentTransaction.TRANSIT_FRAGMENT_FADE, true, "Places");
			return true;
		}
		case R.id.get_dir:
			Address to = poi.asGoogleAddress();
			Address from = null;
			GeoPoint mylocation = MapManager.requestMyLocation(getActivity());
			if (mylocation != null) {
				from = new Address(Locale.getDefault());
				from.setLatitude(mylocation.getLatitudeE6()/1E6);
				from.setLongitude(mylocation.getLongitudeE6()/1E6);
			}
			
			NavigationHelper.bringMeThere(getActivity(), from, to);
			return true;
		case R.id.see_on_map: {
			ArrayList<BaseDTObject> list = new ArrayList<BaseDTObject>();
			list.add(poi);
			MapManager.switchToMapView(list, this);
			return true;
		}
		case R.id.follow:
			FollowEntityObject obj = new FollowEntityObject(poi.getEntityId(),
					poi.getTitle(), DTConstants.ENTITY_TYPE_POI);
			FollowHelper.follow(getActivity(), obj);
			return true;
		case R.id.rate:
			ratingDialog();
			return true;

		case R.id.edit_btn:
			/*Gio comment
			 * FragmentTransaction fragmentTransaction = getSherlockActivity()
					.getSupportFragmentManager().beginTransaction();
			Fragment fragment = new CreatePoiFragment();
			Bundle args = new Bundle();
			args.putSerializable(CreatePoiFragment.ARG_POI, poi);
			fragment.setArguments(args);
			fragmentTransaction
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			// fragmentTransaction.detach(this);
			fragmentTransaction.replace(android.R.id.content, fragment, "pois");
			fragmentTransaction.addToBackStack(fragment.getTag());
			fragmentTransaction.commit();*/
			Fragment fragment = new CreatePoiFragment();
			Bundle args = new Bundle();
			args.putSerializable(CreatePoiFragment.ARG_POI, poi);
			fragment.setArguments(args);
			sl.replaceFragmentWithTransition(fragment,
					FragmentTransaction.TRANSIT_FRAGMENT_FADE, true, "Places");
			return true;
		case R.id.delete_btn:
			new SCAsyncTask<POIObject, Void, Boolean>(getActivity(),
					new POIDeleteProcessor(getActivity())).execute(poi);
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void ratingDialog() {
		float rating = (poi != null && poi.getCommunityData() != null && poi
				.getCommunityData().getAverageRating() > 0) ? poi
				.getCommunityData().getAverageRating() : 2.5f;
		RatingHelper.ratingDialog(getActivity(), rating,
				new RatingProcessor(getActivity()));
	}

	private class RatingProcessor extends
			AbstractAsyncTaskProcessor<Integer, Integer> implements
			RatingHandler {

		public RatingProcessor(Activity activity) {
			super(activity);
		}

		@Override
		public Integer performAction(Integer... params)
				throws SecurityException, Exception {
			return DTHelper.rate(poi, params[0]);
		}

		@Override
		public void handleResult(Integer result) {
			updateRating(result);
			Toast.makeText(getSherlockActivity(), R.string.rating_success, Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onRatingChanged(float rating) {
			new SCAsyncTask<Integer, Void, Integer>(getActivity(), this)
					.execute((int) rating);
		}
	}

	private class POIDeleteProcessor extends
			AbstractAsyncTaskProcessor<POIObject, Boolean> {
		public POIDeleteProcessor(Activity activity) {
			super(activity);
		}

		@Override
		public Boolean performAction(POIObject... params)
				throws SecurityException, Exception {
			return DTHelper.deletePOI(params[0]);
		}

		@Override
		public void handleResult(Boolean result) {
			if (result) {
				getSherlockActivity().getSupportFragmentManager().popBackStack(
						"pois", FragmentManager.POP_BACK_STACK_INCLUSIVE);
			} else {
				Toast.makeText(
						getActivity(),
						getActivity().getString(
								R.string.app_failure_cannot_delete),
						Toast.LENGTH_LONG).show();
			}
		}

	}

}
