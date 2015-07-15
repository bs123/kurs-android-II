package de.mvhs.android.zeiterfassung;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class SensorActivity extends ActionBarActivity {

    Button _gpsCommand;
    TextView _longitude;
    TextView _latitude;
    TextView _accuracy;

    Button _cameraCommand;
    Button _galleryCommand;
    ImageView _image;

    String _fileName;

    final static int _CAMERA_REQUEST = 100;
    final static int _CAMERA_FULL_REQUEST = 200;
    final static int _GALLERY_REQUEST = 300;

    Switch _accCommand;
    TextView _x;
    TextView _y;
    TextView _z;

    SensorManager _sensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);

        // GPS
        _gpsCommand = (Button) findViewById(R.id.GpsCommand);
        _longitude = (TextView) findViewById(R.id.Longitude);
        _latitude = (TextView) findViewById(R.id.Latitude);
        _accuracy = (TextView) findViewById(R.id.Accuracy);
        _gpsCommand.setOnClickListener(new OnGpsClicked());

        // Kemera
        _cameraCommand = (Button) findViewById(R.id.CameraCommand);
        _galleryCommand = (Button) findViewById(R.id.GalleryCommand);
        _image = (ImageView) findViewById(R.id.Image);
        _cameraCommand.setOnClickListener(new OnCameraClicked());
        _galleryCommand.setOnClickListener(new OnGelleryClicked());

        // Beschleunigungssensor
        _accCommand = (Switch) findViewById(R.id.AccCommand);
        _x = (TextView) findViewById(R.id.X);
        _y = (TextView) findViewById(R.id.Y);
        _z = (TextView) findViewById(R.id.Z);
        _sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        _accCommand.setOnCheckedChangeListener(new OnAccClicked());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sensor, menu);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == _CAMERA_REQUEST) {
            // Vorschaubild
            Bundle extras = data.getExtras();
            Bitmap image = (Bitmap) extras.get("data");

            _image.setImageBitmap(image);
        } else if (requestCode == _CAMERA_FULL_REQUEST) {
            Bitmap image = BitmapFactory.decodeFile(_fileName);
            _image.setImageBitmap(image);
        } else if (requestCode == _GALLERY_REQUEST) {
            Uri imagePath = data.getData();

            try {
                InputStream imageStream = getContentResolver().openInputStream(imagePath);
                Bitmap image = BitmapFactory.decodeStream(imageStream);

                imageStream.close();
                _image.setImageBitmap(image);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private class OnGpsClicked implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            LocationManager manager = (LocationManager) getSystemService(LOCATION_SERVICE);

            // Fall 1
            // Letzter bekannter Standort
            String gpsProvider = LocationManager.GPS_PROVIDER;
            String networkProvider = LocationManager.NETWORK_PROVIDER;

            Location gpsLocation = manager.getLastKnownLocation(gpsProvider);
            Location networkLocation = manager.getLastKnownLocation(networkProvider);

            //showLocation(gpsLocation);

            // Fall 2
            // Permanente Lokalisierung
            LocationListener listener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    showLocation(location);
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            };

            long zeitDelta = 1000l; // 1 Sekunde
            float entfernungDelta = 100; // 100 Meter

            manager.requestLocationUpdates(gpsProvider, // Provider
                    zeitDelta, // Zeit
                    entfernungDelta, // Entfernung
                    listener); // Listener
        }

        private void showLocation(Location location) {
            if (location == null) {
                return;
            }

            _longitude.setText(String.valueOf(location.getLongitude()));
            _latitude.setText(String.valueOf(location.getLatitude()));

            if (location.hasAccuracy()) {
                _accuracy.setText(String.valueOf(location.getAccuracy()));
            }
        }
    }


    private class OnCameraClicked implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            // Fall 1
            // Vorschaubild reicht
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            /*
            if(cameraIntent.resolveActivity(getPackageManager()) != null){
                startActivityForResult(cameraIntent, _CAMERA_REQUEST);
            }
            */

            // Fall 2
            // Volle Bildauflösung
            if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                File pictureDir = Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

                try {
                    File imageFile = File.createTempFile("temp_", ".jpg", pictureDir);
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));

                    _fileName = imageFile.getAbsolutePath();

                    startActivityForResult(cameraIntent, _CAMERA_FULL_REQUEST);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private class OnGelleryClicked implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent pickPicture = new Intent(Intent.ACTION_PICK);
            pickPicture.setType("image/*");

            if (pickPicture.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(pickPicture, _GALLERY_REQUEST);
            }
        }
    }


    private class OnAccClicked implements CompoundButton.OnCheckedChangeListener,
            SensorEventListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                // Sensoren abfragen
                _sensorManager.registerListener(this,
                        _sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                        _sensorManager.SENSOR_DELAY_NORMAL);
            } else {
                _sensorManager.unregisterListener(this);
            }
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                _x.setText(String.valueOf(x));
                _y.setText(String.valueOf(y));
                _z.setText(String.valueOf(z));

            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }


}
