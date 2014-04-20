package com.jaedongchicken.socialdining;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.listeners.OnLogoutListener;

public class MainActivity extends Activity {

	// begin declare 
	private Button mButtonLogout;
	private SimpleFacebook mSimpleFacebook;
	
	
	// Logout listener
	private OnLogoutListener mOnLogoutListener = new OnLogoutListener() {

		@Override
		public void onFail(String reason) {
		}

		@Override
		public void onException(Throwable throwable) {
		}

		@Override
		public void onThinking() {
			// show progress bar or something to the user while login is
			// happening
		}

		@Override
		public void onLogout() {
			// change the state of the button or do whatever you want
			setUIState();
		}

	};
	@Override
	protected void onResume() {
		super.onResume();
		mSimpleFacebook = SimpleFacebook.getInstance(this);
		setUIState();
	}
	
	private void setUIState() {
		if (mSimpleFacebook.isLogin()) {
			
			
		} else {
			//loggedOutUIState();
			Intent intent = new Intent();
			intent.setClass(getApplicationContext(), LoginActivity.class);
			// intent.putExtra("EXTRA_ID", "SOME DATAS");
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initUI();


		// 2. Logout example
		logoutExample();
	}

	

	private void initUI() {
		mButtonLogout = (Button) findViewById(R.id.button_logout);
	}
	
	/**
	 * Logout example
	 */
	private void logoutExample() {
		mButtonLogout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				mSimpleFacebook.logout(mOnLogoutListener);
			}
		});
	}
}
