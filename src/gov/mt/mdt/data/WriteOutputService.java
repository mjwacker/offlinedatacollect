/* Copyright (c) 2013 The Montana Department of Transportation
 * 
 */
package gov.mt.mdt.data;

import gov.mt.mdt.data.OfflineMapActivity.ResponseReceiverWriteOut;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.IntentService;
import android.content.Intent;

import com.esri.core.map.FeatureSet;

public class WriteOutputService extends IntentService {
	
	private static final String TAG = "WriteOutputService";
	public static final String FEATURESET = "featureset";
	public static final String FILENAME = "file_name";
	public static final String RESULTRECEIVER = "ResultReceiver";
	
	public static final String PARAM_OUT_MSG = "complete";
	public static String OUT_FEATURE;

	public WriteOutputService() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		//ResultReceiver receiver = intent.getParcelableExtra(RESULTRECEIVER);
		String fileName = intent.getStringExtra(FILENAME);
		System.out.println("FileName is: " + fileName);
		FeatureSet featureSet = (FeatureSet) intent.getSerializableExtra(FEATURESET);
		//featureSet.setSpatialReference(SpatialReference.create(32100));
		
		System.out.println("Write Output Service: about to write out, declared fs..");
		
		FileOutputStream outstream = null;
		try {
			
			String jsonString = FeatureSet.toJson(featureSet);
			System.out.println(jsonString);
			String path = OfflineCollectUtils.getJSONFilePath(fileName, this.getResources().getString(R.string.config_data_sdcard_offline_dir));
			
			System.out.println(path);
			File outfile = new File(path);
			outstream = new FileOutputStream(outfile);
			outstream.write(jsonString.getBytes());
			outstream.close();
			String resultTxt = "Write Output Service: Completed: " + fileName;
			System.out.println("Write Output Service: Completed: " + fileName);
			//String feature = fileName;
			
			Intent broadcastIntent = new Intent();
			broadcastIntent.setAction(ResponseReceiverWriteOut.ACTION_RESP);
			broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
			broadcastIntent.putExtra(PARAM_OUT_MSG, resultTxt);
			//broadcastIntent.putExtra(OUT_FEATURE, feature);
			sendBroadcast(broadcastIntent);
			
		} catch (Exception e) {
			e.printStackTrace();
			if (outstream != null) {
				try {
					outstream.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}

	}
	
	@Override
	public void onLowMemory() {
		// TODO Auto-generated method stub
		super.onLowMemory();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
}