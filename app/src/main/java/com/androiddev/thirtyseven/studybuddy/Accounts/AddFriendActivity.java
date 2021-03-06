package com.androiddev.thirtyseven.studybuddy.Accounts;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.androiddev.thirtyseven.studybuddy.Backend.ServerConnection;
import com.androiddev.thirtyseven.studybuddy.Main.HubActivity;
import com.androiddev.thirtyseven.studybuddy.Main.NavBase;
import com.androiddev.thirtyseven.studybuddy.R;

import org.json.JSONObject;

public class AddFriendActivity extends NavBase {

    private JSONObject j;
    private JSONObject j2;
    private String id;
    private String id2;
    private SharedPreferences prefs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        id = prefs.getString("id", "None");
        createButton();




    }
    protected void createButton(){
        Button button = (Button) findViewById(R.id.buddyButton);
        button.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                EditText usernameField = (EditText) (findViewById(R.id.editText));

                //username in email form
                String friendName = usernameField.getText().toString();

                //first async task to get the id
                final String my_params = "/users/username/" + friendName;
                AsyncTask a = new AsyncTask<Object, Void, JSONObject>() {


                    @Override
                    protected JSONObject doInBackground(Object... params) {

                        ServerConnection s = new ServerConnection(my_params);
                        j = s.run();
                        try {
                            j2 = j.getJSONObject("user");
                        } catch (Exception e) {

                        }
                        return j2;
                    }

                };
                try {
                    j2 = (JSONObject) a.execute().get();
                } catch (Exception e) {

                }
                try {
                    id2 = j2.getString("_id");
                } catch (Exception e) {

                }


                //Don't run the second async task if the person didn't exist!
                if (j2 == null) {
                    Toast.makeText(getApplicationContext(), "That user does not exists!", Toast.LENGTH_LONG).show();
                } else {

                    //make them your friend
                    final String my_params2 = "/users/updatefriends/" + id + "/" + id2;

                    AsyncTask a2 = new AsyncTask<Object, Void, Void>() {


                        @Override
                        protected Void doInBackground(Object... params) {

                            ServerConnection s = new ServerConnection(null, "PUT", my_params2);
                            j2 = s.run();
                            return null;

                        }

                    };
                    a2.execute();
                    Intent i = new Intent(getApplicationContext(), BuddyList.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);

                }
            }
        });
    }
    @Override
    public void onBackPressed(){
        Intent main = new Intent(getApplicationContext(), HubActivity.class);
        main.addCategory(Intent.CATEGORY_HOME);
        main.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(main);
        finish();


    }

}
