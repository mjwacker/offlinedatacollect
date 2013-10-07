package gov.mt.mdt.data;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Video.Thumbnails;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;



public class GridViewActivity extends Activity {
	public static final String DIR = "dir";
	public static final String ARR = "arr";
	String directory;
	
	ArrayList<Integer> item_ids = new ArrayList<Integer>();
	
	String[] files;
	
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.grid);
		
		//File dir = intent(DIR);
		
		//Get the dir passed into the activity
		Bundle extras = getIntent().getExtras();
		//System.out.println(extras.toString());
			
		
		if(extras != null){
			//passing the directory is required
			//the array is optional
			directory = extras.getString(DIR);
			files =	(String[])	extras.getSerializable(ARR);
			//Bitmap[] bmpArray = new Bitmap[files.length];
		}	
		
		File dir = new File(directory);
		if(files == null){
			files = dir.list();
		}
		Bitmap[] bmpArray = new Bitmap[files.length];
		
		
		
		for (int i=0;i< files.length;i++){
			String[] split = files[i].split("\\.");
			//System.out.println(split[1]);
			if(split[1].equals("jpg")){
				bmpArray[i] = decodeFile(dir +  File.separator + files[i], 40);
				//bmpArray[i] = BitmapFactory.decodeFile(dir +  File.separator + files[i]);
				
			}else if(split[1].equals("mp4")){
							
				bmpArray[i] = ThumbnailUtils.createVideoThumbnail(dir + File.separator + files[i], Thumbnails.MICRO_KIND);
				//System.out.println(bmpArray[i].toString());
				
			}
		}
		
		GridView gridView = (GridView) findViewById(R.id.gridView1);
		gridView.setAdapter(new ImageAdapter(this, bmpArray));
		gridView.setOnItemClickListener(itemClickListener);
		
		
	}
	
	
	private OnItemClickListener itemClickListener = new OnItemClickListener() {
	    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {	
	    	String[] split = files[position].split("\\.");
			String fileType = split[1].toString();
			String uriParsed = "file://" + directory + File.separator + files[position];
			Intent intent = new Intent();
			intent.setAction(android.content.Intent.ACTION_VIEW);
			Uri uri = Uri.parse(uriParsed);
			if (fileType.equals("jpg")) {
				intent.setDataAndType(uri, "image/*");
				startActivity(intent);
			}else if (fileType.equals("mp4")){
				intent.setDataAndType(uri, "video/*");
				startActivity(intent);
			}
	    }
	};
	/*
	 * This needs to be Async
	 */
	public static Bitmap decodeFile(String file, int size)  {
	    //Decode image size
	    BitmapFactory.Options o = new BitmapFactory.Options();
	    o.inJustDecodeBounds = true;
	    BitmapFactory.decodeFile(file, o);

	    //Find the correct scale value. It should be the power of 2.
	    int width_tmp = o.outWidth, height_tmp = o.outHeight;
	    int scale = 0;
	    scale = (int)Math.pow(2, (double)(scale-1));
	    while (true) {
	        if (width_tmp / 2 < size || height_tmp / 2 < size) {
	            break;
	        }
	        width_tmp /= 2;
	        height_tmp /= 2;
	        scale++;
	    }

	    //Decode with inSampleSize
	    BitmapFactory.Options o2 = new BitmapFactory.Options();
	    o2.inSampleSize = scale;
	    return BitmapFactory.decodeFile(file, o2);
	}
	
	

	
}
