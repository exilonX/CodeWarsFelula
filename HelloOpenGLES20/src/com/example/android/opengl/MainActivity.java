package com.example.android.opengl;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}
	
	public void start_multiplayer(View view) {
	    Intent intent_multiplayer = new Intent(this, Multiplayer.class);
	    startActivity(intent_multiplayer);
	}

	
	
	
	
}
