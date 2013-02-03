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

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

import eu.trentorise.smartcampus.dt.R;
import eu.trentorise.smartcampus.android.common.SCAsyncTask;
import eu.trentorise.smartcampus.dt.custom.data.DTHelper;
import eu.trentorise.smartcampus.dt.custom.map.BaseDTObjectMapItemTapListener;
import eu.trentorise.smartcampus.dt.custom.map.DTItemizedOverlay;
import eu.trentorise.smartcampus.dt.custom.map.MapItemsHandler;
import eu.trentorise.smartcampus.dt.custom.map.MapLayerDialogHelper;
import eu.trentorise.smartcampus.dt.custom.map.MapLoadProcessor;
import eu.trentorise.smartcampus.dt.custom.map.MapManager;
import eu.trentorise.smartcampus.dt.model.BaseDTObject;

public class HomeFragment extends SherlockFragment implements MapItemsHandler, BaseDTObjectMapItemTapListener {

	public static final String ARG_OBJECTS = "objects";
	public static final String ARG_CATEGORY = "category";
	protected ViewGroup mapContainer;
	protected MapView mapView;
	DTItemizedOverlay mItemizedoverlay = null;
	MyLocationOverlay mMyLocationOverlay = null;

	private Context context;
	private String[] categories = null;

	@Override
	public void onStart() {
		super.onStart();
		// hide keyboard if it is still open
		InputMethodManager imm = (InputMethodManager) getSherlockActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(mapView.getWindowToken(), 0);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.context = this.getSherlockActivity();
		setHasOptionsMenu(true);
	}

	@SuppressWarnings("unchecked")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		boolean initialized = mapView != null;
		mapContainer = new FrameLayout(getActivity());
		mapView = MapManager.getMapView();

		final ViewGroup parent = (ViewGroup) mapView.getParent();
		if (parent != null) {
			parent.removeView(mapView);
		}
		mapContainer.addView(mapView);

		List<Overlay> listOfOverlays = mapView.getOverlays();
		mapView.getOverlays().clear();

		mItemizedoverlay = new DTItemizedOverlay(getActivity(), mapView);
		mItemizedoverlay.setMapItemTapListener(this);
		listOfOverlays.add(mItemizedoverlay);

		mMyLocationOverlay = new MyLocationOverlay(getSherlockActivity(), mapView) {
			@Override
			protected void drawMyLocation(Canvas canvas, MapView mapView, Location lastFix, GeoPoint myLocation, long when) {
				Projection p = mapView.getProjection();
				float accuracy = p.metersToEquatorPixels(lastFix.getAccuracy());
				Point loc = p.toPixels(myLocation, null);
				Paint paint = new Paint();
				paint.setAntiAlias(true);
				// paint.setColor(Color.BLUE);
				paint.setColor(Color.parseColor(context.getResources().getString(R.color.appcolor)));

				if (accuracy > 10.0f) {
					paint.setAlpha(50);
					canvas.drawCircle(loc.x, loc.y, accuracy, paint);
					// border
					paint.setAlpha(200);
					paint.setStyle(Paint.Style.STROKE);
					canvas.drawCircle(loc.x, loc.y, accuracy, paint);
				}

				Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.me).copy(
						Bitmap.Config.ARGB_8888, true);
				canvas.drawBitmap(bitmap, loc.x - (bitmap.getWidth() / 2), loc.y - bitmap.getHeight(), null);
			}
		};
		listOfOverlays.add(mMyLocationOverlay);

		if (!initialized) {
			// TODO correct for final version
			mapView.getController().animateTo(MapManager.trento());
			mapView.getController().setZoom(MapManager.ZOOM_DEFAULT);
		}

		if (getArguments() != null && getArguments().containsKey(ARG_OBJECTS)) {
			final List<BaseDTObject> list = (List<BaseDTObject>) getArguments().getSerializable(ARG_OBJECTS);
			MapManager.fitMap(list, mapView);
			new SCAsyncTask<Void, Void, Collection<? extends BaseDTObject>>(getActivity(), new MapLoadProcessor(getActivity(),
					mItemizedoverlay, mapView) {
				@Override
				protected Collection<? extends BaseDTObject> getObjects() {
					try {
						return list;
					} catch (Exception e) {
						e.printStackTrace();
						return Collections.emptyList();
					}
				}
			}).execute();
		} else if (getArguments() != null && getArguments().containsKey(ARG_CATEGORY)) {
			setPOICategoriesToLoad(getArguments().getString(ARG_CATEGORY));
		} else if (categories != null) {
			setPOICategoriesToLoad(categories);
		}

		return mapContainer;
	}

	@Override
	public void onResume() {
		mMyLocationOverlay.enableMyLocation();
		super.onResume();
	}

	@Override
	public void onPause() {
		mMyLocationOverlay.disableMyLocation();
		super.onPause();
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		MenuItem item = menu.add(Menu.CATEGORY_SYSTEM, R.id.menu_item_showlayers, 1, R.string.menu_item_layers_text);
		item.setIcon(R.drawable.layers);
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_item_showlayers:
			// LayerDialogFragment dialogFragment = new
			// LayerDialogFragment(this,
			// categories);
			// dialogFragment.show(getSherlockActivity()
			// .getSupportFragmentManager(), "dialog");
			MapLayerDialogHelper.createDialog(getActivity(), this, getString(R.string.layers_title), categories).show();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void setPOICategoriesToLoad(final String... categories) {
		this.categories = categories;
		mItemizedoverlay.clearMarkers();

		new SCAsyncTask<Void, Void, Collection<? extends BaseDTObject>>(getActivity(), new MapLoadProcessor(getActivity(),
				mItemizedoverlay, mapView) {
			@Override
			protected Collection<? extends BaseDTObject> getObjects() {
				try {
					// TODO
					return DTHelper.getPOIByCategory(0, -1, categories);
				} catch (Exception e) {
					e.printStackTrace();
					return Collections.emptyList();
				}
			}
		}).execute();
	}

	@Override
	public void onBaseDTObjectTap(BaseDTObject o) {
		new InfoDialog(o).show(getSherlockActivity().getSupportFragmentManager(), "me");
	}

}
