package henryfebryan.com.videoapp.model;

import android.graphics.Bitmap;

public class Video {
    Bitmap thumnail;

    private String VideoName;
    private String VideoDir;
    private String duration;
    private String lastModified;
    private String size;
    private String resolution;

    public Video() {
    }

    public Video(Bitmap thumnail, String videoName, String videoDir, String duration, String lastModified, String size, String resolution) {
        this.thumnail = thumnail;
        VideoName = videoName;
        VideoDir = videoDir;
        this.duration = duration;
        this.lastModified = lastModified;
        this.size = size;
        this.resolution = resolution;
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

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }
}
