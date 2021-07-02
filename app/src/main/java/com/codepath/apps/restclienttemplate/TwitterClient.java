package com.codepath.apps.restclienttemplate;

import android.content.Context;

import com.codepath.asynchttpclient.RequestParams;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.codepath.oauth.OAuthBaseClient;
import com.github.scribejava.apis.TwitterApi;
import com.github.scribejava.core.builder.api.BaseApi;

/*
 * 
 * This is the object responsible for communicating with a REST API. 
 * Specify the constants below to change the API being communicated with.
 * See a full list of supported API classes: 
 *   https://github.com/scribejava/scribejava/tree/master/scribejava-apis/src/main/java/com/github/scribejava/apis
 * Key and Secret are provided by the developer site for the given API i.e dev.twitter.com
 * Add methods for each relevant endpoint in the API.
 * 
 * NOTE: You may want to rename this object based on the service i.e TwitterClient or FlickrClient
 * 
 */

// This object is responsible for communicating with the RESTful API
public class TwitterClient extends OAuthBaseClient {

	// Constants
	public static final BaseApi REST_API_INSTANCE = TwitterApi.instance();
	public static final String REST_URL = "https://api.twitter.com/1.1";
	public static final String REST_CONSUMER_KEY = BuildConfig.CONSUMER_KEY;
	public static final String REST_CONSUMER_SECRET = BuildConfig.CONSUMER_SECRET;
	// Landing page to indicate the OAuth flow worked in case Chrome for Android 25+ blocks navigation back to the app.
	public static final String FALLBACK_URL = "https://codepath.github.io/android-rest-client-template/success.html";
	// See https://developer.chrome.com/multidevice/android/intents
	public static final String REST_CALLBACK_URL_TEMPLATE = "intent://%s#Intent;action=android.intent.action.VIEW;scheme=%s;package=%s;S.browser_fallback_url=%s;end";

	public TwitterClient(Context context) {
		super(context, REST_API_INSTANCE,
				REST_URL,
				REST_CONSUMER_KEY,
				REST_CONSUMER_SECRET,
				null,  // OAuth2 scope, null for OAuth1
				String.format(REST_CALLBACK_URL_TEMPLATE, context.getString(R.string.intent_host),
						context.getString(R.string.intent_scheme), context.getPackageName(), FALLBACK_URL));
		// this.max_id = max_id;
	}

	// Gets current user's profile
	public void getProfile(JsonHttpResponseHandler handler) {
		String apiUrl = getApiUrl("account/verify_credentials.json");
		RequestParams params = new RequestParams();
		client.get(apiUrl, params, handler);
	}

	// Gets user's timeline
	public void getHomeTimeline(JsonHttpResponseHandler handler) {
		String apiUrl = getApiUrl("statuses/home_timeline.json");
		RequestParams params = new RequestParams();
		params.put("count", 25);
		params.put("since_id", 1);
		params.put("tweet_mode", "extended");
		client.get(apiUrl, params, handler);
	}

	public void getRefreshedTimeline(long maxId, JsonHttpResponseHandler handler) {
		String apiUrl = getApiUrl("statuses/home_timeline.json");
		RequestParams params = new RequestParams();
		params.put("count", 25);
		params.put("max_id", maxId);
		params.put("tweet_mode", "extended");
		client.get(apiUrl, params, handler);
	}

	// Get user's Tweets
	public void getUserTweets(String screenName, JsonHttpResponseHandler handler) {
		String apiUrl = getApiUrl("statuses/user_timeline.json");
		RequestParams params = new RequestParams();
		params.put("screen_name", screenName);
		client.get(apiUrl, params, handler);
	}

	// Gets user's following list
	public void getUserFollowing(String tweetId, JsonHttpResponseHandler handler) {
		String apiUrl = getApiUrl("friends/list.json");
		RequestParams params = new RequestParams();
		params.put("id", tweetId);
		client.get(apiUrl, params, handler);
	}

	// Gets user's followers list
	public void getUserFollowers(String tweetId, JsonHttpResponseHandler handler) {
		String apiUrl = getApiUrl("followers/list.json");
		RequestParams params = new RequestParams();
		params.put("id", tweetId);
		client.get(apiUrl, params, handler);
	}

	// Publishes Tweet
	public void publishTweet(String tweetContent, JsonHttpResponseHandler handler) {
		String apiUrl = getApiUrl("statuses/update.json");
		RequestParams params = new RequestParams();
		params.put("status", tweetContent);
		client.post(apiUrl, params, "", handler);
	}

	// Likes Tweet
	public void likeTweet(String tweetId, JsonHttpResponseHandler handler) {
		String apiUrl = getApiUrl("favorites/create.json");
		RequestParams params = new RequestParams();
		params.put("id", tweetId);
		client.post(apiUrl, params, "", handler);
	}

	// Unlikes Tweets
	public void unlikeTweet(String tweetId, JsonHttpResponseHandler handler) {
		String apiUrl = getApiUrl("favorites/destroy.json");
		RequestParams params = new RequestParams();
		params.put("id", tweetId);
		client.post(apiUrl, params, "", handler);
	}

	// Retweets a Tweet
	public void retweet(String tweetId, JsonHttpResponseHandler handler) {
		String apiUrl = getApiUrl("statuses/retweet/" + tweetId + ".json");
		RequestParams params = new RequestParams();
		params.put("id", tweetId);
		client.post(apiUrl, params, "", handler);
	}

	// Unretweets a Tweet
	public void unretweet(String tweetId, JsonHttpResponseHandler handler) {
		String apiUrl = getApiUrl("statuses/unretweet/" + tweetId + ".json");
		RequestParams params = new RequestParams();
		params.put("id", tweetId);
		client.post(apiUrl, params, "", handler);
	}
}
