package com.alan.homeautomationapp;

import android.content.Context;
import android.net.Uri;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.util.VLCVideoLayout;

public class VideoStreamPlayer {

    private final LibVLC libVlc;
    private final MediaPlayer mediaPlayer;
    private final VLCVideoLayout videoLayout;

    public VideoStreamPlayer(Context context, VLCVideoLayout videoLayout) {
        libVlc = new LibVLC(context);
        mediaPlayer = new MediaPlayer(libVlc);
        this.videoLayout = videoLayout;
    }

    public void startVideo(String url) {
        mediaPlayer.attachViews(videoLayout, null, false, false);

        Media media = new Media(libVlc, Uri.parse(url));
        media.setHWDecoderEnabled(true, false);
        media.addOption(":network-caching=200");


        mediaPlayer.setMedia(media);
        media.release();
        mediaPlayer.play();
    }

    public void stopVideo() {
        mediaPlayer.stop();
        mediaPlayer.detachViews();
    }

    public void releaseVideo() {
        mediaPlayer.release();
        libVlc.release();
    }
}