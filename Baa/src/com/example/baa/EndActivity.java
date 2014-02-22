package com.example.baa;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;

public class EndActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		int winner = getIntent().getExtras().getInt("winner");
		if (winner == 1) {
			setContentView(R.layout.end_game);
		} else {
			setContentView(R.layout.end_game1);
		}
		
	}
	
	public void start_multiplayer(View view) {
	    Intent intent_multiplayer = new Intent(this, Multiplayer.class);
	    startActivity(intent_multiplayer);
	}
	
	public void start_singleplayer(View view) {
		  
	}
	
}
