/* Copyright (c) 2013 The Montana Department of Transportation
 * 
 */
package gov.mt.mdt.data;

import gov.mt.mdt.data.OfflineMapActivity.ResponseReceiverUpdate;
import android.app.IntentService;
import android.content.Intent;

import com.esri.android.map.ags.ArcGISFeatureLayer;
import com.esri.android.map.ags.ArcGISFeatureLayer.MODE;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.CallbackListener;
import com.esri.core.map.FeatureSet;
import com.esri.core.tasks.SpatialRelationship;
import com.esri.core.tasks.ags.query.Query;

public class GetUpdateService extends IntentService {
	
	private static final String TAG = "GetUpdateService";
	public static final String FEATURELAYER = "featurelayer";
	public static final String FILENAME = "file_name";
	public static final String URL = "service_url";
	public static final String RESULTRECEIVER = "ResultReceiver";
	public static final String EXTENT = "extent";
	public static final String SPREF = "spRef";
	
	
	public static final String PARAM_OUT_MSG = "complete";
	public static String OUT_FEATURE;

	public GetUpdateService() {
		super(TAG);
	}
	
	//need to send these into this service.
	String resultTxt;
	//Envelope mtExtent = new Envelope(-12927749.00, 5506050.00, -11552188.00, 6300264.00);
	Envelope mtStExtent = new Envelope(82334.00, 16457.00, 1059721.00, 561538);
	//SpatialReference wgs84 = SpatialReference.create(4326);
	SpatialReference mtSt = SpatialReference.create(32100);
	//SpatialReference webMerc = SpatialReference.create(102100);
	
	FeatureSet resultFS;

	@Override
	protected void onHandleIntent(Intent intent) {
		//ResultReceiver receiver = intent.getParcelableExtra(RESULTRECEIVER);
		String fileName = intent.getStringExtra(FILENAME);
		System.out.println("FileName is: " + fileName);
		String url = intent.getStringExtra(URL);
		int srsCode = intent.getIntExtra(SPREF, 102100);
		//String srsCode = intent.getStringExtra(SPREF);
		Envelope extent = (Envelope) intent.getSerializableExtra(EXTENT);
		SpatialReference srs = SpatialReference.create(srsCode); 
		
		ArcGISFeatureLayer featureLayer = new ArcGISFeatureLayer(url, MODE.SNAPSHOT);
		//ArcGISFeatureLayer featureLayer = (ArcGISFeatureLayer) intent.getSerializableExtra(FEATURELAYER);
		//featureSet.setSpatialReference(SpatialReference.create(32100));
		
		System.out.println("Get Update Service: about to query");
		
		//FileOutputStream outstream = null;
		try {
			
			
			final String fn = fileName;
			Query query = new Query();
			
			//query.setGeometry(mtStExtent);
			query.setGeometry(extent);
			query.setSpatialRelationship(SpatialRelationship.CONTAINS);
			query.setInSpatialReference(srs);
			query.setOutSpatialReference(srs);
			//query.setInSpatialReference(mtSt);
			//query.setOutSpatialReference(mtSt);
			
			query.setOutFields(new String[] { "*" });
			query.setReturnGeometry(true);
			// run query on FeatureLayer off UI thread
			System.out.println("About to Query");
			featureLayer.queryFeatures(query, new CallbackListener<FeatureSet>() {
			
				// an error occurred, log exception
				@Override
				public void onError(Throwable e) {
					//alert the user somehow.
					System.out.println("There was an error: " + e);
				}

				// create json from resulting FeatureSet
				@Override
				public void onCallback(FeatureSet result) {
					System.out.println(result.toString());
					if (result != null) {
						// Write out the update file through the intent service.
						System.out.println("Query Successful");
					
						resultFS = result;
						//Need a context for this...
						Intent intent = new Intent(getBaseContext(), WriteOutputService.class);
						intent.putExtra(WriteOutputService.FEATURESET, result);
						//intent.putExtra(WriteOutputService.FILENAME, fileName);
						intent.putExtra(WriteOutputService.FILENAME, fn);
						startService(intent);
						//startService(intent);
						resultTxt = "query complete: next..";
						// Updates the featureLayer based on query results
						//updateFeatureLayer(result, fileName);
					}
				}
			});
			
			Intent broadcastIntent = new Intent();
			broadcastIntent.setAction(ResponseReceiverUpdate.ACTION_RESP);
			broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
			broadcastIntent.putExtra(PARAM_OUT_MSG, resultTxt);
			//broadcastIntent.putExtra(OUT_FEATURE, feature);
			
			//setResult(resultFS);
			sendBroadcast(broadcastIntent);
			
		} catch (Exception e) {
			e.printStackTrace();
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