package henryfebryan.com.videoapp.model;

import android.graphics.Bitmap;

public class Video {
    private int VideoId;
    private String VideoName;
    private String VideoDir;
    Bitmap thumnail;
    private String duration;
    private String lastModified;

    public Video() {
    }

    public Video(int videoId, String videoName, String videoDir, Bitmap thumnail, String duration, String lastModified) {
        VideoId = videoId;
        VideoName = videoName;
        VideoDir = videoDir;
        this.thumnail = thumnail;
        this.duration = duration;
        this.lastModified = lastModified;
    }

    public int getVideoId() {
        return VideoId;
    }

    public void setVideoId(int videoId) {
        VideoId = videoId;
    }

    public String getVideoName() {
        return VideoName;
    }

    public void setVideoName(String videoName) {
        VideoName = videoName;
    }

    public String getVideoDir() {
        return VideoDir;
    }

    public void setVideoDir(String videoDir) {
        VideoDir = videoDir;
    }

    public Bitmap getThumnail() {
        return thumnail;
    }

    public void setThumnail(Bitmap thumnail) {
        this.thumnail = thumnail;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }
}
