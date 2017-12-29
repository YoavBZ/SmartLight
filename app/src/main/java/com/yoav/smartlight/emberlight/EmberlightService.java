package com.yoav.smartlight.emberlight;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;
import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.TokenResponse;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Yoav on 7/30/2017.
 */

public class EmberlightService {

	private Context context;
	static final String HOST = "https://api.emberlight.co";
	private static final String socketId = "SOCKET_IT";
	private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
	private OkHttpClient client;
	private AuthHandler authHandler;
	private AuthorizationService authorizationService;

	public EmberlightService(Context context) {
		this.context = context;
		authHandler = new AuthHandler(context);
		authorizationService = authHandler.getAuthorizationService();
		client = new OkHttpClient();
	}

	void getInfo() {
		AuthState authState = authHandler.getAuthState();
		if (authState != null) {
			authState.performActionWithFreshTokens(authorizationService, new AuthState.AuthStateAction() {
				@Override
				public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException exception) {
					new AsyncLightTask() {
						@Override
						protected JSONObject doInBackground(String... tokens) {
							Request request = new Request.Builder().url(HOST + "/third-party/v1/user/devices")
									.addHeader("Authorization", String.format("Bearer %s", tokens[0]))
									.build();
							Log.d("getInfo", String.format("Request: %s", request.toString()));
							try {
								Response response = client.newCall(request).execute();
								String jsonBody = response.body().string();
								Log.i("getInfo", String.format("User Info Response %s", jsonBody));
								return new JSONObject(jsonBody);
							} catch (Exception e) {
								Log.w("getInfo", e);
							}
							return null;
						}
					}.execute(accessToken);
				}
			});
		} else {
			Toast.makeText(context, "Authorization required for this action", Toast.LENGTH_SHORT).show();
		}
	}

	public void turnLightOn() {
		AuthState authState = authHandler.getAuthState();
		if (authState != null) {
			authState.performActionWithFreshTokens(authorizationService, new AuthState.AuthStateAction() {
				@Override
				public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException exception) {
					new AsyncLightTask() {
						@Override
						protected JSONObject doInBackground(String... tokens) {
							Request request = new Request.Builder().url(HOST + "/third-party/v1/actions/turn-on-lights")
									.addHeader("Authorization", String.format("Bearer %s", tokens[0]))
									.post(buildBody())
									.build();
							Log.d("turnOn", String.format("Request: %s", request.toString()));
							try {
								Response response = client.newCall(request).execute();
								String jsonBody = response.body().string();
								Log.i("turnOn", String.format("Response %s", jsonBody));
								return new JSONObject(jsonBody);
							} catch (Exception e) {
								Log.w("turnOn", e);
							}
							return null;
						}
					}.execute(accessToken);
				}
			});
		} else {
			Toast.makeText(context, "Authorization required for this action", Toast.LENGTH_SHORT).show();
		}
	}

	public void turnLightOff() {
		AuthState authState = authHandler.getAuthState();
		if (authState != null) {
			authState.performActionWithFreshTokens(authorizationService, new AuthState.AuthStateAction() {
				@Override
				public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException exception) {
					new AsyncLightTask() {
						@Override
						protected JSONObject doInBackground(String... tokens) {
							Request request = new Request.Builder().url(HOST + "/third-party/v1/actions/turn-off-lights")
									.addHeader("Authorization", String.format("Bearer %s", tokens[0]))
									.post(buildBody())
									.build();
							Log.d("turnOff", String.format("Request: %s", request.toString()));
							try {
								Response response = client.newCall(request).execute();
								String jsonBody = response.body().string();
								Log.i("turnOff", String.format("Response %s", jsonBody));
								return new JSONObject(jsonBody);
							} catch (Exception e) {
								Log.w("turnOff", e);
							}
							return null;
						}
					}.execute(accessToken);
				}
			});
		} else {
			Toast.makeText(context, "Authorization required for this action", Toast.LENGTH_SHORT).show();
		}

	}

	void toggleLight() {
		AuthState authState = authHandler.getAuthState();
		if (authState != null) {
			authState.performActionWithFreshTokens(authorizationService, new AuthState.AuthStateAction() {
				@Override
				public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException exception) {
					new AsyncLightTask() {
						@Override
						protected JSONObject doInBackground(String... tokens) {
							Request request = new Request.Builder().url(HOST + "/third-party/v1/actions/toggle-lights")
									.addHeader("Authorization", String.format("Bearer %s", tokens[0]))
									.post(buildBody())
									.build();
							Log.d("toggleLight", String.format("Request: %s", request.toString()));
							try {
								Response response = client.newCall(request).execute();
								String jsonBody = response.body().string();
								Log.i("toggleLight", String.format("Response %s", jsonBody));
								return new JSONObject(jsonBody);
							} catch (Exception e) {
								Log.w("toggleLight", e);
							}
							return null;
						}
					}.execute(accessToken);
				}
			});
		} else {
			Toast.makeText(context, "Authorization required for this action", Toast.LENGTH_SHORT).show();
		}
	}

	public void dimLights(final int dimLevel) {
		AuthState authState = authHandler.getAuthState();
		if (authState != null) {
			authState.performActionWithFreshTokens(authorizationService, new AuthState.AuthStateAction() {
				@Override
				public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException exception) {
					new AsyncLightTask() {
						@Override
						protected JSONObject doInBackground(String... tokens) {
							Request request = new Request.Builder().url(HOST + "/third-party/v1/actions/dim-lights")
									.addHeader("Authorization", String.format("Bearer %s", tokens[0]))
									.post(buildBody(dimLevel))
									.build();
							Log.d("dimLight", String.format("Request: %s", request.toString()));
							try {
								Response response = client.newCall(request).execute();
								String jsonBody = response.body().string();
								Log.i("dimLight", String.format("Response %s", jsonBody));
								return new JSONObject(jsonBody);
							} catch (Exception e) {
								Log.w("dimLight", e);
							}
							return null;
						}
					}.execute(accessToken);
				}
			});
		} else {
			Toast.makeText(context, "Authorization required for this action", Toast.LENGTH_SHORT).show();
		}
	}

	public void authorize() {
		authHandler.authorize();
	}

	public void signOut() {
		authHandler.signOut();
	}

	String getAccessToken() {
		AuthState authState = authHandler.getAuthState();
		return authState != null ? authState.getAccessToken() : null;
	}

	public void close() {
		authorizationService.dispose();
	}

	private RequestBody buildBody() {
		JSONObject jsonObject = new JSONObject();
		JSONObject actionFields = new JSONObject();
		try {
			actionFields.put("device", socketId);
			jsonObject.put("actionFields", actionFields);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		Log.d("buildBody", jsonObject.toString());
		return RequestBody.create(JSON, jsonObject.toString());
	}

	private RequestBody buildBody(int dim) {
		JSONObject jsonObject = new JSONObject();
		JSONObject actionFields = new JSONObject();
		try {
			actionFields.put("device", socketId);
			actionFields.put("dim", dim);
			jsonObject.put("actionFields", actionFields);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		Log.d("buildBody", jsonObject.toString());
		return RequestBody.create(JSON, jsonObject.toString());
	}

	/**
	 * Exchanges the code, for the {@link TokenResponse}.
	 *
	 * @param intent represents the {@link Intent} from the Custom Tabs or the System Browser.
	 */
	public void handleAuthorizationResponse(@NonNull Intent intent) {
		authHandler.handleAuthorizationResponse(intent);
	}

	public boolean isAuthorized() {
		return authHandler.isAuthorized();
	}

	private abstract class AsyncLightTask extends AsyncTask<String, Void, JSONObject> {
		protected void onPostExecute(JSONObject devices) {
			if (devices != null) {
				JSONArray data = devices.optJSONArray("data");

				String message;
				if (devices.has("error")) {
					message = String.format("Request failed [%s]", devices.optString("error_description", "No " + "description"));
				} else {
					message = String.format("Request succeeded [%s]", data.toString());
				}
				Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
			}
		}
	}
}
