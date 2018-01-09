package com.example.android.videodrag;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private final static String TAG = "MainActivity";
    ImageView imageView1;
    int mMediaWidth;
    int mMediaHeight;
    int mRotate;
    Activity mActivity;
    String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mActivity = this;
        imageView1 = (ImageView) findViewById(R.id.video1);
        imageView1.setOnClickListener(this);
        String uriPath = "android.resource://" + getPackageName() + "/raw/test1";
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(getApplication(), Uri.parse(uriPath));
        String width = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
        String height = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
        String rotate = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
        mRotate = Integer.valueOf(rotate);
        mMediaWidth = Integer.valueOf(width);
        mMediaHeight = Integer.valueOf(height);
        Bitmap test1 = mmr.getFrameAtTime();
        imageView1.setImageBitmap(test1);
        path = uriPath;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.video1) {
            startPreviewActivity();
        }
    }

    private void startPreviewActivity() {
        int location[] = new int[2];
        imageView1.getLocationOnScreen(location);
        Intent intent = new Intent(mActivity, PreviewVideoActivity.class);//你要跳转的界面
        intent.putExtra("left", location[0]);
        intent.putExtra("top", location[1]);
        intent.putExtra("height", imageView1.getHeight());
        intent.putExtra("width", imageView1.getWidth());
        intent.putExtra("path", path);
        intent.putExtra("mediaWidth", mMediaWidth);
        intent.putExtra("mediaHeight", mMediaHeight);
        intent.putExtra("rotateAngle", mRotate);
        mActivity.startActivity(intent);
        mActivity.overridePendingTransition(0, 0);
    }
}
