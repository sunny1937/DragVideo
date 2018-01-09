package com.example.android.videodrag;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;

import java.io.IOException;


public class PreviewVideoActivity extends Activity implements TextureView
        .SurfaceTextureListener, DragVideoView.Callback {
    private static final String TAG = "PreviewVideoActivity";
    private MediaPlayer mMediaPlayer;
    private DragVideoView mDragVideoView;
    private TextureView mVideoView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_preview);
        mMediaPlayer = new MediaPlayer();
        mDragVideoView = (DragVideoView) findViewById(R.id.drag_view);
        mDragVideoView.setCallback(this);

        mVideoView = (TextureView) findViewById(R.id.video_view);
        mVideoView.setSurfaceTextureListener(this);

        int originLeft = getIntent().getIntExtra("left", 0);
        int originTop = getIntent().getIntExtra("top", 0);
        int originHeight = getIntent().getIntExtra("height", 0);
        int originWidth = getIntent().getIntExtra("width", 0);
        int mediaWidth = getIntent().getIntExtra("mediaWidth", 0);
        int mediaHeight = getIntent().getIntExtra("mediaHeight", 0);
        int rotateAngle = getIntent().getIntExtra("rotateAngle", 0);
        Log.i(TAG, "rotateAngle=" + rotateAngle);
        String path = getIntent().getStringExtra("path");
        startPlayVideo(path, mediaWidth, mediaHeight, originLeft, originTop, originWidth, originHeight, rotateAngle);
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMediaPlayer.release();
        mMediaPlayer = null;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mMediaPlayer.setSurface(new Surface(surface));
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        finish();
        overridePendingTransition(0, 0);
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    @Override
    public void onVideoDisappear() {
        if (null != mMediaPlayer) {
            mMediaPlayer.stop();
        }
        mDragVideoView.setVisibility(View.GONE);
        finish();
        overridePendingTransition(0, 0);
    }

    private void startPlayVideo(String path, final int mediaWidth, final int mediaHeight, final int left, final int
            top, final int width, final int height, final float rotateAngle) {
        mMediaPlayer.reset();
        try {
            mMediaPlayer.setDataSource(getApplicationContext(), Uri.parse(path));
            mMediaPlayer.setLooping(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mMediaPlayer.start();
                }
            });
            mMediaPlayer.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mDragVideoView.setVisibility(View.VISIBLE);
        mDragVideoView.post(new Runnable() {
            @Override
            public void run() {
                mDragVideoView.show(mediaWidth, mediaHeight, left, top, width, height, rotateAngle);
            }
        });
    }

}
