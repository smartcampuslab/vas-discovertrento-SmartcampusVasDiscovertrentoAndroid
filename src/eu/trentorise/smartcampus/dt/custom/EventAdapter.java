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
package eu.trentorise.smartcampus.dt.custom;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import eu.trentorise.smartcampus.dt.R;
import eu.trentorise.smartcampus.dt.model.EventObject;

// in EventsListingFragment
public class EventAdapter extends ArrayAdapter<EventObject> {

	private Context context;
	private int layoutResourceId;
//	private EventObject[] data;

	public EventAdapter(Context context, int layoutResourceId) {
		super(context, layoutResourceId);
		this.context = context;
		this.layoutResourceId = layoutResourceId;
//		this.data = data;
	}

//	@Override
//	public void remove(EventObject object) {
//		EventObject[] newData = new EventObject[data.length - 1];
//		int i = 0;
//		for (EventObject o : data) {
//			if (i == newData.length)
//				return;
//			if (!o.getId().equals(object.getId())) {
//				newData[i] = o;
//			}
//			i++;
//		}
//		data = newData;
//	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		EventPlaceholder e = null;
		if (row == null) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			row = inflater.inflate(layoutResourceId, parent, false);
			e = new EventPlaceholder();
			e.title = (TextView) row.findViewById(R.id.event_placeholder_title);
			// e.description = (TextView)
			// row.findViewById(R.id.event_placeholder_descr);
			e.location = (TextView) row
					.findViewById(R.id.event_placeholder_loc);
			e.date = (TextView) row.findViewById(R.id.event_placeholder_date);
			row.setTag(e);
		} else
			e = (EventPlaceholder) row.getTag();

		e.event = getItem(position);//data[position];
		e.title.setText(e.event.getTitle());
		// e.description.setText(data[position].getDescription());
		if (e.event.assignedPoi() != null) {
			e.location.setText(e.event.assignedPoi().shortAddress());
		} else {
			e.location.setText(null);
		}
		e.date.setText(e.event.dateTimeString());

		return row;
	}

}
