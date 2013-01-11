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

import android.accounts.Account;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.os.Bundle;
import android.preference.Preference;
import android.util.Log;
import eu.trentorise.smartcampus.android.common.GlobalConfig;
import eu.trentorise.smartcampus.dt.custom.data.Constants;
import eu.trentorise.smartcampus.dt.custom.data.DTHelper;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.ProtocolException;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;
import eu.trentorise.smartcampus.storage.DataException;
import eu.trentorise.smartcampus.storage.sync.SyncStorage;

/**
 * SyncAdapter implementation for syncing sample SyncAdapter contacts to the
 * platform ContactOperations provider.
 */
public class DTSyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String TAG = "DTSyncAdapter";

    private final Context mContext;


    public DTSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContext = context;
		DTHelper.init(mContext);
        ContentResolver.setSyncAutomatically(new Account(eu.trentorise.smartcampus.ac.Constants.ACCOUNT_NAME, eu.trentorise.smartcampus.ac.Constants.ACCOUNT_TYPE), "eu.trentorise.smartcampus.dt", true);
        ContentResolver.addPeriodicSync(new Account(eu.trentorise.smartcampus.ac.Constants.ACCOUNT_NAME, eu.trentorise.smartcampus.ac.Constants.ACCOUNT_TYPE), "eu.trentorise.smartcampus.dt", new Bundle(),Constants.SYNC_INTERVAL*60);

    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
        ContentProviderClient provider, SyncResult syncResult) {
    	 try {
 			Log.e(TAG, "Trying synchronization");
			SyncStorage storage = DTHelper.getSyncStorage();
			storage.synchronize(DTHelper.getAuthToken(), GlobalConfig.getAppUrl(mContext), eu.trentorise.smartcampus.dt.custom.data.Constants.SYNC_SERVICE);

		}  catch (SecurityException e) {
			handleSecurityProblem();
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "on PerformSynch Exception: "+ e.getMessage());
		}
    }
    
	public void handleSecurityProblem() {
        Intent i = new Intent("eu.trentorise.smartcampus.START");
        i.setPackage(mContext.getPackageName());

        DTHelper.getAccessProvider().invalidateToken(mContext, null);
        
        NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        
        int icon = eu.trentorise.smartcampus.ac.R.drawable.stat_notify_error;
        CharSequence tickerText = mContext.getString(eu.trentorise.smartcampus.ac.R.string.token_expired);
        long when = System.currentTimeMillis();
        CharSequence contentText = mContext.getString(eu.trentorise.smartcampus.ac.R.string.token_required);
        PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, i, 0);

        Notification notification = new Notification(icon, tickerText, when);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.setLatestEventInfo(mContext, tickerText, contentText, contentIntent);
        
        mNotificationManager.notify(eu.trentorise.smartcampus.ac.Constants.ACCOUNT_NOTIFICATION_ID, notification);
	}
    
}
