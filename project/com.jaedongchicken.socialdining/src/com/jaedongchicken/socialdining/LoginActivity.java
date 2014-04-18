/**
 * Copyright 2010-present Facebook.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jaedongchicken.socialdining;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphPlace;
import com.facebook.model.GraphUser;
import com.facebook.widget.FriendPickerFragment;
import com.facebook.widget.LoginButton;
import com.facebook.widget.PickerFragment;
import com.facebook.widget.PlacePickerFragment;
import com.facebook.widget.ProfilePictureView;

public class LoginActivity extends Activity {

	private static final List<String> PERMISSIONS = Arrays
			.asList("publish_actions");
	private static final Location SEATTLE_LOCATION = new Location("") {
		{
			setLatitude(47.6097);
			setLongitude(-122.3331);
		}
	};

	private final String PENDING_ACTION_BUNDLE_KEY = "com.jaedongchicken.socialdining:PendingAction";

	private LoginButton loginButton;
	private ProfilePictureView profilePictureView;
	private TextView greeting;
	private TextView name;
	private TextView gender;
	private TextView age;
	private TextView email;
	private TextView fbId;
	private TextView birthday;

	private PendingAction pendingAction = PendingAction.NONE;
	private ViewGroup controlsContainer;
	private GraphUser user;

	private enum PendingAction {
		NONE, POST_PHOTO, POST_STATUS_UPDATE
	}

	private UiLifecycleHelper uiHelper;

	private Session.StatusCallback callback = new Session.StatusCallback() {
		@Override
		public void call(Session session, SessionState state,
				Exception exception) {
			onSessionStateChange(session, state, exception);
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		uiHelper = new UiLifecycleHelper(this, callback);
		uiHelper.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			String name = savedInstanceState
					.getString(PENDING_ACTION_BUNDLE_KEY);
			pendingAction = PendingAction.valueOf(name);
		}

		setContentView(R.layout.activity_login);

		loginButton = (LoginButton) findViewById(R.id.login_button);
		// loginButton.setReadPermissions(Arrays.asList("user_birthday"));
		loginButton.setReadPermissions(Arrays.asList("basic_info"));
		loginButton
				.setUserInfoChangedCallback(new LoginButton.UserInfoChangedCallback() {
					@Override
					public void onUserInfoFetched(GraphUser user) {
						LoginActivity.this.user = user;
						updateUI();
						// It's possible that we were waiting for this.user to
						// be populated in order to post a
						// status update.
						handlePendingAction();
					}
				});

		// begin 로그인 처리
		// facebook.isSessionValid() ;

		int flag = 0;

		for (int i = 0; i < 10; i++) {

			if (flag == 0) {
				isLoggedIn();
			} else if (flag == 1) {
				return;
			}
		}
		// end 로그인처리
	}

	@Override
	protected void onResume() {
		super.onResume();
		uiHelper.onResume();

		updateUI();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		uiHelper.onSaveInstanceState(outState);

		outState.putString(PENDING_ACTION_BUNDLE_KEY, pendingAction.name());
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		uiHelper.onActivityResult(requestCode, resultCode, data);

		// 이것안하면 로그인처리해도 안넘어간다!!???! fact는 안넘어간다.
		intent_main();

	}

	@Override
	public void onPause() {
		super.onPause();
		uiHelper.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		uiHelper.onDestroy();
	}

	/*
	 * // begin birthday private String buildUserInfoDisplay(GraphUser user) {
	 * StringBuilder userInfo = new StringBuilder("");
	 * userInfo.append(String.format("Birthday: %s\n\n", user.getBirthday()));
	 * 
	 * return userInfo.toString(); }
	 */

	public boolean isLoggedIn() {
		Session session = Session.getActiveSession();
		if (session != null && session.isOpened()) {
//			Toast toast = Toast.makeText(LoginActivity.this, "true",
//					Toast.LENGTH_SHORT);
//			toast.show();
			intent_main();
			return true;
		} else {
			// Toast toast = Toast.makeText(LoginActivity.this, "false",
			// Toast.LENGTH_SHORT);
			// toast.show();
			return false;
		}
	}

	public void intent_main() {
		Intent intent = new Intent(LoginActivity.this, MainActivity.class);
		startActivity(intent);
	}

	private void onSessionStateChange(Session session, SessionState state,
			Exception exception) {
		if (pendingAction != PendingAction.NONE
				&& (exception instanceof FacebookOperationCanceledException || exception instanceof FacebookAuthorizationException)) {
			new AlertDialog.Builder(LoginActivity.this)
					.setTitle(R.string.cancelled)
					.setMessage(R.string.permission_not_granted)
					.setPositiveButton(R.string.ok, null).show();
			pendingAction = PendingAction.NONE;
		} else if (state == SessionState.OPENED_TOKEN_UPDATED) {
			handlePendingAction();
		}

		updateUI();

		if (state.isOpened()) { // Request user data and show the results
			Request.executeMeRequestAsync(session,
					new Request.GraphUserCallback() {

						@Override
						public void onCompleted(GraphUser user,
								Response response) {
							if (user != null) { // Display the parsed user info
								// birthday.setText(buildUserInfoDisplay(user));
							}
						}
					});
		}

	}

	private void updateUI() {
		Session session = Session.getActiveSession();
		boolean enableButtons = (session != null && session.isOpened());

		// session.openForRead(loginButton);

		if (enableButtons && user != null) {

		}

		/*
		 * String name = user.asMap().get("name").toString();
		 * this.name.setText(name); String email =
		 * user.asMap().get("username").toString(); this.email.setText(email);
		 * String gender = user.asMap().get("gender").toString();
		 * this.gender.setText(gender); String fbId =
		 * user.asMap().get("id").toString(); this.fbId.setText(fbId);
		 */
		// String birthday = user.getBirthday();
		// this.birthday.setText(birthday);

		else {
			// profilePictureView.setProfileId(null);
			// greeting.setText(null);
		}

	}

	@SuppressWarnings("incomplete-switch")
	private void handlePendingAction() {
		PendingAction previouslyPendingAction = pendingAction;
		// These actions may re-set pendingAction if they are still pending, but
		// we assume they
		// will succeed.
		pendingAction = PendingAction.NONE;

	}

	/*
	 * @SuppressWarnings("deprecation") private AsyncFacebookRunner mAsyncRunner
	 * = new AsyncFacebookRunner( new Facebook(PENDING_ACTION_BUNDLE_KEY));
	 * 
	 * public void getProfileInformation() { mAsyncRunner.request("me", new
	 * RequestListener() {
	 * 
	 * @Override public void onComplete(String response, Object state) {
	 * Log.d("Profile", response); String json = response; try { JSONObject
	 * profile = new JSONObject(json); // getting name of the user String name =
	 * profile.getString("name"); // getting email of the user String email =
	 * profile.getString("email"); //getting user birthday String
	 * birth_day=profile.getString("birthday");
	 * 
	 * runOnUiThread(new Runnable() {
	 * 
	 * @Override public void run() { Toast.makeText(getApplicationContext(),
	 * "Name: " + birthday, Toast.LENGTH_LONG).show(); }
	 * 
	 * });
	 * 
	 * } catch (JSONException e) { e.printStackTrace(); } }
	 * 
	 * @Override public void onIOException(IOException e, Object state) { }
	 * 
	 * @Override public void onFileNotFoundException(FileNotFoundException e,
	 * Object state) { }
	 * 
	 * @Override public void onMalformedURLException(MalformedURLException e,
	 * Object state) { }
	 * 
	 * @Override public void onFacebookError(FacebookError e, Object state) { }
	 * });
	 * 
	 * }
	 */

	private interface GraphObjectWithId extends GraphObject {
		String getId();
	}

	private void showPublishResult(String message, GraphObject result,
			FacebookRequestError error) {
		String title = null;
		String alertMessage = null;
		if (error == null) {
			title = getString(R.string.success);
			String id = result.cast(GraphObjectWithId.class).getId();
			alertMessage = getString(R.string.successfully_posted_post,
					message, id);
		} else {
			title = getString(R.string.error);
			alertMessage = error.getErrorMessage();
		}

		new AlertDialog.Builder(this).setTitle(title).setMessage(alertMessage)
				.setPositiveButton(R.string.ok, null).show();
	}

	private void showPickerFragment(PickerFragment<?> fragment) {
		fragment.setOnErrorListener(new PickerFragment.OnErrorListener() {
			@Override
			public void onError(PickerFragment<?> pickerFragment,
					FacebookException error) {
				String text = getString(R.string.exception, error.getMessage());
				Toast toast = Toast.makeText(LoginActivity.this, text,
						Toast.LENGTH_SHORT);
				toast.show();
			}
		});

		// FragmentManager fm = getSupportFragmentManager();
		// fm.beginTransaction().replace(R.id.fragment_container, fragment)
		// .addToBackStack(null).commit();

		controlsContainer.setVisibility(View.GONE);

		// We want the fragment fully created so we can use it immediately.
		// fm.executePendingTransactions();

		// fragment.loadData(false);
	}

	private void showAlert(String title, String message) {
		new AlertDialog.Builder(this).setTitle(title).setMessage(message)
				.setPositiveButton(R.string.ok, null).show();
	}

	private boolean hasPublishPermission() {
		Session session = Session.getActiveSession();
		return session != null
				&& session.getPermissions().contains("publish_actions");
	}

	private void performPublish(PendingAction action) {
		Session session = Session.getActiveSession();
		if (session != null) {
			pendingAction = action;
			if (hasPublishPermission()) {
				// We can do the action right away.
				handlePendingAction();
			} else {
				// We need to get new permissions, then complete the action when
				// we get called back.
				session.requestNewPublishPermissions(new Session.NewPermissionsRequest(
						this, PERMISSIONS));
			}
		}
	}
}
