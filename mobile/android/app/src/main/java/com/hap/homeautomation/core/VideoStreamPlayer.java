package com.hap.homeautomation.core;

import android.content.Context;
import android.net.Uri;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.util.VLCVideoLayout;

import java.util.ArrayList;

public class VideoStreamPlayer {

    private final LibVLC libVlc;
    private final MediaPlayer mediaPlayer;
    private final VLCVideoLayout videoLayout;

    private boolean attached = false;

    public VideoStreamPlayer(
            Context context,
            VLCVideoLayout videoLayout
    ) {

        ArrayList<String> options = new ArrayList<>();

        options.add("--network-caching=150");
        options.add("--rtsp-tcp");

        libVlc = new LibVLC(context, options);

        mediaPlayer = new MediaPlayer(libVlc);

        this.videoLayout = videoLayout;
    }

    public void startVideo(String url) {

        if (!attached) {

            mediaPlayer.attachViews(
                    videoLayout,
                    null,
                    false,
                    false
            );

            attached = true;
        }

        Media media = new Media(libVlc, Uri.parse(url));

        media.setHWDecoderEnabled(true, false);

        media.addOption(":clock-jitter=0");
        media.addOption(":clock-synchro=0");
        media.addOption(":live-caching=150");

        mediaPlayer.setMedia(media);

        media.release();

        mediaPlayer.play();
    }

    public void stopVideo() {

        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
    }

    public void releaseVideo() {

        stopVideo();

        if (attached) {
            mediaPlayer.detachViews();
            attached = false;
        }

        mediaPlayer.release();
        libVlc.release();
    }
}