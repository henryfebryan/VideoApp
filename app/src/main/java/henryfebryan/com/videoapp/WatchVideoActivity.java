package henryfebryan.com.videoapp;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import henryfebryan.com.videoapp.model.Video;

public class WatchVideoActivity extends AppCompatActivity {

    Button btn_back;
    TextView tv_title_video, tv_video_modified, tv_video_duration, tv_video_size, tv_video_resolution, tv_video_path;
    VideoView videoView;

    private  static final String VIDEO_PATH = "VIDEO_PATH";
    private  static final String VIDEO_NAME = "VIDEO_NAME";
    private  static final String VIDEO_SIZE = "VIDEO_SIZE";
    private  static final String VIDEO_DURATION = "VIDEO_DURATION";
    private  static final String VIDEO_RESOLUTION = "VIDEO_RESOLUTION";
    private  static final String VIDEO_DATE_MODIFIED = "VIDEO_DATE_MODIFIED";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch_video);
        initView();
        initListener();
        setData();
    }

    private void setData() {
        Bundle extras = getIntent().getExtras();
        final String sDirVideo = getIntent().getStringExtra(VIDEO_PATH);
        final String sNameVideo = getIntent().getStringExtra(VIDEO_NAME);
        final String sDateModified = getIntent().getStringExtra(VIDEO_DATE_MODIFIED);
        final String sDuration = getIntent().getStringExtra(VIDEO_DURATION);
        final String sSize = getIntent().getStringExtra(VIDEO_SIZE);
        final String sResolution = getIntent().getStringExtra(VIDEO_RESOLUTION);

        videoView.setVideoPath(sDirVideo);

        tv_title_video.setText(sNameVideo);
        tv_video_modified.setText(sDateModified);
        tv_video_duration.setText(sDuration);
        tv_video_size.setText(sSize);
        tv_video_resolution.setText(sResolution);
        tv_video_path.setText(sDirVideo);

        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);

        videoView.start();
    }

    private void initListener() {
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void initView() {
        btn_back = (Button) findViewById(R.id.btn_back);
        videoView = (VideoView) findViewById(R.id.videoView);
        tv_title_video = (TextView) findViewById(R.id.tv_title_video);
        tv_video_modified = (TextView) findViewById(R.id.tv_video_modified);
        tv_video_duration = (TextView) findViewById(R.id.tv_video_duration);
        tv_video_size = (TextView) findViewById(R.id.tv_video_size);
        tv_video_resolution = (TextView) findViewById(R.id.tv_video_resolution);
        tv_video_path = (TextView) findViewById(R.id.tv_video_path);
    }
}
