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

package eu.trentorise.smartcampus.dt.syncadapter;

import java.util.List;

import android.accounts.Account;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;
import eu.trentorise.smartcampus.communicator.model.DBNotification;
import eu.trentorise.smartcampus.dt.DiscoverTrentoActivity;
import eu.trentorise.smartcampus.dt.R;
import eu.trentorise.smartcampus.notifications.NotificationsHelper;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;
import eu.trentorise.smartcampus.storage.sync.SyncData;

/**
 * SyncAdapter implementation for syncing sample SyncAdapter contacts to the
 * platform ContactOperations provider.
 */
public class NotificationsSyncAdapter extends AbstractThreadedSyncAdapter {
	private static final String TAG = "NotificationsSyncAdapter";

	private final Context mContext;

	// NotificationHelper consts
	private final static String appToken = "discovertrento";

	private final static String APP_ID = "core.territory";

	private final static int MAX_MSG = 50;

	public NotificationsSyncAdapter(Context context, boolean autoInitialize) {
		super(context, autoInitialize);
		mContext = context;

		init(context);
	}

	private void init(Context context) {
		if (!NotificationsHelper.isInstantiated()) {
			String authority = context
					.getString(R.string.notificationprovider_authority);
			try {
				NotificationsHelper.init(context, appToken, authority, APP_ID, MAX_MSG);
				NotificationsHelper.start(true);
			} catch (Exception e) {
				Log.e(TAG,
						"Failed to instantiate SyncAdapter: " + e.getMessage());
			}
		}
	}

	@Override
	public void onPerformSync(Account account, Bundle extras, String authority,
			ContentProviderClient provider, SyncResult syncResult) {
		init(getContext());
		try {
			Log.e(TAG, "Trying synchronization");
			// SyncStorage storage = NotificationsHelper.getSyncStorage();
			SyncData data = NotificationsHelper.synchronize();
			if (data.getUpdated() != null
					&& !data.getUpdated().isEmpty()
					&& data.getUpdated().containsKey(
							DBNotification.class.getCanonicalName()))
				onDBUpdate(data.getUpdated().get(
						DBNotification.class.getCanonicalName()));
		} catch (SecurityException e) {
			handleSecurityProblem();
		} catch (Exception e) {
			Log.e(TAG, "on PerformSynch Exception: " + e.getMessage());
		}
	}

	private void handleSecurityProblem() {
		// Intent i = new Intent("eu.trentorise.smartcampus.START");
		// i.setPackage(mContext.getPackageName());
		//
		// NotificationManager mNotificationManager = (NotificationManager)
		// mContext
		// .getSystemService(Context.NOTIFICATION_SERVICE);
		//
		// int icon = R.drawable.launcher;
		// CharSequence tickerText = mContext
		// .getString(eu.trentorise.smartcampus.ac.R.string.token_expired);
		// long when = System.currentTimeMillis();
		// CharSequence contentText = mContext
		// .getString(eu.trentorise.smartcampus.ac.R.string.token_required);
		// PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0,
		// i,
		// 0);
		//
		// android.app.Notification notification = new android.app.Notification(
		// icon, tickerText, when);
		// notification.flags |= android.app.Notification.FLAG_AUTO_CANCEL;
		// notification.setLatestEventInfo(mContext, tickerText, contentText,
		// contentIntent);
		//
		// mNotificationManager.notify(
		// eu.trentorise.smartcampus.ac.Constants.ACCOUNT_NOTIFICATION_ID,
		// notification);
	}

	private void onDBUpdate(List<Object> objsList) {
		if (!objsList.isEmpty()) {
			int icon = 0;
			Intent intent = null;

			icon = R.drawable.dt;
			intent = new Intent(mContext,
					DiscoverTrentoActivity.class);
			if (intent != null) {
				intent.putExtra(DiscoverTrentoActivity.PARAM_NOTIFICATION_ACTIVITY, true);
			}

			NotificationManager mNotificationManager = (NotificationManager) mContext
					.getSystemService(Context.NOTIFICATION_SERVICE);

			CharSequence tickerText = extractTitle(objsList);
			long when = System.currentTimeMillis();
			CharSequence contentText = extractText(objsList);
			PendingIntent contentIntent = PendingIntent.getActivity(
					mContext, 0, intent, 0);

			android.app.Notification notification = new android.app.Notification(
					icon, tickerText, when);
			notification.flags |= android.app.Notification.FLAG_AUTO_CANCEL;
			notification.setLatestEventInfo(mContext, tickerText,
					contentText, contentIntent);

			mNotificationManager.notify(1, notification);
		}

	}

	private CharSequence extractTitle(List<Object> list) {
		String txt = "";

		DBNotification map = (DBNotification) list.get(0);
		String type = map.getNotification().getType();
		txt = mContext.getString(R.string.app_name);

		return txt;
	}

	private CharSequence extractText(List<Object> list) {
		String txt = "";

		if (list.size() == 1) {
			txt = mContext.getString(R.string.notification_text,
					Integer.toString(list.size()));
		} else {
			txt = mContext.getString(R.string.notification_text_multi,
					Integer.toString(list.size()));
		}
		return txt;
	}
}
