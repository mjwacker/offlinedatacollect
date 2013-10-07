/* Copyright (c) 2013 The Montana Department of Transportation
 * 
 * This file has some code based on samples from Esri and subject to the
 * following copyright:
 * 
 * Copyright 2012 ESRI
 *
 * All rights reserved under the copyright laws of the United States
 * and applicable international laws, treaties, and conventions.
 *
 * You may freely redistribute and use this sample code, with or
 * without modification, provided you include the original copyright
 * notice and use restrictions.
 *
 * See the “Sample code usage restrictions” document for further information.
 *
 */

package gov.mt.mdt.data;

import gov.mt.mdt.data.FeatureLayerUtils.FieldType;

import java.io.File;
import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.android.map.Callout;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.LocationService;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISFeatureLayer;
import com.esri.android.map.ags.ArcGISLocalTiledLayer;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.LinearUnit;
import com.esri.core.geometry.MultiPath;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.geometry.Unit;
import com.esri.core.io.UserCredentials;
import com.esri.core.map.FeatureSet;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.symbol.Symbol;


public class OfflineMapActivity extends Activity {
	
	//Project paths to be set by preferences			
	String projectPath;
	String fullProjectPath; 

	// for handling messages from processes
	static Handler mHandler;
	
	private ResponseReceiverWriteOut receiverWriteOut;
	private ResponseReceiverUpdate receiverUpdate;

	// AutoFollow, the gps listener will move map to position if true.
	boolean autoFollow = false;
	
	
	
	// logging tag
	public static final String TAG = "MDTOfflineCollect";

	// for attribute list view
	AttributeListAdapter listAdapter;
	AttributeListAdapter listAdapter2;
	static final int ATTRIBUTE_EDITOR_DIALOG_ID = 1;
	ListView listView;
	View listLayout;
	LayoutInflater inflator;

	//Holds the currently selected site....
	String selectedSite;

	// How often the gps updates the position
	// TODO: make this a setting...
	final static double SEARCH_RADIUS = 10;

	// Spatial References
	//TODO: Probably need to read map cache & service to get srid
	//Will set this based on the local tile package
	SpatialReference localSRS;
	
	// The Map
	MapView map;

	// User Credentials
	UserCredentials creds = new UserCredentials();

	// Just for a text display of current location
	String display = "Coordinates: ";
	
	//logging..
	String logString = new String();

	// setup for the callouts on clicks
	Callout callout;

	// Highlight Graphics
	GraphicsLayer highlightLayer = new GraphicsLayer();

	// Carry the current lat,long:
	double locy;
	double locx;
	
	 ArcGISFeatureLayer editLayer;
	 ArcGISFeatureLayer picLayer;
     
	 List<DataCollectLayer> collectionLayers = new ArrayList<DataCollectLayer>();
     
	 DataCollectLayer dataCollectLayer;
     DataCollectLayer pictureCollectLayer;
     
     //The preferences and it's change listener
     private SharedPreferences preferences;
     private SharedPreferences.OnSharedPreferenceChangeListener prefListener;
     
     
  // isOnline, will ping service periodically to see if online
 	boolean isOnline = false;
     /*
 	 * Implement a handler to catch messages from is online.
 	 */
 	Handler netHandler = new Handler(){
 		@Override
 		  public void handleMessage(Message msg) {
 			  //isOnline = true;
 		     }
 		 };
	
     /*
	 * Called when the activity is first created.
	 */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_map);
	
		//get the preferences and register an onChange listener
		//TODO: need to implement some methods to handle changing preferences
		//that method should be very similar or the same to a firstRun method...
		preferences = PreferenceManager.getDefaultSharedPreferences(this);	
		prefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
			
			@Override
			public void onSharedPreferenceChanged(SharedPreferences preferences,
					String key) {
				// TODO Auto-generated method stub
				System.out.println("you changed preference: "+ key);		
			}
		};
		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(prefListener);
		
		//get the credentials from preferences
		creds.setUserAccount(preferences.getString("username", "dupe"), preferences.getString("password", "foo"));
		
		//set the map view
		map = (MapView) findViewById(R.id.map);
		
		// Setup broadcast receivers for services
		registerBroadcastReceiverWriteOut();
		registerBroadcastReceiverUpdate();
		
		//Setup the project directories from preferences.
        projectPath = preferences.getString("storagepath", "/mdt/OfflineData/");
        fullProjectPath = Environment.getExternalStorageDirectory().toString()  + File.separator + projectPath;

        // Setup Exception Handler
		Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(
				fullProjectPath));

		// Action bar setup
		ActionBar actionBar = getActionBar();
		actionBar.show();

		// Add the local tiles.
		ArcGISLocalTiledLayer local = new ArcGISLocalTiledLayer(preferences.getString("offlinemapcache", "DEFAULT"));
		map.addLayer(local);
		//local.getDefaultSpatialReference().getID()
		localSRS = local.getSpatialReference();
		//System.out.println("Spatial Reference: " + local.getDefaultSpatialReference().getID());

		// TODO: Should probably setup a first run to make sure the config is correct, or just run it in the DataCollectLayer class
		// Path, TPK, user/password
		// Check for initial project setup, i.e. create the directories...
		// problem is the local tile layer is stored in here so first run will
		// produce a blank map
		File file = new File(Environment.getExternalStorageDirectory()
				+ projectPath);
		if (!file.exists()) {
			file.mkdirs();
		}
						
		// add the two Feature Layers
		// These should be parsed from the service somehow.
		String hsip = "HSIP_SITES";
		String hsip_pics = "HSIP_PICS";
		
		dataCollectLayer = new DataCollectLayer(preferences.getString("featurelayerurl", ""), hsip, preferences, getApplicationContext(), map, creds);
		editLayer = dataCollectLayer.getOfflineFeatureLayer();
		collectionLayers.add(dataCollectLayer);
		
		pictureCollectLayer = new DataCollectLayer(preferences.getString("medialayerurl", ""), hsip_pics, preferences, getApplicationContext(), map, creds);
		picLayer = pictureCollectLayer.getOfflineFeatureLayer();
		collectionLayers.add(pictureCollectLayer);
		
		//Setup the listView for the AttributeListAdapter
		inflator = LayoutInflater.from(getApplicationContext());
		listLayout = inflator.inflate(R.layout.list_layout, null);
		listView = (ListView) listLayout.findViewById(R.id.list_view);
		
		//figure out how to handle many of these.
		listAdapter = new AttributeListAdapter(this, dataCollectLayer.getOfflineFeatureLayer().getFields(), dataCollectLayer.getOfflineFeatureLayer().getTypes(), dataCollectLayer.getOfflineFeatureLayer().getTypeIdField());
		listAdapter2 = new AttributeListAdapter(this, pictureCollectLayer.getOfflineFeatureLayer().getFields(), pictureCollectLayer.getOfflineFeatureLayer().getTypes(), pictureCollectLayer.getOfflineFeatureLayer().getTypeIdField());
		/*
		 * End Map/App init.
		 */

		/*
		 * Status listener for update of the GPS, boilerplate from Esri sample.
		 */
		map.setOnStatusChangedListener(new OnStatusChangedListener() {
			private static final long serialVersionUID = 1L;

			public void onStatusChanged(Object source, STATUS status) {
				if (source == map && status == STATUS.INITIALIZED) {
					LocationService ls = map.getLocationService();
					ls.setAutoPan(false);
					ls.setLocationListener(new LocationListener() {

						boolean locationChanged = false;

						// Zooms to the current location with first GPS fix
						public void onLocationChanged(Location loc) {
							if (!locationChanged) {
								locationChanged = true;
								locy = loc.getLatitude();
								locx = loc.getLongitude();

								Point wgspoint = new Point(locx, locy);
								Point mapPoint = (Point) GeometryEngine
										.project(wgspoint,
												SpatialReference.create(4326),
												map.getSpatialReference());

								Unit mapUnit = map.getSpatialReference()
										.getUnit();
								double zoomWidth = Unit.convertUnits(
										SEARCH_RADIUS,
										Unit.create(LinearUnit.Code.MILE_US),
										mapUnit);
								Envelope zoomExtent = new Envelope(mapPoint,
										zoomWidth, zoomWidth);
								map.setExtent(zoomExtent);

								// Display wgs84 coords on map..
								TextView textView = (TextView) findViewById(R.id.textsat);
								textView.setText("Long:" + locx + "\r\nLat:"
										+ locy);

							} else {
								// locationChanged, update the variable and text
								// this is where an tracking array can be built..
								locy = loc.getLatitude();
								locx = loc.getLongitude();
								SpatialReference wgs84 = SpatialReference.create(4326);
								//IF the autofollow is checked recenter the map.
								if(autoFollow){
									Point centerPt = (Point) GeometryEngine.project(new Point(locx, locy),
										wgs84,
										map.getSpatialReference());
									//Point centerPt = new Point(locx, locy);
									map.zoomTo(centerPt, 3);
								}
								
								TextView textView = (TextView) findViewById(R.id.textsat);
								textView.setText("Long:" + locx + "\r\nLat:"
										+ locy);
							}
						}

						public void onProviderDisabled(String arg0) {
						}

						public void onProviderEnabled(String arg0) {
						}

						public void onStatusChanged(String arg0, int arg1,
								Bundle arg2) {
						}
					});
					ls.start();
				}
			}
		});
		
		/*
		 * the maps on tap listener, needs to be generic to handle whatever
		 * happens to be on the map.
		 */
		
		map.setOnSingleTapListener(new OnSingleTapListener() {
			//GraphicsLayer highlightLayer;

			private static final long serialVersionUID = 2L;

			public void onSingleTap(float x, float y) {
				if (map.isLoaded()) {
					int tolerance = 20;

					//TODO: need a callout class here
					callout = map.getCallout();
					
					if(pictureCollectLayer.getOfflineFeatureLayer().getGraphicIDs(x, y,  tolerance).length > 0){
						int[] selectGraphics = pictureCollectLayer.getOfflineFeatureLayer().getGraphicIDs(x, y, tolerance, 10);
						Graphic[] selected = new Graphic[selectGraphics.length];
						String[] fileNames = new String[selectGraphics.length];

						for(int i = 0; i < selectGraphics.length; i++){
							selected[i] = pictureCollectLayer.getOfflineFeatureLayer().getGraphic(selectGraphics[i]);
							
						}

						highlightLayer.removeAll();
						//wrap with a check for points..
						for(int j = 0; j < selected.length; j++){
							Graphic highlightGraphic = new Graphic(selected[j].getGeometry(), new SimpleMarkerSymbol(
									Color.BLUE, 10, SimpleMarkerSymbol.STYLE.CIRCLE));
							highlightLayer.addGraphic(highlightGraphic);
							fileNames[j] = selected[j].getAttributeValue("PICTURE").toString();
						}
						
						if(fileNames.length > 1){
							//Launch the grid view activity
							Intent gvIntent = new Intent(OfflineMapActivity.this, GridViewActivity.class);
							gvIntent.putExtra(GridViewActivity.DIR, fullProjectPath + File.separator + selected[0].getAttributeValue("SITE_ID"));
							//System.out.println(fullProjectPath + File.separator + selected[0].getAttributeValue("SITE_ID"));
							gvIntent.putExtra(GridViewActivity.ARR, fileNames);
							startActivity(gvIntent);
						}else{
							if(fileNames[0] != null){
								File singleImage = new File(fullProjectPath + File.separator + selected[0].getAttributeValue("SITE_ID") + File.separator + fileNames[0]);
								
								if(singleImage.exists()){
									String[] split = fileNames[0].split("\\.");
									String fileType = split[1].toString();
									
									Intent imageIntent = new Intent();
									imageIntent.setAction(android.content.Intent.ACTION_VIEW);
									Uri uri = Uri.parse("file://" + singleImage);
									
									if(fileType.equals("jpg")){
										imageIntent.setDataAndType(uri, "image/*");
										startActivity(imageIntent);
									}else if(fileType.equals("mp3")){
										imageIntent.setDataAndType(uri, "video/*");
										startActivity(imageIntent);
									}else {
										Toast.makeText(OfflineMapActivity.this, "Image or Video not on device", Toast.LENGTH_LONG).show();
									}
								}
							}
						}
						
					}
					else if (editLayer.getGraphicIDs(x, y, tolerance).length > 0){

						int selectedGraphics[] = editLayer.getGraphicIDs(x,
								y, tolerance);
						int selectedNum = selectedGraphics[0];

						Graphic selected = editLayer.getGraphic(selectedNum);

						// Clear the highlight Layer
						if (highlightLayer != null) {
							highlightLayer.removeAll();
						}
						// if the graphics exist, clear them..
						highlightLayer = new GraphicsLayer();
						Point callPoint = new Point();
						// need to get the highlightLayer from map

						// Check the Type..
						if (selected.getGeometry().getType().toString() == "POINT") {
							Graphic highlightGraphic = new Graphic(
									selected.getGeometry(),
									(Symbol) new SimpleMarkerSymbol(Color.BLUE,
											23, SimpleMarkerSymbol.STYLE.CIRCLE));
							highlightLayer.addGraphic(highlightGraphic);
							callPoint = (Point) selected.getGeometry();
						}

						if (selected.getGeometry().getType().toString() == "POLYLINE") {
							Graphic highlightGraphic = new Graphic(selected
									.getGeometry(),
									(Symbol) new SimpleLineSymbol(Color.BLUE,
											4, SimpleLineSymbol.STYLE.SOLID));
							highlightLayer.addGraphic(highlightGraphic);

							// get an anchor for the callout
							MultiPath mpath = (MultiPath) highlightGraphic
									.getGeometry();
							callPoint = mpath.getPoint(mpath.getPointCount() / 2);
						}

						map.addLayer(highlightLayer);

						// TODO: Add graphic for polygons

						// use the graphic to construct a feature set for
						// creating and attribute editing dialog.
						// This really returns an array, but we just grab the
						// first one.
						Graphic[] update = new Graphic[1];
						update[0] = selected;
						FeatureSet attributesToEdit = new FeatureSet();
						attributesToEdit.setGraphics(update);

						// TODO: ListAdapter
						listAdapter.setFeatureSet(attributesToEdit);
						listAdapter.notifyDataSetChanged();

						// TODO: callout
						callout.setStyle(R.xml.callout);
						callout.getStyle().setMaxHeight(1000);
						callout.getStyle().setMaxWidth(1000);
						
						/**
						Map<String,Object> calloutValues = selected.getAttributes();
						callout.setContent(loadCallout(calloutValues));
						**/
						
						String siteID = (String) selected
								.getAttributeValue("SITE_ID");

						// populate selected site, used for launching
						// gridViewActivity
						selectedSite = siteID;

						String corridor = (String) selected
								.getAttributeValue("CORRIDOR");
						String beg = (String) selected.getAttributeValue("BEG");
						String end = (String) selected.getAttributeValue("END");
						String district = (String) selected
								.getAttributeValue("DISTRICT");
						String county = (String) selected
								.getAttributeValue("COUNTY");
						String type = (String) selected
								.getAttributeValue("TYPE");
						callout.setContent(loadView(corridor, beg, end, district, county, type));
						
						
						
						callout.show(callPoint);
					}else{
						if(callout != null && callout.isShowing()){
							callout.hide();
						}
						highlightLayer.removeAll();
					}
				}
			}

		});
	}

	private void registerBroadcastReceiverWriteOut() {
		//register broadcast receiver:
		IntentFilter filter = new IntentFilter(ResponseReceiverWriteOut.ACTION_RESP);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiverWriteOut = new ResponseReceiverWriteOut();
        registerReceiver(receiverWriteOut, filter);
	}
	
	
	private void registerBroadcastReceiverUpdate(){
		IntentFilter filter = new IntentFilter(ResponseReceiverUpdate.ACTION_RESP);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiverUpdate = new ResponseReceiverUpdate();
        registerReceiver(receiverUpdate, filter);
	}

	/*
	 * Create the options menu. Should always be an icon, on phones it goes to
	 * the menu button.(non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// use an inflater to populate the ActionBar with items
		MenuInflater inflater = getMenuInflater();
		//inflater.inflate(R.layout.menu, menu);
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	/*
	 * Options menu selection handling (non-Javadoc)
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		
		case R.id.item_autoFollow:
			if(item.isChecked()){
				autoFollow = false;
			}else{
				autoFollow = true;
			}
			item.setChecked(!item.isChecked());
			break;
		
		case R.id.item_gpsStatus:
			if (isPackageExists("com.eclipsim.gpsstatus2")) {
				Intent LaunchGpsStatus = getPackageManager()
						.getLaunchIntentForPackage("com.eclipsim.gpsstatus2");
				startActivity(LaunchGpsStatus);
			} else {
				Toast.makeText(OfflineMapActivity.this,
						R.string.message_nogpsstatus, Toast.LENGTH_LONG).show();
				try {
					startActivity(new Intent(Intent.ACTION_VIEW)
							.setData(Uri
									.parse("market://search?q=pname:com.eclipsim.gpsstatus2")));
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
			//return true;
			break;

		case R.id.item_update:
			// check whether or not we are online...
			//online = isOnline();
			//Boolean online = OfflineCollectUtils.onNetwork(getApplicationContext());
			Boolean online = OfflineCollectUtils.isInternetAvailable(getApplicationContext());
			// if we are online ask the user to query the feature layer
			if (online) {
				// ask the user whether they want to, check the date of the
				// file./
				AlertDialog.Builder alert = new AlertDialog.Builder(this);
				alert.setTitle("Get New Data?");
				alert.setMessage("You are online, do you want to update data from server?");
				alert.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// Get the data
								for(int i = 0; i < collectionLayers.size(); i++){
									collectionLayers.get(i).updateLayer();
									try {
										Thread.sleep(5000);
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
								
							}
						});

				alert.setNegativeButton("No",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// do nothing
							}
						});

				alert.show();
				//return true;
				break;
			} else {
				alertOffline();
				//return false;
				break;
			}
		case R.id.item_edit:
			//finalizeEdits();
			for(int i = 0; i < collectionLayers.size(); i++){
				System.out.println("item edit " + collectionLayers.get(i).featureLayerName);
				collectionLayers.get(i).finalizeEdits();
			}
			//return true;
			break;
		case R.id.item_help:
			// launch the help page.
			startActivity(new Intent("gov.mt.mdt.data.WebViewActivity"));
			//return true;
			break;
		case R.id.item_preferences:
			Intent i = new Intent(OfflineMapActivity.this, PreferencesActivity.class);
			startActivity(i);
			break;
			//return true;
		
		/**
		case R.id.item_upload:
			//Launch the webViewUploader
			startActivity(new Intent("gov.mt.mdt.data.WebViewUploader"));
			return true; **/
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

	/*
	 * Checks for the package availability. Used for satellite status, but could
	 * check for any package, this needs a context to getPackageManagers, so if passing as var it could be moved to utils.
	 */
	public boolean isPackageExists(String targetPackage) {
		//need the context to getPackageManager()..
		PackageManager pm = getPackageManager();
		try {
			PackageInfo info = pm.getPackageInfo(targetPackage,
					PackageManager.GET_META_DATA);
		} catch (NameNotFoundException e) {
			return false;
		}
		return true;
	}

	/*
	 * Creates custom content view with 'Graphic' attributes So far only can do
	 * one callout this is problematic with multiple layers
	 */
	private View loadView(String corridor, String beg, String end,
			String district, String county, String type) {
		View view = LayoutInflater.from(OfflineMapActivity.this).inflate(
				R.layout.callout_sites, null);

		final TextView corridorText = (TextView) view
				.findViewById(R.id.corridor);
		
		corridorText.setText("Corridor: " + corridor);

		final TextView begText = (TextView) view.findViewById(R.id.beg);
		begText.setText("Begin: " + beg);

		final TextView endText = (TextView) view.findViewById(R.id.end);
		endText.setText("End: " + end);

		final TextView districtText = (TextView) view
				.findViewById(R.id.district);
		districtText.setText("District: " + district);

		final TextView countyText = (TextView) view.findViewById(R.id.county);
		countyText.setText("County: " + county);

		final TextView typeText = (TextView) view.findViewById(R.id.type);
		typeText.setText("Type: " + type);

		return view;

	}
	
	
	private View loadCallout(Map<String, Object> attribs){
		View view = LayoutInflater.from(OfflineMapActivity.this).inflate(
				R.layout.callout, null);
		
		//this is not quite right...
		Object[] keys = attribs.keySet().toArray();
		
		System.out.println(keys[0].toString());
		Object[] vals = attribs.values().toArray();
		
		final TextView firstText = (TextView) view.findViewById(R.id.first_field);
		firstText.setText(keys[0].toString() + ": " + vals[0].toString());
		final TextView secondText = (TextView) view.findViewById(R.id.second_field);
		secondText.setText(keys[1].toString() + ": " + vals[1].toString());
		final TextView thridText = (TextView) view.findViewById(R.id.third_field);
		thridText.setText(keys[2].toString() + ": " + vals[2].toString());
		final TextView fourthText = (TextView) view.findViewById(R.id.fourth_field);
		fourthText.setText(keys[3].toString() + ": " + vals[3].toString());
		final TextView fifthText = (TextView) view.findViewById(R.id.fifth_field);
		fifthText.setText(keys[4].toString() + ": " + vals[4].toString());
		
		return view;
	}
	
	/*
	 * Show the attribute editing window when the button is Edit button is
	 * pressed
	 */
	public void onClickCalloutButton(View v) {
		OfflineMapActivity.this.runOnUiThread(new Runnable() {
			public void run() {
				showDialog(ATTRIBUTE_EDITOR_DIALOG_ID);		
			}
		});
	}

	/*
	 * Process for taking video, would eventually like to track at the same
	 * time, for now point before and after linking to video
	 */
	public void onClickTakeVideo(View v) {
		setupMediaLocal(".mp4");
	}

	/*
	 * Process for taking pictures, not sure if point is taken before or after
	 * intent completes Need to look into returns from intents.
	 */
	public void onClickTakePicture(View v) {
		setupMediaLocal(".jpg");
	}
	
	/*
	 * Initiates the grid view activity that displays the pictures and videos for the
	 * site which is clicked upon.
	 */
	public void onClickViewFolder(View v) {
		//Check to see if there are any media.
		File dir = new File(fullProjectPath + File.separator + selectedSite);
		String[] listFiles = dir.list();
		if(listFiles != null){
			Intent gvIntent = new Intent(OfflineMapActivity.this, GridViewActivity.class); //"gov.mt.mdt.data.GridViewActivity");
			gvIntent.putExtra(GridViewActivity.DIR, fullProjectPath + File.separator + selectedSite);
			startActivity(gvIntent);
		}else{
			Toast.makeText(this, "There is no media for this site.", Toast.LENGTH_SHORT).show();
		}
	}
	
	/*
	 * This is an attempt to modularize the pics and video routine.
	 */
	public void setupMediaLocal(String fileExt) {
		Date date = new Date();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
		// Should have a variable for the path...
		String imagePath = Environment.getExternalStorageDirectory() + File.separator
				+ projectPath + File.separator + selectedSite;
		File file = new File(imagePath);
		if (!file.exists()) {
			file.mkdirs();
		}
		String fileName = df.format(date) + fileExt;
		File imageFile = new File(imagePath + "/" + fileName);
		Uri outputFileUri = Uri.fromFile(imageFile);

		// figure out intent by where the fileExt (.jpg or .mp4)
		if (fileExt == ".jpg") {
			Intent i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
			i.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, outputFileUri);
			startActivityForResult(i, 1);
		} else {
			Intent i = new Intent(android.provider.MediaStore.ACTION_VIDEO_CAPTURE);
			i.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, outputFileUri);
			startActivityForResult(i, 2);
		}

		/*
		 * This needs to wait for the task to complete with an image/video
		 * Then process the point
		 * Problem is that now it takes the point when this method is initiated
		 * If we wait it will take the point with the save button is pushed.
		 * 
		 */
		
		// the startActivitiy appear to be async so it grabs your location when
		// the button is pushed.
		Point wgsPoint = new Point(locx, locy);

		//get srs from local tiles, class variable
		SpatialReference wgs = SpatialReference.create(4326);
		Point mtPoint = (Point) GeometryEngine.project(wgsPoint, wgs, localSRS);
		
		// Add the attributes to the graphic
		// Device path not transferring, must be too long
		HashMap<String, Object> hashMap = new HashMap<String, Object>();
		hashMap.put("SITE_ID", selectedSite);
		hashMap.put("PICTURE", fileName);
		hashMap.put("DEVICE_PATH", imagePath);
		//logString += "Setup Media Local: " + imagePath + "/" + fileName + ". ";
		
		Graphic mapGraphic = new Graphic(mtPoint, new SimpleMarkerSymbol(
				Color.RED, 12, SimpleMarkerSymbol.STYLE.CIRCLE), hashMap, null);
		

		picLayer.addGraphic(mapGraphic);
		pictureCollectLayer.updateEditFeatureSet(mapGraphic, null, null);
		
		//This one is not quite right...
		//pictureCollectLayer.syncFeatureSets(mapGraphic);
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 * Should handle video and pictures this way.
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		//handle picture
		if(requestCode == 1){
			System.out.println("we took a picture and saved.");
		}
		//handle video
	}

    /*
     * Produces a dialog that alerts the user that they are offline
     */
	public void alertOffline(){
		AlertDialog.Builder offline = new AlertDialog.Builder(this);
		offline.setTitle("Offline");
		offline.setMessage("You are currently offline, please get online to update data and/or send edits.");
		offline.setPositiveButton("OK",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		offline.show();
	}
	

	/**
	 * Overidden method from Activity class - this is the recommended way of
	 * creating dialogs
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		//Added to stop tapping outside the dialog from dismissing.
		this.setFinishOnTouchOutside(false);
		switch (id) {
		
		case ATTRIBUTE_EDITOR_DIALOG_ID:
			this.setFinishOnTouchOutside(false);
			
			// create the attributes dialog
			Dialog dialog = new Dialog(this);
			dialog.setCanceledOnTouchOutside(false);
			listView.setAdapter(listAdapter);
			dialog.setContentView(listLayout);
			dialog.setTitle("Edit Attributes");

			// set button on click listeners, setting as xml attributes doesnt
			// work due to a scope/thread issue
			Button btnEditCancel = (Button) listLayout
					.findViewById(R.id.btn_edit_discard);
			btnEditCancel
					.setOnClickListener(returnOnClickDiscardChangesListener());

			Button btnEditApply = (Button) listLayout
					.findViewById(R.id.btn_edit_apply);
			btnEditApply
					.setOnClickListener(returnOnClickApplyChangesListener());

			return dialog;
		}
		return null;
	}

	/**
	 * OnClick method for the Discard button
	 */
	public OnClickListener returnOnClickDiscardChangesListener() {
		return new OnClickListener() {

			public void onClick(View v) {
				// close the dialog
				dismissDialog(ATTRIBUTE_EDITOR_DIALOG_ID);
			}
		};

	}

	/**
	 * Helper method to return an OnClickListener for the Apply button
	 */
	public OnClickListener returnOnClickApplyChangesListener() {

		return new OnClickListener() {

			public void onClick(View v) {
				
				boolean isTypeField = false;
				boolean hasEdits = false;
				boolean updateMapLayer = false;
				Map<String, Object> attrs = new HashMap<String, Object>();

				// loop through each attribute and set the new values of
				// changes.
				for (int i = 0; i < listAdapter.getCount(); i++) {

					AttributeItem item = (AttributeItem) listAdapter.getItem(i);
					String value = "";

					// check to see if the View has been set
					if (item.getView() != null) {

						// TODO implement applying domain fields values if
						// required
						// determine field type and therefore View type
						if (item.getField()
								.getName()
								.equals(dataCollectLayer.getOfflineFeatureLayer().getTypeIdField())){
										

							Spinner spinner = (Spinner) item.getView();
							// get value for the type
							String typeName = spinner.getSelectedItem()
									.toString();
							value = FeatureLayerUtils.returnTypeIdFromTypeName(
									dataCollectLayer.getOfflineFeatureLayer().getTypes(), 
									typeName);

							// update map layer as for this featurelayer the
							// type change will
							// change the features symbol.
							isTypeField = true;

						} else if (FieldType
								.determineFieldType(item.getField()) == FieldType.DATE) {
							// date
							Button dateButton = (Button) item.getView();
							value = dateButton.getText().toString();

						} else {
							// edit text
							EditText editText = (EditText) item.getView();
							value = editText.getText().toString();

						}

						// try to set the attribute value on the graphic and see
						// if it has
						// been changed
						boolean hasChanged = FeatureLayerUtils.setAttribute(
								attrs, listAdapter.featureSet.getGraphics()[0],
								item.getField(), value, listAdapter.formatter);

						// if a value has for this field, log this and set the
						// hasEdits
						// boolean to true
						if (hasChanged) {

							Log.d(TAG, "Change found for field="
									+ item.getField().getName() + " value = "
									+ value + " applyEdits() will be called");
							hasEdits = true;

							// If the change was from a Type field then set the
							// dynamic map
							// service to update when the edits have been
							// applied, as the
							// renderer of the feature will likely change
							if (isTypeField) {

								updateMapLayer = true;

							}
						}

						// check if this was a type field, if so set boolean
						// back to false
						// for next field
						if (isTypeField) {

							isTypeField = false;
						}
					}
				}

				// check there have been some edits before applying the changes
				if (hasEdits) {

					// set objectID field value from graphic held in the
					// featureset
					attrs.put(dataCollectLayer.getOfflineFeatureLayer().getObjectIdField(),
							listAdapter.featureSet.getGraphics()[0]
									.getAttributeValue(dataCollectLayer.getOfflineFeatureLayer()
											.getObjectIdField()));

					// got to grab the graphic from the hightlight layer, not
					// efficient.
					// This is likely to combat a bug and we are going to end up
					// store all the geoms, when unecessary,...
					int[] index = highlightLayer.getGraphicIDs();
					//logString += "Attribute Update: highlightLayer gets id. ";
					Graphic graphicInd = highlightLayer.getGraphic(index[0]);
					Geometry updater = graphicInd.getGeometry();
					Graphic newGraphic = new Graphic(updater, null, attrs, null);

					// push this into a fs and save as json...
					dataCollectLayer.updateEditFeatureSet(null, null, newGraphic);
					
					
					dataCollectLayer.syncFeatureSets(newGraphic);
					// Sync the changes to the offline feature set.
					//for(int i = 0; i < collectionLayers.size(); i++){
						//collectionLayers.get(i).syncFeatureSets(newGraphic);
					//}
					//syncFeatureSets(newGraphic, dataCollectFeatureSet);

					callout.hide();
				}

				// close the dialog
				dismissDialog(ATTRIBUTE_EDITOR_DIALOG_ID);
			}
		};
	}
		
	/*
	 * Broadcast Receiver for WriteOutputService
	 */
	public class ResponseReceiverWriteOut extends BroadcastReceiver {
		public static final String ACTION_RESP = "gov.mt.mdt.data.WriteOutputService";
		
		@Override
		public void onReceive(Context context, Intent intent){
			String text = intent.getStringExtra(WriteOutputService.PARAM_OUT_MSG);
			//String fs = intent.getStringExtra(WriteOutputService.OUT_FEATURE);
			Toast.makeText(OfflineMapActivity.this,	text, Toast.LENGTH_LONG).show();
			
			}
	}
	
	/*
	 * Broadcast Receiver for GetUpdateService
	 */

	public class ResponseReceiverUpdate extends BroadcastReceiver {
		public static final String ACTION_RESP = "gov.mt.mdt.data.GetUpdateService";
		
		@Override
		public void onReceive(Context context, Intent intent){
			String text = intent.getStringExtra(GetUpdateService.PARAM_OUT_MSG);
			Toast.makeText(OfflineMapActivity.this, text, Toast.LENGTH_LONG).show();
		}
	}
	
	
	
	
	/*
	 * On check box change, set's the auto follow true or false, the location listenser 
	 * (OnStatusChanged listener has logic to pan map to local..
	 * moved into the options menu.
	public void onAutoFollowClicked(View view){
		boolean checked = ((CheckBox) view).isChecked();
		if (checked){
			autoFollow = true;
		}else{
			autoFollow = false;
		}
	}*/

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		super.onPause();
		map.pause();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		map.unpause();
	}

	/*
	 * Dismiss dialog if activity is destroyed(non-Javadoc)
	 * 
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		//unregister any response receivers
		unregisterReceiver(receiverWriteOut);
		unregisterReceiver(receiverUpdate);
		//System.out.println("On Destroy: " + logString);
		// Write out the log file
		OfflineCollectUtils.appendLog(logString, projectPath);
		
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		return map.retainState();
	}
	
}
	
	
