package com.qtfreet.yunbo.ui.activity;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.qtfreet.yunbo.R;
import com.qtfreet.yunbo.util.SystemUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.Bind;
import butterknife.ButterKnife;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class VideoActivity extends Activity implements OnClickListener, OnSeekBarChangeListener, View.OnSystemUiVisibilityChangeListener, IMediaPlayer.OnPreparedListener, IMediaPlayer.OnCompletionListener, IMediaPlayer.OnBufferingUpdateListener, IMediaPlayer.OnErrorListener {

    private View controlView;
    private TextView tvDuration;
    private TextView tvCurrentTime;
    private TextView tvName;
    private ImageView ivBack;
    private ImageView ivPlayState;
    IjkMediaPlayer ijkMediaPlayer;
    private SeekBar seekBar;
    SurfaceHolder holder;
    private String videoPath;
    private String videoName;
    private View main;
    @Bind(R.id.pb_search_wait)
    ProgressBar progressBar;


    SurfaceView surfaceView;
    String cookie = "";

    public static final String NAME = "name";
    public static final String HASH = "hash";
    public static final String COOKIE = "cookie";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        videoName = getIntent().getExtras().getString(NAME);
        videoPath = getIntent().getExtras().getString(HASH);
        cookie = getIntent().getExtras().getString(COOKIE);
        if (videoPath.equals("")) {
            Toast.makeText(this, "未找到播放链接", Toast.LENGTH_SHORT).show();
            return;
        }
        main = getLayoutInflater().inflate(R.layout.activity_mv, null);
        setContentView(main);
        main.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        hideNag();
        initControlView();

        initVideoView();

        initControlListener();


        initThread();
    }

    private void hideNag() {
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                m.sendEmptyMessage(0);

            }
        };
        Timer timer = new Timer();
        timer.schedule(timerTask, 0, 5000);
    }


    private android.os.Handler m = new android.os.Handler(new android.os.Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    if (main.getSystemUiVisibility() == View.SYSTEM_UI_FLAG_VISIBLE) {
                        main.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
                    }
                    if (isControlShowing) {
                        m.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (isControlShowing) {
                                    dismissControlView();
                                }
                            }
                        }, 3000);
                    }

                    break;
            }
            return true;
        }
    });
    private boolean isShowCurrentTime = true;
    ExecutorService e;

    private void initThread() {
        e = Executors.newSingleThreadExecutor();
        e.execute(new Runnable() {
            @Override
            public void run() {
                while (isShowCurrentTime) {
                    try {
                        Thread.sleep(1000);

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    runOnUiThread(new Runnable() {
                        public void run() {
                            updataProgressBar();
                        }
                    });

                }

            }
        });


    }

    @Override
    protected void onDestroy() {
        isShowCurrentTime = false;
        super.onDestroy();
        if (ijkMediaPlayer != null) {
            ijkMediaPlayer = null;
        }


    }

    private void updataProgressBar() {
        if (ijkMediaPlayer == null) {
            return;
        }
        if (!isShowCurrentTime) {
            return;
        }
        int currentTime = (int) (ijkMediaPlayer.getCurrentPosition() / 1000);
        tvCurrentTime.setText(SystemUtil.generateTime(ijkMediaPlayer.getCurrentPosition()));
        seekBar.setProgress(currentTime);
    }


    private void initVideoView() {
        ButterKnife.bind(this);
        progressBar.setVisibility(View.VISIBLE);
        surfaceView = (SurfaceView) findViewById(R.id.vv);
        holder = surfaceView.getHolder();
        Log.e("TAG", videoPath + "      ");
        ijkMediaPlayer = new IjkMediaPlayer();
        ijkMediaPlayer.setOption(IjkMediaPlayer.FFP_PROPV_DECODER_AVCODEC, "headers", "Cookie: FTN5K=" + cookie + ";\n");
        try {
            ijkMediaPlayer.setDataSource(videoPath);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        ijkMediaPlayer.prepareAsync();

        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {

            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                ijkMediaPlayer.setDisplay(holder);
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });
        ijkMediaPlayer.setOnErrorListener(this);
        ijkMediaPlayer.setOnBufferingUpdateListener(this);
        ijkMediaPlayer.setOnPreparedListener(this);
        ijkMediaPlayer.setOnCompletionListener(this);


    }


    private void initControlView() {
        controlView = findViewById(R.id.control_rl);
        ivBack = (ImageView) findViewById(R.id.iv_back);
        ivPlayState = (ImageView) findViewById(R.id.iv_mode);
        tvCurrentTime = (TextView) findViewById(R.id.tv_current_time);
        tvDuration = (TextView) findViewById(R.id.tv_duration_time);
        tvName = (TextView) findViewById(R.id.tv_name);
        seekBar = (SeekBar) findViewById(R.id.seek_bar);
        tvName.setText(videoName);
    }

    private void initControlListener() {
        ivBack.setOnClickListener(this);

        ivPlayState.setOnClickListener(this);
        tvName.setOnClickListener(this);
        seekBar.setOnSeekBarChangeListener(this);
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.iv_mode:
                boolean isPlaying = ijkMediaPlayer.isPlaying();
                if (isPlaying) {
                    ijkMediaPlayer.pause();
                    ivPlayState.setSelected(true);
                } else {
                    ijkMediaPlayer.start();
                    ivPlayState.setSelected(false);
                }
                break;

            default:
                break;
        }
    }

    private boolean isControlShowing = false;

    private void dismissControlView() {
        controlView.setVisibility(View.GONE);

        isControlShowing = false;
    }

    private void showControlView() {
        controlView.setVisibility(View.VISIBLE);
        isControlShowing = true;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {


            if (!isControlShowing) {

                showControlView();
            }
        }

        return super.onTouchEvent(event);
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
                                  boolean fromUser) {
        if (!fromUser) {
            return;
        }
        if (seekBar == this.seekBar) {
            ijkMediaPlayer.seekTo(progress * 1000);
        }

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }


    @Override
    public void onSystemUiVisibilityChange(int visibility) {

    }


    @Override
    public void onPrepared(IMediaPlayer mp) {
        mp.start();
        progressBar.setVisibility(View.GONE);

        int duration = (int) (ijkMediaPlayer.getDuration() / 1000);
        seekBar.setMax(duration);
        tvDuration.setText(SystemUtil.generateTime(ijkMediaPlayer.getDuration()));

    }

    @Override
    public void onCompletion(IMediaPlayer mp) {
        isShowCurrentTime = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (ijkMediaPlayer.isPlaying()) {
            ijkMediaPlayer.pause();
            ijkMediaPlayer.stop();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //捕获返回键按下的事件
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public void onBufferingUpdate(IMediaPlayer mp, int percent) {


    }

    @Override
    public boolean onError(IMediaPlayer mp, int what, int extra) {
        Toast.makeText(VideoActivity.this, "播放该链接出错，请重新尝试", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                VideoActivity.this.finish();
            }
        }, 2000);
        return false;
    }
}
