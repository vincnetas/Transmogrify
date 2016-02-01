package com.vincentas.transmogrify;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.vincentas.transmogrify.engine.trace.Trace;
import com.vincentas.transmogrify.util.FreehandView;

import java.io.Serializable;

public class MainActivity extends AppCompatActivity {

    private FreehandView imageView;

    public static final String IMAGE_URI = "image_uri";

    private Uri mCurrentPhotoUri;

    private Switch mSwitch;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initImageUri();

        imageView = (FreehandView)this.findViewById(R.id.view);
        imageView.setOrientation(SubsamplingScaleImageView.ORIENTATION_0);
        imageView.setPanLimit(SubsamplingScaleImageView.PAN_LIMIT_OUTSIDE);
        imageView.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CUSTOM);

        imageView.setImage(ImageSource.uri(mCurrentPhotoUri));
        imageView.onTraceAvailable(new FreehandView.OnTraceAvailableListener() {
            @Override
            public void traceAvailable(Trace from, Trace to) {
                doIt(from, to);
            }
        });

        mSwitch = (Switch) findViewById(R.id.switch1);
        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                imageView.setPanning(!isChecked);
            }
        });

        // This is hack, because initially getScale method returns zero
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
                float originalScale = imageView.getScale();
                imageView.setMinScale(originalScale / 2f);
            }
        }).start();

    }

    private void initImageUri() {
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                mCurrentPhotoUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
            }
        } else {
            mCurrentPhotoUri = (Uri) getIntent().getExtras().getParcelable(IMAGE_URI);
        }
    }

    private void doIt(final Trace from, final Trace to) {
        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra(ResultActivity.FROM_TRACE, (Serializable) from);
        intent.putExtra(ResultActivity.TO_TRACE, (Serializable) to);
        intent.putExtra(ResultActivity.SOURCE_URI, mCurrentPhotoUri);
        startActivity(intent);
    }

    public void panOn(View view) {
        mSwitch.setChecked(false);
    }

    public void drawOn(View view) {
        mSwitch.setChecked(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        imageView.reset();
    }
}
