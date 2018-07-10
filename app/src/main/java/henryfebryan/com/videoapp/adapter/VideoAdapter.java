package henryfebryan.com.videoapp.adapter;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.File;
import java.util.ArrayList;

import henryfebryan.com.videoapp.R;
import henryfebryan.com.videoapp.model.Video;

public class VideoAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater layoutInflater;
    private ArrayList<Video> videoList;

    public VideoAdapter(Context context, ArrayList<Video> videoList) {
        this.context = context;
        this.videoList = videoList;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return videoList.size();
    }

    @Override
    public Object getItem(int i) {
        return videoList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        final Video lVideo = (Video) getItem(i);
        if (view == null) {
            LayoutInflater infalInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = infalInflater.inflate(R.layout.layout_each_video, null);
        }

        ImageView iv_thumbnails = (ImageView) view.findViewById(R.id.iv_thumbnails);
        Bitmap bitmap = lVideo.getThumnail();
        iv_thumbnails.setImageBitmap(bitmap);
        iv_thumbnails.setScaleType(ImageView.ScaleType.FIT_XY);

        TextView tv_video_name = (TextView) view.findViewById(R.id.tv_video_name);
        TextView tv_video_dir = (TextView) view.findViewById(R.id.tv_video_dir);
        TextView tv_video_duration = (TextView) view.findViewById(R.id.tv_video_duration);
        TextView tv_video_modified = (TextView) view.findViewById(R.id.tv_video_modified);

        tv_video_name.setText(lVideo.getVideoName());
        tv_video_dir.setText(lVideo.getVideoDir());
        tv_video_duration.setText(lVideo.getDuration());
        tv_video_modified.setText(lVideo.getLastModified());

        return view;
    }
}
