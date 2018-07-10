package henryfebryan.com.videoapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
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
    private  static final String VIDEO_DATE_MODIFIED = "VIDEO_DATE_MODIFIED";

    Button btn_record,btn_reload;
    ListView lv_videolist;
    ArrayList<Video> videoList;
    VideoAdapter adapter;
    ArrayList<String> filenameList;

    TextView tv_path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        verifyStoragePermissions(this);
        initView();
        initPath();
        //loadList();
        new LoadList().execute("x");
    }

    private void initPath() {
        final File dir = new File(Environment.getExternalStoragePublicDirectory("Documents").getAbsolutePath(), "AppsVideo");
        tv_path.setText("path: "+dir.toString());
    }

    private void initView() {
        tv_path = (TextView) findViewById(R.id.tv_path);
        btn_record = (Button) findViewById(R.id.btn_record);
        btn_reload = (Button) findViewById(R.id.btn_reload);
        lv_videolist = (ListView) findViewById(R.id.lv_videolist);
        btn_record.setOnClickListener(this);
        btn_reload.setOnClickListener(this);
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
                intent.putExtra(VIDEO_DATE_MODIFIED, sDateModified);
                startActivity(intent);
            }
        });
    }

    private boolean loadList() {
        try {
            final File dir = new File(Environment.getExternalStoragePublicDirectory("Documents").getAbsolutePath(), "AppsVideo");
            File[] filelist = dir.listFiles();
            filenameList = new ArrayList<String>();

            int loop = 0;
            if (filelist == null) {
                loop = 0;
            } else {
                loop = filelist.length;
            }

            videoList = new ArrayList<Video>();
            for (int i = 0; i < loop; i++) {
                int lString = filelist[i].getName().length() - 4;
                if (filelist[i].getName().substring(lString).equalsIgnoreCase(".3gp")||filelist[i].getName().substring(lString).equalsIgnoreCase(".mp4") || filelist[i].getName().substring(lString).equalsIgnoreCase(".mpg")||filelist[i].getName().substring(lString).equalsIgnoreCase(".avi")||filelist[i].getName().substring(lString).equalsIgnoreCase(".wmv")||filelist[i].getName().substring(lString).equalsIgnoreCase(".flv") ){
                    Video video = new Video();
                    video.setVideoName(filelist[i].getName());

                    video.setThumnail(ThumbnailUtils.createVideoThumbnail(dir + "/" + filelist[i].getName(), MediaStore.Video.Thumbnails.MINI_KIND));
                    Log.d("videodir", dir + "/" + filelist[i].getName());
                    video.setVideoDir(dir + "/" + filelist[i].getName());

                    MediaPlayer mp = MediaPlayer.create(MainActivity.this, Uri.parse(dir + "/" + filelist[i].getName()));
                    long duration = mp.getDuration();
                    video.setDuration(milliSecondsToTimer(duration));

                    File filevideo = new File(dir + "/" + filelist[i].getName());
                    video.setLastModified(getDate(filevideo.lastModified(), "EEE, d MMM yyyy HH:mm"));
                    videoList.add(video);
                }
            }
            return true;
        }
        catch (Exception e){
            Log.d("Exception",e.toString());
            return false;
        }
    }

    public static String getDate(long milliSeconds, String dateFormat)
    {
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

    static String getTimeString(Long millis) {
        return String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
    }

    private void setAdapterList() {
        try {
            adapter = new VideoAdapter(MainActivity.this, videoList);
            lv_videolist.setAdapter(adapter);
        }
        catch (Exception e){
            Log.d("ExceptionSetadapter",e.toString());
        }
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
            case R.id.btn_reload:
                new LoadList().execute("x");
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
                new LoadList().execute("x");
            }
            else {
               // Toast.makeText(this, "Video Capture Failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    class LoadList extends AsyncTask<String,Integer,Boolean> {

        ProgressDialog bar = new ProgressDialog(MainActivity.this);
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            bar.setCancelable(false);
            bar.setMessage("Loading...");
            bar.setIndeterminate(true);
            bar.setCanceledOnTouchOutside(false);
            bar.show();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            bar.dismiss();
            if(result){
                Toast.makeText(getApplicationContext(),"Load Done",      Toast.LENGTH_SHORT).show();
                setAdapterList();
            }
            else {
                Toast.makeText(getApplicationContext(),"Load Error",      Toast.LENGTH_SHORT).show();
            }

        }
        @Override
        protected Boolean doInBackground(String... f_name) {
            return loadList();
        }
    }

}
