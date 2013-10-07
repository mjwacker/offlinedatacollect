package gov.mt.mdt.data;

import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory.Options;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;

public class MediaCursorAdapter extends CursorAdapter {
	private LayoutInflater mInflater;
    private final static int mColumnID = 0;
    private Options mOptions;

    public MediaCursorAdapter(Context context, Cursor c) {
        super(context, c);

        mInflater = LayoutInflater.from(context);
        mOptions = new Options();
        mOptions.inSampleSize = 4;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder holder = (ViewHolder) view.getTag();
        holder.thumbImg.setId(cursor.getPosition());
        BitmapWorkerTask imageLoader = new BitmapWorkerTask(holder.thumbImg, context.getContentResolver());
        imageLoader.execute(cursor.getLong(mColumnID));
   // Log.i("Prototype", "bindView : " + cursor.getPosition());
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        //Log.i("Prototype", "newView : " + cursor.getPosition());
        View view = mInflater.inflate(R.layout.image_grid, null);
        ViewHolder holder = new ViewHolder(view);
        view.setTag(holder);
        return view;
    }


    private static class ViewHolder {
        ImageView thumbImg, dragImg;

        ViewHolder(View base) {
            thumbImg = (ImageView) base.findViewById(R.id.grid_item_image);
            dragImg = (ImageView) base.findViewById(R.id.grid_item_image);
        }
    }

}

