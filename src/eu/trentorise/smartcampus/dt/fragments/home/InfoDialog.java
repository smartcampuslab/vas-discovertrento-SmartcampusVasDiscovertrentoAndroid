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
package eu.trentorise.smartcampus.dt.fragments.home;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockDialogFragment;

import eu.trentorise.smartcampus.dt.R;
import eu.trentorise.smartcampus.dt.custom.data.DTHelper;
import eu.trentorise.smartcampus.dt.fragments.events.EventDetailsFragment;
import eu.trentorise.smartcampus.dt.fragments.pois.PoiDetailsFragment;
import eu.trentorise.smartcampus.dt.model.BaseDTObject;
import eu.trentorise.smartcampus.dt.model.EventObject;
import eu.trentorise.smartcampus.dt.model.POIObject;

public class InfoDialog extends SherlockDialogFragment {
	private BaseDTObject data;

	public InfoDialog(BaseDTObject o) {
		this.data = o;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		getDialog().setTitle(data.getTitle());
		return inflater.inflate(R.layout.mapdialog, container, false);
	}

	@Override
	public void onStart() {
		super.onStart();
		TextView msg = (TextView) getDialog().findViewById(R.id.mapdialog_msg);
		if (data.getDescription() != null)
			msg.setText(data.getFormattedDescription());
		else {
			if (data instanceof POIObject)
				msg.setText(((POIObject) data).shortAddress());
			else {
				POIObject poi = DTHelper.findPOIById(((EventObject) data)
						.getPoiId());
				msg.setText(poi.shortAddress());
			}
		}
		msg.setMovementMethod(new ScrollingMovementMethod());
		Button b = (Button) getDialog().findViewById(R.id.mapdialog_cancel);
		b.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				getDialog().dismiss();
			}
		});
		b = (Button) getDialog().findViewById(R.id.mapdialog_ok);
		b.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				FragmentTransaction fragmentTransaction = getSherlockActivity()
						.getSupportFragmentManager().beginTransaction();
				Bundle args = new Bundle();

				if (data instanceof POIObject) {
					PoiDetailsFragment fragment = new PoiDetailsFragment();
					args.putSerializable(PoiDetailsFragment.ARG_POI, data);
					fragment.setArguments(args);
					fragmentTransaction
							.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
					fragmentTransaction.replace(android.R.id.content, fragment,
							"me");
					fragmentTransaction.addToBackStack(fragment.getTag());
				} else {
					EventDetailsFragment fragment = new EventDetailsFragment();
					args.putSerializable(EventDetailsFragment.ARG_EVENT_OBJECT,
							data);
					fragment.setArguments(args);
					fragmentTransaction
							.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
					fragmentTransaction.replace(android.R.id.content, fragment,
							"me");
					fragmentTransaction.addToBackStack(fragment.getTag());
				}
				fragmentTransaction.commit();
				getDialog().dismiss();
			}
		});

	}
}
