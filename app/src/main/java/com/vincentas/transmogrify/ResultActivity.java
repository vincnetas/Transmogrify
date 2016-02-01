package com.vincentas.transmogrify;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.vincentas.transmogrify.engine.trace.Trace;
import com.vincentas.transmogrify.engine.TraceEngine;
import com.vincentas.transmogrify.engine.TraceHandle;

import java.io.IOException;

public class ResultActivity extends AppCompatActivity implements TraceEngine.TraceProgress {

    public static final String FROM_TRACE = "from_trace";

    public static final String TO_TRACE = "to_trace";

    public static final String SOURCE_URI = "source_uri";

    private static final int SHARE_RESULT = 1234;

    private SubsamplingScaleImageView imageView;

    private ImageButton shareButton;

    private Bitmap resultBitmap;

    private Uri shareUri;

    private TraceHandle traceHandle = new TraceHandle();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        shareButton = (ImageButton) findViewById(R.id.shareButton);
        shareButton.setVisibility(View.INVISIBLE);

        Uri mCurrentPhotoUri = getIntent().getParcelableExtra(SOURCE_URI);
        final Trace from = (Trace) getIntent().getSerializableExtra(FROM_TRACE);
        final Trace to = (Trace) getIntent().getSerializableExtra(TO_TRACE);

        final Bitmap bitmap;

        if (mCurrentPhotoUri.getScheme().equals("content")) {
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), mCurrentPhotoUri);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        } else if (mCurrentPhotoUri.getScheme().equals("file")) {
            bitmap = BitmapFactory.decodeFile(mCurrentPhotoUri.getPath());
        } else {
            return;
        }

        resultBitmap = Bitmap.createBitmap(bitmap.getWidth() / 1, bitmap.getHeight() / 1, bitmap.getConfig());

        if (traceHandle != null) {
            traceHandle.cancel();
        }
        traceHandle = TraceEngine.render(bitmap, from, to, resultBitmap, this);

        imageView = (SubsamplingScaleImageView) findViewById(R.id.imageView);
        imageView.setImage(ImageSource.bitmap(resultBitmap));
    }

    public void share(View view) {
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), resultBitmap, "Transmogrified", null);
        shareUri = Uri.parse(path);

        Intent share = new Intent(Intent.ACTION_SEND).setType("image/jpeg").putExtra(Intent.EXTRA_STREAM, shareUri);
        startActivityForResult(Intent.createChooser(share, "Select"), SHARE_RESULT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SHARE_RESULT) {
            ContentResolver contentResolver = getContentResolver();
            contentResolver.delete(shareUri, null, null);
        }
    }

    @Override
    public void done() {
        shareButton.post(new Runnable() {
            @Override
            public void run() {
                shareButton.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onProgress(int current, int total, Bitmap bitmap) {
        imageView.postInvalidate();
    }
}
