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

import java.util.LinkedList;
import java.util.List;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;

import eu.trentorise.smartcampus.dt.R;
import eu.trentorise.smartcampus.dt.custom.data.DTHelper;
import eu.trentorise.smartcampus.dt.model.Concept;
import eu.trentorise.smartcampus.dt.model.EventObject;
import eu.trentorise.smartcampus.dt.model.POIObject;

public class ImportFragment extends SherlockFragment {
	private AlertDialog self;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.importform, container, false);
	}

	@Override
	public void onStart() {
		super.onStart();

		Button b = (Button) getView().findViewById(R.id.browse_btn);
		b.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// to be replaced either by intent to file browsing app or by
				// call to custom activity
				Toast.makeText(getActivity(), "Browsing your files...",
						Toast.LENGTH_SHORT).show();
			}
		});

		b = (Button) getView().findViewById(R.id.import_btn_ok);
		b.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// to be replaced with list of events parsed from file
				List<EventObject> list = new LinkedList<EventObject>();
				list.add(new EventObject());
				list.add(new EventObject());
				onParsingCompleted(list);
			}
		});
	}

	private void onParsingCompleted(List<EventObject> list) {
		for (EventObject e : list) {
			AlertDialog dialog = new AlertDialog.Builder(getActivity())
					.setView(
							getActivity().getLayoutInflater().inflate(
									R.layout.import_dialog, null)).create();
			dialog.setTitle("Event found:");
			dialog.show();
			EditText et;Button b;
			self = dialog;
			
			// event title
			if (e.getTitle() != null) {
				et = (EditText) dialog.findViewById(R.id.import_title);
				et.setText(e.getTitle());
			}
			
			// event date
			//if (e.dateTimeString() != null) {et = (EditText) dialog.findViewById(R.id.import_date);et.setText(e.dateTimeString());}
			// starting time
			// if(e.getFromTime()!=null){et=(EditText)dialog.findViewById(R.id.import_start);et.setText(e.getFromTime().toString());}
			// ending time
			// if(e.getToTime()!=null){et=(EditText)dialog.findViewById(R.id.import_end);et.setText(e.getToTime().toString());}
			
			// location
			if (e.getPoiId() != null) {
				POIObject poi = DTHelper.findPOIById(e.getPoiId());
				if (poi != null) {
					et = (EditText) dialog.findViewById(R.id.import_place);
					et.setText(poi.shortAddress());
				}
			}
			// tags
			if (e.getCommunityData().getTags() != null) {
				et = (EditText) dialog.findViewById(R.id.import_tags);
				et.setText(Concept.toSimpleString(e.getCommunityData()
						.getTags()));
			}
			// categories
			if (e.getType() != null) {
				et = (EditText) dialog.findViewById(R.id.import_category);
				et.setText(e.getType());
			}
			// notes
			if (e.getCommunityData().getNotes() != null) {
				et = (EditText) dialog.findViewById(R.id.import_notes);
				et.setText(e.getCommunityData().getNotes());
			}
			
			b = (Button) dialog.findViewById(R.id.btn_import_ok);
			b.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// save new event and handle "apply to all"
					Toast.makeText(getActivity(), "Event saved!",
							Toast.LENGTH_SHORT).show();
					self.dismiss();
				}
			});
			b = (Button) dialog.findViewById(R.id.btn_import_cancel);
			b.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					self.dismiss();
				}
			});

		}
	}
}
