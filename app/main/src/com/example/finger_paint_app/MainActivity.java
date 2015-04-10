package com.example.finger_paint_app;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends Activity implements View.OnClickListener{

    TouchDisplayView touchDisplayView;
    Button btnRed;
    Button btnYellow;
    Button btnGreen;
    Button btnBlue;
    Button btnViolet;
    Button btnOrange;

    ImageButton btnTriangle;
    ImageButton btnSquare;
    ImageButton btnCircle;

    private Button btnSave = null;
    private Button btnEmail = null;
    private Button btnExit = null;
    private Button btnReset = null;
    

    String mAppWidgetName = "screen shot";
    private static final String LOG_TAG = "MainActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);        
		setContentView(R.layout.activity_main);
		touchDisplayView = (TouchDisplayView)findViewById(R.id.paint_view);        

        btnRed = (Button) findViewById(R.id.toggleBtnRed);
        btnRed.setOnClickListener(this);

        btnYellow = (Button) findViewById(R.id.toggleBtnYellow);
        btnYellow.setOnClickListener(this);

        btnGreen = (Button) findViewById(R.id.toggleBtnGreen);
        btnGreen.setOnClickListener(this);      

        btnBlue = (Button) findViewById(R.id.toggleBtnBlue);
        btnBlue.setOnClickListener(this);

        btnViolet = (Button) findViewById(R.id.toggleBtnViolet);
        btnViolet.setOnClickListener(this);
        
        btnOrange = (Button) findViewById(R.id.toggleBtnOrange);
        btnOrange.setOnClickListener(this);

        btnTriangle = (ImageButton) findViewById(R.id.imgBtnTriangle);
        btnTriangle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("click","triangle was clicked");
                TouchDisplayView.shape = 0;
            }
        });
        
        btnSquare = (ImageButton) findViewById(R.id.imgBtnSquare);
        btnSquare.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.e("click","square was clicked");
				TouchDisplayView.shape = 1;				
			}
		});
        
        btnCircle = (ImageButton) findViewById(R.id.imgBtnCircle);
        btnCircle.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.e("click","circle was clicked");
				TouchDisplayView.shape = 2;					
			}
		});

        btnSave = (Button) findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int textId = R.string.saving_preview;

                Toast preToast = Toast.makeText(getBaseContext(), textId, Toast.LENGTH_SHORT);
                preToast.show();

                Bitmap bmp = getPreviewBitmap();
                if (saveImage(bmp, mAppWidgetName)) {
                    textId = R.string.preview_saved;
                } else {
                    textId = R.string.preview_save_error;
                }

                Toast postToast = Toast.makeText(getBaseContext(), textId, Toast.LENGTH_SHORT);
                postToast.show();

            }
        });

        btnEmail = (Button) findViewById(R.id.btnEmail);
        btnEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File file = buildFile(mAppWidgetName);
                if (file.exists()) {
                    Intent emailIntent = new Intent(Intent.ACTION_SEND);
                    emailIntent.setType("image/png");
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT,
                            getResources().getString(R.string.email_subject));
                    emailIntent.putExtra(Intent.EXTRA_TEXT,
                            getResources().getString(R.string.email_body));
                    emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
                    startActivity(emailIntent);
                } else {
                    Toast postToast = Toast.makeText(
                            getBaseContext(), R.string.no_preview, Toast.LENGTH_SHORT);
                    postToast.show();
                }
            }
        });
        
        btnExit = (Button) findViewById(R.id.btnExit);
        btnExit.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				System.exit(0);					
			}
		});
        
        btnReset = (Button) findViewById(R.id.btnReset);
        btnReset.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {				
				touchDisplayView.clearCanvas();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        Button colorPressed = (Button)v;        
        // determine which button was pressed        

        switch (colorPressed.getId()) {

            case R.id.toggleBtnRed: {
                Log.e("choose red","red button was clicked");
                TouchDisplayView.color = 0;
                btnRed.setSelected(true);
                btnYellow.setSelected(false);
                btnGreen.setSelected(false);
                btnBlue.setSelected(false);
                btnViolet.setSelected(false);
                btnOrange.setSelected(false);                
                break;
            }

            case R.id.toggleBtnYellow: {
                Log.e("choose yellow","yellow button was clicked");                
                TouchDisplayView.color = 1;
                btnRed.setSelected(false);
                btnYellow.setSelected(true);
                btnGreen.setSelected(false);
                btnBlue.setSelected(false);
                btnViolet.setSelected(false);
                btnOrange.setSelected(false);                
                break;
            }
            
            case R.id.toggleBtnGreen: {
                Log.e("choose green","green button was clicked");                
                TouchDisplayView.color = 2;
                btnRed.setSelected(false);
                btnYellow.setSelected(false);
                btnGreen.setSelected(true);
                btnBlue.setSelected(false);
                btnViolet.setSelected(false);
                btnOrange.setSelected(false);               
                break;
            }
            
            case R.id.toggleBtnBlue: {
                Log.e("choose blue","blue button was clicked");                
                TouchDisplayView.color = 3;
                btnRed.setSelected(false);
                btnYellow.setSelected(false);
                btnGreen.setSelected(false);
                btnBlue.setSelected(true);
                btnViolet.setSelected(false);
                btnOrange.setSelected(false);
                break;
            }
            
            case R.id.toggleBtnViolet: {
                Log.e("choose violet","violet button was clicked");                
                TouchDisplayView.color = 4;
                btnRed.setSelected(false);
                btnYellow.setSelected(false);
                btnGreen.setSelected(false);
                btnBlue.setSelected(false);
                btnViolet.setSelected(true);
                btnOrange.setSelected(false);                
                break;
            }
            
            case R.id.toggleBtnOrange: {
                Log.e("choose orange","orange button was clicked");                
                TouchDisplayView.color = 5;
                btnRed.setSelected(false);
                btnYellow.setSelected(false);
                btnGreen.setSelected(false);
                btnBlue.setSelected(false);
                btnViolet.setSelected(false);
                btnOrange.setSelected(true);            
                break;
            }
            
            default:
				break;            
        }

    }


    private File buildFile(String name) {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return null;
        }

        File path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS);
        int orientationCode = getResources().getConfiguration().orientation;
        String orientation;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            orientation = "landscape";
        } else if (orientationCode == Configuration.ORIENTATION_PORTRAIT) {
            orientation = "portrait";
        } else if (orientationCode == Configuration.ORIENTATION_SQUARE) {
            orientation = "square";
        } else {
            orientation = "undefined";
        }
        return new File(path, name + "_ori_" + orientation + ".png");
    }

    public Bitmap getPreviewBitmap() {
        
        Log.e(LOG_TAG,"width " + touchDisplayView.getWidth());
        Log.e(LOG_TAG,"height " + touchDisplayView.getHeight());

        Bitmap bmp = Bitmap.createBitmap(
        		touchDisplayView.getWidth(), touchDisplayView.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        touchDisplayView.draw(c);
        return bmp;        
    }

    private boolean saveImage(Bitmap bmp, String name) {
        File pic = buildFile(mAppWidgetName);
        if (pic == null) {
            Log.e(LOG_TAG, "External storage not present");
            return false;
        }

        pic.getParentFile().mkdirs();
        FileOutputStream fout = null;
        try {
            fout = new FileOutputStream(pic);
            if (!bmp.compress(Bitmap.CompressFormat.PNG, 100, fout)) {
                Log.e(LOG_TAG, "Failed to compress image");
                return false;
            }
            return true;
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error writing to disk: " + e);
        } finally {
            try {
                if (fout != null) {
                    fout.close();
                }
            } catch (IOException e) {
                Log.e(LOG_TAG, "Could not close file: " + e);
            }
        }
        return false;
    }

}





































