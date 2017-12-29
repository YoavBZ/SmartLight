package com.yoav.smartlight.emberlight;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import com.yoav.smartlight.MainActivity;
import net.openid.appauth.*;
import org.json.JSONException;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Yoav on 9/9/2017.
 */

class AuthHandler {

	private static final String CLIENT_ID = "CLIENT_ID";
	private static final String CLIENT_SECRET = "CLIENT_SECRET";
	private static final String AUTHORIZE = "/third-party/oauth/authorize";
	private static final String REQUEST_TOKEN = "/third-party/oauth/token";
	private static final String SHARED_PREFERENCES_NAME = "AuthStatePreference";
	private static final String AUTH_STATE = "AUTH_STATE";

	private Context context;
	private AuthState authState;
	private AuthorizationService authorizationService;

	AuthHandler(Context context) {
		this.context = context;
		this.authState = readAuthState();
		authorizationService = new AuthorizationService(context);
	}

	void authorize() {
		AuthorizationServiceConfiguration serviceConfig = new AuthorizationServiceConfiguration(Uri.parse(EmberlightService.HOST + AuthHandler.AUTHORIZE), Uri
				.parse(EmberlightService.HOST + AuthHandler.REQUEST_TOKEN));
		AuthorizationRequest request = new AuthorizationRequest.Builder(serviceConfig, AuthHandler.CLIENT_ID, ResponseTypeValues.CODE, Uri
				.parse("com.yoav.smartlight:/oauth2callback")).build();
		String action = "com.yoav.smartlight.HANDLE_AUTHORIZATION_RESPONSE";
		Intent postAuthorizationIntent = new Intent(context, MainActivity.class);
		postAuthorizationIntent.setAction(action);
		postAuthorizationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, request.hashCode(), postAuthorizationIntent, 0);
		authorizationService.performAuthorizationRequest(request, pendingIntent);
	}

	AuthState getAuthState() {
		authState = readAuthState();
		return authState;
	}

	AuthorizationService getAuthorizationService() {
		return authorizationService;
	}

	private void persistAuthState(@NonNull AuthState authState) {
		context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
				.edit()
				.putString(AUTH_STATE, authState.jsonSerializeString())
				.apply();
	}

	private AuthState readAuthState() {
		SharedPreferences authPrefs = context.getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
		String stateJson = authPrefs.getString(AUTH_STATE, null);
		if (stateJson != null) {
			try {
				return AuthState.jsonDeserialize(stateJson);
			} catch (JSONException e) {
				Log.e("getAuthState", e.getMessage());
				return new AuthState();
			}
		} else {
			return null;
		}
	}

	/**
	 * Exchanges the code, for the {@link TokenResponse}.
	 *
	 * @param intent represents the {@link Intent} from the Custom Tabs or the System Browser.
	 */
	void handleAuthorizationResponse(@NonNull Intent intent) {
		AuthorizationResponse response = AuthorizationResponse.fromIntent(intent);
		AuthorizationException error = AuthorizationException.fromIntent(intent);
		authState = new AuthState(response, error);

		if (response != null) {
			Log.i("handleAuthResponse", String.format("Handled Authorization Response %s ", authState.jsonSerializeString()));
			AuthorizationService service = getAuthorizationService();
			service.performTokenRequest(response.createTokenExchangeRequest(), new ClientSecretPost(AuthHandler.CLIENT_SECRET), new AuthorizationService.TokenResponseCallback() {
				@Override
				public void onTokenRequestCompleted(@Nullable TokenResponse tokenResponse, @Nullable AuthorizationException exception) {
					if (exception != null) {
						Log.w("handleAuthResponse", "Token Exchange failed", exception);
					} else {
						if (tokenResponse != null) {
							authState.update(tokenResponse, null);
							persistAuthState(authState);
							Log.i("handleAuthResponse", String.format("Token Response [ Access Token: %s, ID Token: %s ]", tokenResponse.accessToken, tokenResponse.idToken));
						}
					}
				}
			});
		}
	}

	void signOut() {
		authState = null;
		clearAuthState();
	}

	private void clearAuthState() {
		context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).edit().remove(AUTH_STATE).apply();
	}

	boolean isAuthorized() {
		return authState != null && authState.isAuthorized();
	}
}
