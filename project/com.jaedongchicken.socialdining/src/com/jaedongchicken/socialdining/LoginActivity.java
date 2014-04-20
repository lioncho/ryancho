package com.jaedongchicken.socialdining;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.sromku.simple.fb.Permission.Type;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.listeners.OnLoginListener;

public class LoginActivity extends Activity {

	protected static final String TAG = MainActivity.class.getName();
	private SimpleFacebook mSimpleFacebook;
	private Button mButtonLogin;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		initUI();
		loginExample();
		
	}

	private void initUI() {
		mButtonLogin = (Button) findViewById(R.id.button_login);
		
	}

	/**
	 * Login example.
	 */
	private void loginExample() {
		mButtonLogin.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				mSimpleFacebook.login(mOnLoginListener);
			}
		});
	}
	

	// Login listener
	private OnLoginListener mOnLoginListener = new OnLoginListener() {

		@Override
		public void onFail(String reason) {
			Log.w(TAG, "Failed to login");
		}

		@Override
		public void onException(Throwable throwable) {
			Log.e(TAG, "Bad thing happened", throwable);
		}

		@Override
		public void onThinking() {
			// show progress bar or something to the user while login is
			// happening
		}

		@Override
		public void onLogin() {
			// change the state of the button or do whatever you want
		}

		@Override
		public void onNotAcceptingPermissions(Type type) {
			// TODO Auto-generated method stub
		}

	};


	


	@Override
	public void onResume() {
		super.onResume();
		mSimpleFacebook = SimpleFacebook.getInstance(this);
		setUIState();
	}

	//로그인 시도했다가 실패했을때 처리해주는곳
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		mSimpleFacebook.onActivityResult(this, requestCode, resultCode, data);
	}

	private void setUIState() {
		if (mSimpleFacebook.isLogin()) {
			//loggedInUIState();
			Intent intent = new Intent();
			intent.setClass(getApplicationContext(), MainActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			
		} else {
		
		}
	}
}
