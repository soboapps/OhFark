package com.soboapps.ohfark;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

public class GameOver extends Activity{

	private Button quitButton;
	private Button playButton;

	private OhFarkActivity UI;

	public void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.game_over);

		quitButton = (Button) findViewById(R.id.goQuitButton);
		playButton = (Button) findViewById(R.id.goPlayButton);

		Button btnQuit = (Button) findViewById(R.id.goQuitButton);
		btnQuit.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				quitGame();
				finish();
			}
		});

		Button btnPlay = (Button) findViewById(R.id.goPlayButton);
		btnPlay.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				reload();
				finish();
			}
		});


	}

	public GameOver() {

	}

	public void reload() {
		Intent intent = new Intent("finish_activity");
		sendBroadcast(intent);
		startActivity(new Intent(this.getApplicationContext(), OhFarkActivity.class));
	}

	public void quitGame() {
		Intent intent = new Intent("finish_activity");
		sendBroadcast(intent);
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// Kills & force closes the app
			android.os.Process.killProcess(android.os.Process.myPid());
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		//if(shaker != null)
		//  shaker.close();
	}

}
