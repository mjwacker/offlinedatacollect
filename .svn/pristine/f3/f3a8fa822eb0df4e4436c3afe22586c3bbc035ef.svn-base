package gov.mt.mdt.data;

import java.lang.ref.WeakReference;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory.Options;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.widget.ImageView;

public class BitmapWorkerTask extends AsyncTask<Long, Void, Bitmap> {

    private Options mOptions;

    private WeakReference<ImageView> mImageViewWeakReference;
    private ContentResolver mContentResolver;
    private int mPosition;


        public BitmapWorkerTask(ImageView imageView, ContentResolver cr) {
        mContentResolver = cr;
        mImageViewWeakReference = new WeakReference<ImageView>(imageView);
        mOptions = new Options();
        mOptions.inSampleSize = 4;
        mPosition = imageView.getId();
    }



    @Override
    protected Bitmap doInBackground(Long... params) {
        Bitmap result;
            result = MediaStore.Images.Thumbnails.getThumbnail(
                mContentResolver, params[0],
                MediaStore.Images.Thumbnails.MINI_KIND, mOptions);
        return result;
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        super.onPostExecute(result);
            if (mImageViewWeakReference != null
                    && mImageViewWeakReference.get() != null
                        && mPosition == mImageViewWeakReference.get().getId())
                mImageViewWeakReference.get().setImageBitmap(result);
    }

}
