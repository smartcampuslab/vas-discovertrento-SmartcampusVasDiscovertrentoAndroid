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

import java.text.ParseException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;

import eu.trentorise.smartcampus.dt.R;
import eu.trentorise.smartcampus.android.common.SCAsyncTask;
import eu.trentorise.smartcampus.android.common.tagging.SemanticSuggestion;
import eu.trentorise.smartcampus.android.common.tagging.TaggingDialog;
import eu.trentorise.smartcampus.dt.custom.AbstractAsyncTaskProcessor;
import eu.trentorise.smartcampus.dt.custom.CategoryHelper;
import eu.trentorise.smartcampus.dt.custom.DatePickerDialogFragment;
import eu.trentorise.smartcampus.dt.custom.TimePickerDialogFragment;
import eu.trentorise.smartcampus.dt.custom.data.DTHelper;
import eu.trentorise.smartcampus.dt.fragments.pois.CreatePoiFragment;
import eu.trentorise.smartcampus.dt.fragments.pois.PoisListingFragment;
import eu.trentorise.smartcampus.dt.fragments.pois.CreatePoiFragment.PoiHandler;
import eu.trentorise.smartcampus.dt.fragments.stories.AddStepToStoryFragment;
import eu.trentorise.smartcampus.dt.model.CommunityData;
import eu.trentorise.smartcampus.dt.model.Concept;
import eu.trentorise.smartcampus.dt.model.EventObject;
import eu.trentorise.smartcampus.dt.model.POIObject;
import eu.trentorise.smartcampus.dt.model.UserEventObject;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;

public class CreateEventFragment extends SherlockFragment implements TaggingDialog.OnTagsSelectedListener, TaggingDialog.TagProvider {

	private POIObject poi = null;
	private View view = null;
	private CreatePoiFromEvent poiHandler = new CreatePoiFromEvent();
	private AutoCompleteTextView poiField; 
	public static String ARG_EVENT = "event";
	
	private EventObject eventObject;
	
	@Override
	public void onTagsSelected(Collection<SemanticSuggestion> suggestions) {
		eventObject.getCommunityData().setTags(Concept.convertSS(suggestions));
		((EditText)getView().findViewById(R.id.event_tags)).setText(Concept.toSimpleString(eventObject.getCommunityData().getTags()));	
	}
	
	@Override
	public void onSaveInstanceState(Bundle arg0) {
		super.onSaveInstanceState(arg0);
		arg0.putSerializable(ARG_EVENT, eventObject);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(false);
		
		if (savedInstanceState != null && savedInstanceState.containsKey(ARG_EVENT) && savedInstanceState.getSerializable(ARG_EVENT) != null) {
			eventObject = (EventObject)savedInstanceState.get(ARG_EVENT);
		} else if (getArguments() != null && getArguments().containsKey(ARG_EVENT) && getArguments().getSerializable(ARG_EVENT) != null) {
			eventObject = (EventObject)getArguments().getSerializable(ARG_EVENT);
		} else  {
			eventObject = new UserEventObject();
			if (getArguments() != null && getArguments().containsKey(EventsListingFragment.ARG_CATEGORY))
			{
				eventObject.setType(getArguments().getString(EventsListingFragment.ARG_CATEGORY));
			}
		}
		if (eventObject.getPoiId() != null) {
			poi = DTHelper.findPOIById(eventObject.getPoiId());

		}
		if (eventObject.getCommunityData() == null) eventObject.setCommunityData(new CommunityData());
	}


	@Override
	public void onStart() {
		super.onStart(); 
		
		// date and time will be returned as tags
		final EditText dateEditText = (EditText)getView().findViewById(R.id.event_date);
		dateEditText.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				DialogFragment f = DatePickerDialogFragment
						.newInstance((EditText) v);
				if (dateEditText.getText() != null)
					f.setArguments(DatePickerDialogFragment.prepareData(dateEditText.getText().toString()));
				f.show(getSherlockActivity().getSupportFragmentManager(),
						"datePicker");
			}
		});
		final EditText startTime = (EditText)getView().findViewById(R.id.event_start);
		startTime.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				DialogFragment newFragment = TimePickerDialogFragment
						.newInstance((EditText) v);
				if (startTime != null) 
					newFragment.setArguments(TimePickerDialogFragment.prepareData(startTime.getText().toString()));
				newFragment.show(getSherlockActivity()
						.getSupportFragmentManager(), "timePicker");
			}
		});

		final EditText endTime = (EditText) getView().findViewById(R.id.event_end);
		endTime.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				DialogFragment newFragment = TimePickerDialogFragment
						.newInstance((EditText) v);
				if (endTime != null) 
					newFragment.setArguments(TimePickerDialogFragment.prepareData(endTime.getText().toString()));
				newFragment.show(getSherlockActivity()
						.getSupportFragmentManager(), "timePicker");
			}
		});
		if (poi != null) {
			poiField.setText(poi.getTitle());
		}
	
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.createeventform, container, false);

		Spinner categories = (Spinner) view.findViewById(R.id.event_category);
		int selected = 0;
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),R.layout.dd_list, R.id.dd_textview);;
		categories.setAdapter(adapter);
		for (int i = 0; i < CategoryHelper.EVENT_CATEGORIES.length; i++) {
			adapter.add(CategoryHelper.EVENT_CATEGORIES[i].category);
			if (CategoryHelper.EVENT_CATEGORIES[i].category.equals(eventObject.getType())) selected = i;
		}
		categories.setSelection(selected);

		EditText title = (EditText) view.findViewById(R.id.event_title);
		title.setText(eventObject.getTitle());

		EditText dateEdit = (EditText)view.findViewById(R.id.event_date);
		EditText startTimeEdit = (EditText)view.findViewById(R.id.event_start);
		EditText endTimeEdit = (EditText)view.findViewById(R.id.event_end);
		if (eventObject.getFromTime() != null && eventObject.getFromTime() > 0) {
			dateEdit.setText(DatePickerDialogFragment.DATEFORMAT.format(new Date(eventObject.getFromTime())));
			startTimeEdit.setText(TimePickerDialogFragment.TIMEFORMAT.format(new Date(eventObject.getFromTime())));
		} else {
			Calendar c = Calendar.getInstance();
			c.set(Calendar.MINUTE, 0);
			dateEdit.setText(DatePickerDialogFragment.DATEFORMAT.format(c.getTime()));
			startTimeEdit.setText(TimePickerDialogFragment.TIMEFORMAT.format(c.getTime()));
		}
		if (eventObject.getToTime() != null && eventObject.getToTime() > 0) {
			endTimeEdit.setText(TimePickerDialogFragment.TIMEFORMAT.format(new Date(eventObject.getToTime())));
		} else {
			Calendar c = Calendar.getInstance();
			c.set(Calendar.MINUTE, 0);
			endTimeEdit.setText(TimePickerDialogFragment.TIMEFORMAT.format(c.getTime()));
		}
		
		poiField = (AutoCompleteTextView) view.findViewById(R.id.event_place);
		ArrayAdapter<String> poiAdapter = new ArrayAdapter<String>(
				getSherlockActivity(),
				R.layout.dd_list,
				R.id.dd_textview,
				DTHelper.getAllPOITitles());
		poiField.setAdapter(poiAdapter);
		if (poi != null) {
			poiField.setText(poi.getTitle());
		}
	
		ImageButton addPoiBtn = (ImageButton) view.findViewById(R.id.btn_event_add_poi);
		addPoiBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				FragmentTransaction fragmentTransaction;
				Fragment fragment;

				fragmentTransaction = getSherlockActivity().getSupportFragmentManager().beginTransaction();
				fragment = new CreatePoiFragment();
				Bundle args = new Bundle();
				args.putParcelable(AddStepToStoryFragment.ARG_STEP_HANDLER,
						poiHandler);
				fragment.setArguments(args);				
				fragmentTransaction
						.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
				fragmentTransaction.replace(android.R.id.content, fragment, "pois");
				fragmentTransaction.addToBackStack(fragment.getTag());
				fragmentTransaction.commit();
			}
		});

		ImageButton locationBtn = (ImageButton) view.findViewById(R.id.btn_event_locate);
		locationBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivityForResult(new Intent(getActivity(),
						POISelectActivity.class),
						POISelectActivity.RESULT_SELECTED);
			}
		});

		if (!eventObject.createdByUser()) {
			title.setEnabled(false);
			if (eventObject.getType() != null && !eventObject.isTypeUserDefined()) {
				categories.setEnabled(false);
			}
			if (eventObject.getFromTime() != null && eventObject.getFromTime() > 0 && !eventObject.isFromTimeUserDefined()) {
				dateEdit.setEnabled(false);
				startTimeEdit.setEnabled(false);
			}
			if (eventObject.getToTime() != null && eventObject.getToTime() > 0 && !eventObject.isToTimeUserDefined()) {
				endTimeEdit.setEnabled(false);
			}
			if (poi != null && !eventObject.isPoiIdUserDefined()) {
				poiField.setEnabled(false);
				locationBtn.setEnabled(false);
			}
		}

		
		EditText notes = (EditText)view.findViewById(R.id.event_notes);
		notes.setText(eventObject.getCommunityData().getNotes());

		EditText tagsEdit = (EditText) view.findViewById(R.id.event_tags); 
		tagsEdit.setText(Concept.toSimpleString(eventObject.getCommunityData().getTags()));
		tagsEdit.setClickable(true);
		tagsEdit.setFocusableInTouchMode(false);
		tagsEdit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				TaggingDialog taggingDialog = new TaggingDialog(getActivity(), CreateEventFragment.this, CreateEventFragment.this, Concept.convertToSS(eventObject.getCommunityData().getTags()));
				taggingDialog.show();
			}
		});

		Button cancel = (Button) view.findViewById(R.id.btn_createevent_cancel);
		cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getSherlockActivity().getSupportFragmentManager().popBackStack();
			}
			
		});

		Button save = (Button) view.findViewById(R.id.btn_createevent_ok);
		save.setOnClickListener(new SaveEvent());

		return view;
	}

	private Integer validate(EventObject data) {
		Integer result = null;
		if (data.getTitle() == null || data.getTitle().length() == 0)
			return R.string.create_title;
//		if (data.getFromTime() == null)
//			return R.string.createevent_timestart;
//		if (data.getToTime() == null)
//			return R.string.createevent_timeend;
//		if (data.getToTime() <= data.getFromTime())
//			return R.string.createevent_timeend;
		if (data.getPoiId() == null)
			return R.string.create_place;
		if (data.getType() == null || data.getType().length() == 0)
			return R.string.create_cat;
		return result;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent result) {
		super.onActivityResult(requestCode, resultCode, result);
		if (resultCode == POISelectActivity.RESULT_SELECTED) {
			poi = (POIObject) result.getSerializableExtra("poi");
			AutoCompleteTextView text = (AutoCompleteTextView) view
					.findViewById(R.id.event_place);
			text.setText(poi.getTitle());
			for (int i = 0; i < text.getAdapter().getCount(); i++) {
				if (poi.getTitle().equals((text.getAdapter().getItem(i)))) {
					text.setListSelection(i);
				}
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

	// @Override
	// public void onPrepareOptionsMenu(Menu menu) {
	// MenuItem item = menu.add(Menu.CATEGORY_SYSTEM,
	// R.id.menu_item_allevents, 1, R.string.allevents);
	// item.setIcon(R.drawable.ic_event_all);
	// item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
	//
	// item = menu.add(Menu.CATEGORY_SYSTEM, R.id.menu_item_myevents, 2,
	// R.string.menu_item_myevents_text);
	// item.setIcon(R.drawable.ic_myevents);
	// item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
	//
	// super.onPrepareOptionsMenu(menu);
	// }
	//
	// @Override
	// public boolean onOptionsItemSelected(MenuItem item) {
	// FragmentTransaction fragmentTransaction;
	// Fragment fragment;
	// switch (item.getItemId()) {
	//
	// case R.id.menu_item_allevents:
	// fragmentTransaction = fragmentManager.beginTransaction();
	// fragment = new AllEventsFragment();
	// fragmentTransaction
	// .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
	// fragmentTransaction.detach(this);
	// fragmentTransaction.replace(R.id.mainlayout, fragment);
	// // fragmentTransaction.addToBackStack(null);
	// fragmentTransaction.commit();
	// return true;
	//
	// case R.id.menu_item_myevents:
	// fragmentTransaction = fragmentManager.beginTransaction();
	// fragment = new EventsListingFragment();
	// fragmentTransaction
	// .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
	// fragmentTransaction.detach(this);
	// fragmentTransaction.replace(R.id.mainlayout, fragment);
	// // fragmentTransaction.addToBackStack(null);
	// fragmentTransaction.commit();
	// return true;
	//
	// default:
	// return super.onOptionsItemSelected(item);
	// }
	// }
	
	private class CreateEventProcessor extends AbstractAsyncTaskProcessor<EventObject, Boolean> {

		public CreateEventProcessor(Activity activity) {
			super(activity);
		}

		@Override
		public Boolean performAction(EventObject... params) throws SecurityException, Exception {
			return DTHelper.saveEvent(params[0]);
		}

		@Override
		public void handleResult(Boolean result) {
			getSherlockActivity().getSupportFragmentManager().popBackStack();
			if (result) {
				Toast.makeText(getSherlockActivity(), R.string.event_create_success, Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(getSherlockActivity(), R.string.update_success, Toast.LENGTH_SHORT).show();
			}
		}
		
	}
	
	private class SaveEvent implements OnClickListener{
		@Override
		public void onClick(View v) {
			CharSequence desc = ((EditText) view.findViewById(R.id.event_notes)).getText();
			if (desc != null) {
				eventObject.getCommunityData().setNotes(desc.toString());
			}
			CharSequence title = ((EditText) view.findViewById(R.id.event_title)).getText();
			if (title != null) {
				eventObject.setTitle(title.toString());
			}
			String cat = ((Spinner) view.findViewById(R.id.event_category)).getSelectedItem().toString();

			AutoCompleteTextView eventPlace = (AutoCompleteTextView) view.findViewById(R.id.event_place);
			if ((poi == null || !poi.getTitle().equals(eventPlace.getText().toString()))
					&& eventPlace.getText() != null && eventPlace.getText().length() > 0) 
			{
				poi = DTHelper.findPOIByTitle(eventPlace.getText().toString());
			}

			CharSequence datestr = ((EditText) view.findViewById(R.id.event_date)).getText();
			CharSequence fromstr = ((EditText) view.findViewById(R.id.event_start)).getText();
			CharSequence tostr = ((EditText) view.findViewById(R.id.event_end)).getText();

			if (datestr == null || datestr.length() == 0) {
				Toast.makeText(
						getActivity(),
						getActivity().getResources().getString(R.string.createevent_date) + " is required.", Toast.LENGTH_SHORT)
						.show();
				return;
			}
			if (fromstr == null || fromstr.length() == 0) {
				Toast.makeText(
						getActivity(),
						getActivity().getResources().getString(R.string.createevent_timestart) + " is required.", Toast.LENGTH_SHORT)
						.show();
				return;
			}
			if (tostr == null || tostr.length() == 0) {
				Toast.makeText(
						getActivity(),
						getActivity().getResources().getString(R.string.createevent_timeend) + " is required.", Toast.LENGTH_SHORT)
						.show();
				return;
			}
			
			Calendar cal = Calendar.getInstance();
			Calendar start = Calendar.getInstance();
			Calendar end = Calendar.getInstance();
			try {
				Date d = DatePickerDialogFragment.DATEFORMAT.parse(datestr.toString());
				cal.setTime(d);
			} catch (ParseException e) {
				Toast.makeText(
						getActivity(),
						"Incorrect "
								+ getResources().getString(
										R.string.createevent_date),
						Toast.LENGTH_SHORT).show();
				return;
			}
			try {
				Date d = TimePickerDialogFragment.TIMEFORMAT.parse(fromstr.toString());
				start.setTime(d);
			} catch (ParseException e) {
				Toast.makeText(
						getActivity(),
						"Incorrect "
								+ getResources().getString(
										R.string.createevent_timestart),
						Toast.LENGTH_SHORT).show();
				return;
			}
			try {
				Date d = TimePickerDialogFragment.TIMEFORMAT.parse(tostr.toString());
				end.setTime(d);
			} catch (ParseException e) {
				Toast.makeText(
						getActivity(),
						"Incorrect "
								+ getResources().getString(
										R.string.createevent_timeend),
						Toast.LENGTH_SHORT).show();
				return;
			}
			start.set(Calendar.YEAR, cal.get(Calendar.YEAR));
			start.set(Calendar.MONTH, cal.get(Calendar.MONTH));
			start.set(Calendar.DATE, cal.get(Calendar.DATE));
			end.set(Calendar.YEAR, cal.get(Calendar.YEAR));
			end.set(Calendar.MONTH, cal.get(Calendar.MONTH));
			end.set(Calendar.DATE, cal.get(Calendar.DATE));
			
			if (end.before(start)) {
				end.add(Calendar.DATE, 1);
			}

			
			eventObject.setFromTime(start.getTimeInMillis());
			eventObject.setToTime(end.getTimeInMillis());

			eventObject.setType(cat);
			if (poi != null) {
				eventObject.setPoiId(poi.getId());
			}
			Integer missing = validate(eventObject);
			if (missing != null) {
				Toast.makeText(
						getActivity(),
						getActivity().getResources().getString(missing)
								+ " is required.", Toast.LENGTH_SHORT)
						.show();
				return;
			}
			
			new SCAsyncTask<EventObject, Void, Boolean>(getActivity(),new CreateEventProcessor(getActivity())).execute(eventObject);
		}

	}
	
	private class CreatePoiFromEvent implements PoiHandler, Parcelable {


		private static final long serialVersionUID = 16774297617446649L;

		@Override
		public void addPoi(POIObject poi) {
			

			//add the step, notify to the adapter and go back to this fragment
			CreateEventFragment.this.poi=poi;
			CreateEventFragment.this.eventObject.assignPoi(poi);
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {

		}



	}
}
