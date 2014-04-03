package net.henryhu.cellremote;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;

public class MainActivity extends Activity {
    EditText addrBox = null;
    TextView statusBox;
    Button testBtn;
    String lastAction = null;
    int actionCount = 0;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        addrBox = (EditText)findViewById(R.id.addr);
        testBtn = (Button)findViewById(R.id.test);
        statusBox = (TextView)findViewById(R.id.status);

        testBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                test();
            }
        });
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
/*            long now = System.currentTimeMillis();
            if (last != 0 && (now - last < 1000)) return true;
            last = now;*/
//            Toast t = Toast.makeText(this, "Down", 1000);
//            t.show();
            moveDown();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
/*            long now = System.currentTimeMillis();
            if (last != 0 && (now - last < 1000)) return true;
            last = now;*/
//            Toast t = Toast.makeText(this, "Up", 1000);
//            t.show();
            moveUp();
            return true;
        }
        return false;
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            return true;
        }
        return false;
    }

    void showInfo(String s) {
        Toast t = Toast.makeText(this, s, 5000);
        t.show();
    }

    void setStatus(String status) {
        statusBox.setText(status);
    }

    class RequestTask extends AsyncTask<String, String, String> {
        String uri = "";
        String action;
        Exception exception = null;

        @Override
        protected String doInBackground(String... actions) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response;
            String responseString = null;
            action = actions[0];
            try {
                uri = "http://" + addrBox.getText().toString() + ":5001/?action=" + action;
                response = httpclient.execute(new HttpGet(uri));
                StatusLine statusLine = response.getStatusLine();
                if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    out.close();
                    responseString = out.toString();
                } else{
                    response.getEntity().getContent().close();
                    throw new IOException(statusLine.getReasonPhrase());
                }
            } catch (ClientProtocolException e) {
                Log.v(e.toString(), action);
                exception = e;
            } catch (IOException e) {
                Log.v(e.toString(), action);
                exception = e;
            }
            return responseString;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (action.equals("test")) {
                if (result != null && result.equals("OK")) {
                    setStatus("Test OK");
                } else {
                    if (exception != null) {
                        setStatus("Test fail: " + exception.getLocalizedMessage());
                    } else {
                        if (result != null) {
                            setStatus("Test fail: " + result);
                        } else {
                            setStatus("Test fail: unknown reason");
                        }
                    }
                }
            } else {
                if (result != null && result.equals("OK")) {
                    if (lastAction != null && action.equals(lastAction)) {
                        actionCount ++;
                        setStatus("Sent '" + action + "' +" + (actionCount - 1));
                    } else {
                        lastAction = action;
                        actionCount = 1;
                        setStatus("Sent '" + action + "'");
                    }
                } else {
                    if (exception != null) {
                        setStatus("Action fail: " + exception.getLocalizedMessage());
                    } else {
                        if (result != null) {
                            setStatus("Action fail: " + result);
                        } else {
                            setStatus("Action fail: unknown reason");
                        }
                    }
                }
            }
        }
    }

    void request(String action) {
        new RequestTask().execute(action);
    }

    void moveDown() {
        setStatus("Sending 'down'...");
        request("down");
    }

    void moveUp() {
        setStatus("Sending 'up'...");
        request("up");
    }

    void test() {
        setStatus("Testing...");
        request("test");
    }
}
