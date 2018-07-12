package henryfebryan.com.videoapp;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import henryfebryan.com.videoapp.adapter.VideoAdapter;
import henryfebryan.com.videoapp.model.Video;

import static henryfebryan.com.videoapp.SystemFileDirectory.verifyStoragePermissions;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final int VIDEO_REQUEST_CODE = 100;

    private  static final String VIDEO_PATH = "VIDEO_PATH";
    private  static final String VIDEO_NAME = "VIDEO_NAME";
    private  static final String VIDEO_SIZE = "VIDEO_SIZE";
    private  static final String VIDEO_DURATION = "VIDEO_DURATION";
    private  static final String VIDEO_RESOLUTION = "VIDEO_RESOLUTION";
    private  static final String VIDEO_DATE_MODIFIED = "VIDEO_DATE_MODIFIED";

    Button btn_record;

    TextView tv_path;

    ListView lv_videolist;
    VideoAdapter adapter;

    ArrayList<Video> videoList;
    ArrayList<Video> videoListComplete;

    ProgressBar progressBar, pb_reload_list;

    AsyncTaskWait asyncTaskWait;

    boolean flag_first=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        verifyStoragePermissions(this);
        initView();
        initListener();
        initPath();
        new ReloadCompleteList().execute();
    }

    private void initView() {
        tv_path = (TextView) findViewById(R.id.tv_path);
        btn_record = (Button) findViewById(R.id.btn_record);
        lv_videolist = (ListView) findViewById(R.id.lv_videolist);
        pb_reload_list = (ProgressBar) findViewById(R.id.pb_reload_list);
    }

    private void initListener() {
        btn_record.setOnClickListener(this);
        lv_videolist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final TextView tv_video_dir =(TextView) view.findViewById(R.id.tv_video_dir);
                final TextView tv_video_name =(TextView) view.findViewById(R.id.tv_video_name);
                final TextView tv_video_modified =(TextView) view.findViewById(R.id.tv_video_modified);

                final String sDirVideo = tv_video_dir.getText().toString();
                final String sNameVideo = tv_video_name.getText().toString();
                final String sDateModified = tv_video_modified.getText().toString();

                Intent intent = new Intent(MainActivity.this,WatchVideoActivity.class);
                intent.putExtra(VIDEO_PATH, sDirVideo);
                intent.putExtra(VIDEO_NAME, sNameVideo);
                intent.putExtra(VIDEO_DURATION, milliSecondsToTimer(getFileVideoDuration(sDirVideo)));
                intent.putExtra(VIDEO_SIZE, convertSizeToString(getFileSize(sDirVideo)));
                intent.putExtra(VIDEO_RESOLUTION, getFileVideoResolution(sDirVideo));
                intent.putExtra(VIDEO_DATE_MODIFIED, sDateModified);
                startActivity(intent);
            }
        });

        lv_videolist.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if(scrollState == SCROLL_STATE_IDLE && lv_videolist.getLastVisiblePosition() == videoList.size()){
                    //this is to load more items ONLY if the previous items are loaded,
                    //and not to send two requests at the same time
                    if(asyncTaskWait == null || asyncTaskWait.getStatus() != AsyncTask.Status
                            .RUNNING){
                        progressBar.setVisibility(View.VISIBLE);
                        asyncTaskWait = new AsyncTaskWait(new WeakReference<Context>(MainActivity
                                .this));
                        asyncTaskWait.execute();
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
    }

    private void initPath() {
        final File dir = new File(Environment.getExternalStoragePublicDirectory("Documents").getAbsolutePath(), "AppsVideo");
        tv_path.setText("path: "+dir.toString());
    }

    private void setListViewFirst() {
        flag_first = true;
        videoList = new ArrayList<>(videoListComplete.subList(0,5));

        adapter = new VideoAdapter(this, videoList);
        lv_videolist.setAdapter(adapter);
        setListViewFooter();
    }

    private void setListViewFooter() {
        View view = LayoutInflater.from(this).inflate(R.layout.footer_listview_progressbar, null);
        progressBar = view.findViewById(R.id.progressBar);
        lv_videolist.addFooterView(progressBar);
    }

    private ArrayList<Video> readListFromFolder() {
        ArrayList<Video> readVideoList = new ArrayList<Video>();
        try {
            final File dir = new File(Environment.getExternalStoragePublicDirectory("Documents").getAbsolutePath(), "AppsVideo");
            File[] filelist = dir.listFiles();

            int loop = 0;
            if (filelist == null) {
                loop = 0;
            } else {
                loop = filelist.length;
            }

            for (int i = 0; i < loop; i++) {
                int lString = filelist[i].getName().length() - 4;
                if (filelist[i].getName().substring(lString).equalsIgnoreCase(".3gp")||filelist[i].getName().substring(lString).equalsIgnoreCase(".mp4") || filelist[i].getName().substring(lString).equalsIgnoreCase(".mpg")||filelist[i].getName().substring(lString).equalsIgnoreCase(".avi")||filelist[i].getName().substring(lString).equalsIgnoreCase(".wmv")||filelist[i].getName().substring(lString).equalsIgnoreCase(".flv") ){
                    Video video = new Video();
                    File filevideo = getFileFromPath(dir + "/" + filelist[i].getName());
                    Log.d("videodirComplete", dir + "/" + filelist[i].getName());

                    video.setVideoName(filelist[i].getName());
                    video.setVideoDir(dir + "/" + filelist[i].getName());

                    video.setThumnail(ThumbnailUtils.createVideoThumbnail(dir + "/" + filelist[i].getName(), MediaStore.Video.Thumbnails.MINI_KIND));
                    video.setDuration(milliSecondsToTimer(getFileVideoDuration(dir + "/" + filelist[i].getName())));
                    video.setLastModified(getDate(filevideo.lastModified(), "EEE, d MMM yyyy HH:mm"));
                    video.setSize(convertSizeToString(getFileSize(dir + "/" + filelist[i].getName())));
                    video.setResolution(getFileVideoResolution(dir + "/" + filelist[i].getName()));

                    readVideoList.add(video);
                }
            }
        }
        catch (Exception e){
            Log.d("Exception",e.toString());
        }
        return readVideoList;
    }

    private File getFileFromPath(String path){
        return new File(path);
    }

    private String getFileVideoResolution(String path){
        MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
        metaRetriever.setDataSource(path);
        String height = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
        String width = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
        return width+"x"+height;
    }

    private long getFileVideoDuration(String path){
        MediaPlayer mp = MediaPlayer.create(MainActivity.this, Uri.parse(path));
        long duration = mp.getDuration();
        return duration;
    }

    private int getFileSize(String path){
        return Integer.parseInt(String.valueOf(getFileFromPath(path).length()/1024));
    }

    public String convertSizeToString(int size){
        String hrSize = "";

        double m = size/1024.0;
        double g = size/1048576.0;
        double t = size/1073741824.0;

        DecimalFormat dec = new DecimalFormat("0.00");

        if (t > 1) {
            hrSize = dec.format(t).concat(" TB");
        } else if (g > 1) {
            hrSize = dec.format(g).concat(" GB");
        } else if (m > 1) {
            hrSize = dec.format(m).concat(" MB");
        } else {
            hrSize = dec.format(size).concat(" KB");
        }
        return hrSize;
    }

    private void reloadCompleteList(){
        videoListComplete = readListFromFolder();
    }

    private void addMoreItems(){
        int size = videoList.size();
        for(int i=1;i<=5;i++){
            if((size + i) < videoListComplete.size()){
                videoList.add(videoListComplete.get(size + i));
            }
        }
        adapter.notifyDataSetChanged();
        progressBar.setVisibility(View.GONE);
    }

    public static String getDate(long milliSeconds, String dateFormat) {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    public String milliSecondsToTimer(long milliseconds) {
        String finalTimerString = "";
        String secondsString = "";

        // Convert total duration into time
        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
        // Add hours if there
        if (hours > 0) {
            finalTimerString = hours + ":";
        }

        // Prepending 0 to seconds if it is one digit
        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        // return timer string
        return finalTimerString;
    }

    public File getFilepath(){
        File filefolder = new File(Environment.getExternalStoragePublicDirectory("Documents").getAbsolutePath(), "AppsVideo");
        if (!filefolder.exists()) {
            filefolder.mkdirs();
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());

        String filename = timeStamp+".3gp";
        File file_video = new File(filefolder,filename);

        return file_video;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_record:
                captureVideo();
                break;
        }
    }

    private void captureVideo() {
        try{
            Intent camera_intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            File video_file = getFilepath();
            Uri video_uri = null;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                video_uri = FileProvider.getUriForFile(getApplicationContext(), getPackageName() + ".provider", video_file);
            } else {
                video_uri = Uri.fromFile(video_file);
            }

            camera_intent.putExtra(MediaStore.EXTRA_OUTPUT,video_uri);
            camera_intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY,1);
            if (camera_intent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(camera_intent, VIDEO_REQUEST_CODE);
            }
            Log.d("Capture","Sukses");
        }catch (Exception e){
            Log.d("ErrorCapt",e.toString());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == VIDEO_REQUEST_CODE){
            if(resultCode==RESULT_OK){
                new ReloadCompleteList().execute();
            }
        }
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //the one second is over, load more data
            addMoreItems();
        }
    };
    @Override
    protected void onResume(){
        super.onResume();
        IntentFilter intentFilter = new IntentFilter("result");
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter);
    }
    @Override
    protected void onPause(){
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        super.onPause();
    }

    class ReloadCompleteList extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pb_reload_list.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try{
                reloadCompleteList();
            }catch (Exception e){
                Log.d("Exception Load",e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(!flag_first) {
                setListViewFirst();
            }
            pb_reload_list.setVisibility(View.GONE);
        }
    }


}
