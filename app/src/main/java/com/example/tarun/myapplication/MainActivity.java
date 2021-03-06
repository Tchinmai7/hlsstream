package com.example.tarun.myapplication;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.TimedText;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;


public class MainActivity extends Activity implements MediaPlayer.OnTimedTextListener,SeekBar.OnSeekBarChangeListener {
public  SurfaceView surface;
    MediaPlayer  player;
    SurfaceHolder holder;
    int length=0;
    private TextView txtDisplay;
    private static Handler handler = new Handler();
    public SeekBar seekBar;
    public boolean subsboolean=true;
    public static String movie="";
    public static String subs="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtDisplay=(TextView)findViewById(R.id.txtDisplay);
        final Button startB=(Button)findViewById(R.id.button);
        Button stopB=(Button)findViewById(R.id.button1);
         startB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startB.setText("resume");
                if(player==null)
                  startTrack();
                else
                    player.start();
            }
        });
        Button subsbut=(Button)findViewById(R.id.subs);
        subsbut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subsboolean=!subsboolean;
            }
        });
        seekBar=(SeekBar)findViewById(R.id.seekbar);
        seekBar.setOnSeekBarChangeListener(this);
        stopB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pausetrack();
            }
        });
    }

    private void pausetrack()
    {
        player.pause();
        length=player.getCurrentPosition();
        Log.d("length", "" + length);
    }
    @Override
    public void onTimedText(final MediaPlayer mp, final TimedText text) {
        if (text != null) {
            Log.d("timed","timed");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    int seconds = mp.getCurrentPosition() / 1000;
                    if(subsboolean)
                      txtDisplay.setText(""+text.getText()+"");
                    else
                        txtDisplay.setText("");
                }
            });
        }
    }
    private void startTrack()
    {
        player = new MediaPlayer();
        surface = (SurfaceView)findViewById(R.id.surface_view);
        holder = surface.getHolder();
        try {

            player.setDisplay(holder);
            player.setDataSource(movie);
            player.prepare();

            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                  try {
                      String file = getSubtitleFile(subs);
                      player.addTimedTextSource(file, MediaPlayer.MEDIA_MIMETYPE_TEXT_SUBRIP);
                      int textTrackIndex = findTrackIndexFor(
                              MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_TIMEDTEXT, player.getTrackInfo());
                      if (textTrackIndex >= 0) {
                          player.selectTrack(textTrackIndex);
                      } else {
                          Log.w("ssss", "Cannot find text track!");
                      }
                      player.setOnTimedTextListener(MainActivity.this);
                      player.start();
                      int time= player.getDuration();
                      seekBar.setMax(time);
                      seekBar.postDelayed(onEverySecond, 1000);
                     Log.d("time", time + "");
                  }catch (Exception e)
                  {
                      e.printStackTrace();
                  }
                }
            });
        }
        catch (Exception e)
        {e.printStackTrace();
        }


    }
    private int findTrackIndexFor(int mediaTrackType, MediaPlayer.TrackInfo[] trackInfo) {
        int index = -1;
        for (int i = 0; i < trackInfo.length; i++) {
            if (trackInfo[i].getTrackType() == mediaTrackType) {
                return i;
            }
        }
        return index;
    }
    private Runnable onEverySecond=new Runnable() {

        @Override
        public void run() {

            if(seekBar != null) {
                seekBar.setProgress(player.getCurrentPosition());
            }

            if(player.isPlaying()) {
                seekBar.postDelayed(onEverySecond, 1000);
            }

        }
    };
     private String getSubtitleFile(String url) {
         getSubs s=new getSubs();
         s.execute(url);
        String fname=Environment
                 .getExternalStorageDirectory().toString()
                 + "/subs.srt";
         Log.d("name",fname);
         return fname;
     }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        if(fromUser) {
            // this is when actually seekbar has been seeked to a new position
            player.seekTo(progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    private class getSubs extends AsyncTask<String,String,String>
    {

        @Override
        protected String doInBackground(String... params) {
            int count;
            try {
                URL url = new URL(params[0]);
                URLConnection conection = url.openConnection();
                conection.connect();

                // this will be useful so that you can show a tipical 0-100%
                // progress bar

                // download the file
                InputStream input = new BufferedInputStream(url.openStream(),
                        8192);

                // Output stream
                OutputStream output = new FileOutputStream(Environment
                        .getExternalStorageDirectory().toString()
                        + "/subs.srt");
                Log.d("name,,",Environment
                        .getExternalStorageDirectory().toString()
                        + "/subs.srt");

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    // writing data to file
                    output.write(data, 0, count);
                }

                // flushing output
                output.flush();

                // closing streams
                output.close();
                input.close();

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }

            return null;

        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
