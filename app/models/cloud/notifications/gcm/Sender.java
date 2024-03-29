/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package models.cloud.notifications.gcm;

import com.fasterxml.jackson.databind.JsonNode;
import models.cloud.notifications.gcm.exceptions.InvalidRequestException;
import org.apache.http.protocol.HTTP;
import play.libs.Json;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Helper class to send messages to the GCM service using an API Key.
 */
public class Sender {

	/**
	 * Initial delay before first retry, without jitter.
	 */
	protected static final int BACKOFF_INITIAL_DELAY = 1000;
	/**
	 * Maximum delay before a retry.
	 */
	protected static final int MAX_BACKOFF_DELAY = 1024000;

	protected final Random random = new Random();
	protected final Logger logger = Logger.getLogger(getClass().getName());

	private final String key;

	/**
	 * Default constructor.
	 * 
	 * @param key
	 *            API key obtained through the Google API Console.
	 */
	public Sender(String key) {
		this.key = nonNull(key);
	}

	/**
	 * Sends a message to many devices, retrying in case of unavailability.
	 * 
	 * <p>
	 * <strong>Note: </strong> this method uses exponential back-off to retry in
	 * case of service unavailability and hence could block the calling thread
	 * for many seconds.
	 * 
	 * @param message
	 *            message to be sent.
	 * @param regIds registration id of the devices that will receive the message.
	 * @param retries number of retries in case of service unavailability errors.
	 * 
	 * @return combined result of all requests made.
	 * 
	 * @throws IllegalArgumentException if registrationIds is {@literal null} or empty.
	 * @throws models.cloud.notifications.gcm.exceptions.InvalidRequestException if GCM didn't returned a 200 or 503 status.
	 * @throws java.io.IOException if message could not be sent.
	 */
	public MulticastResult send(Message message, List<String> regIds, int retries) throws IOException {
		int attempt = 0;
		MulticastResult multicastResult;
		int backoff = BACKOFF_INITIAL_DELAY;
		// Map of results by registration id, it will be updated after each
		// attempt
		// to send the messages
		Map<String, Result> results = new HashMap<>();
		List<String> unsentRegIds = new ArrayList<>(regIds);
		boolean tryAgain;
		List<Long> multicastIds = new ArrayList<>();
		do {
			attempt++;
			
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("Attempt #" + attempt + " to send message " + message + " to regIds " + unsentRegIds);
			}
			
			multicastResult = sendNoRetry(message, unsentRegIds);
			long multicastId = multicastResult.getMulticastId();
			logger.fine("multicast_id on attempt # " + attempt + ": " + multicastId);
			multicastIds.add(multicastId);
			unsentRegIds = updateStatus(unsentRegIds, results, multicastResult);
			tryAgain = !unsentRegIds.isEmpty() && attempt <= retries;
			if (tryAgain) {
				int sleepTime = backoff / 2 + random.nextInt(backoff);
				sleep(sleepTime);
				if (2 * backoff < MAX_BACKOFF_DELAY) {
					backoff *= 2;
				}
			}
		} while (tryAgain);
		// calculate summary
		int success = 0, failure = 0, canonicalIds = 0;
		for (Result result : results.values()) {
			if (result.getMessageId() != null) {
				success++;
				if (result.getCanonicalRegistrationId() != null) {
					canonicalIds++;
				}
			} else {
				failure++;
			}
		}
		// build a new object with the overall result
		long multicastId = multicastIds.remove(0);
		MulticastResult.Builder builder = new MulticastResult.Builder(success, failure, canonicalIds, multicastId)
												.retryMulticastIds(multicastIds);

		// add results, in the same order as the input
		for (String regId : regIds) {
			Result result = results.get(regId);
			builder.addResult(result);
		}
		return builder.build();
	}

	/**
	 * Updates the status of the messages sent to devices and the list of
	 * devices that should be retried.
	 * 
	 * @param unsentRegIds list of devices that are still pending an update.
	 * @param allResults map of status that will be updated.
	 * @param multicastResult result of the last multicast sent.
	 * 
	 * @return updated version of devices that should be retried.
	 */
	private List<String> updateStatus(List<String> unsentRegIds, Map<String, Result> allResults, MulticastResult multicastResult) {
		List<Result> results = multicastResult.getResults();
		if (results.size() != unsentRegIds.size()) {
			// should never happen, unless there is a flaw in the algorithm
			throw new RuntimeException("Internal error: sizes do not match. "
					+ "currentResults: " + results + "; unsentRegIds: "
					+ unsentRegIds);
		}
		List<String> newUnsentRegIds = new ArrayList<>();
		for (int i = 0; i < unsentRegIds.size(); i++) {
			String regId = unsentRegIds.get(i);
			Result result = results.get(i);
			allResults.put(regId, result);
			String error = result.getErrorCodeName();
			if (error != null && error.equals(Constants.ERROR_UNAVAILABLE)) {
				newUnsentRegIds.add(regId);
			}
		}
		return newUnsentRegIds;
	}

	/**
	 * Sends a message without retrying in case of service unavailability.
	 * 
	 * @return {@literal true} if the message was sent successfully,
	 *         {@literal false} if it failed but could be retried.
	 * 
	 * @throws IllegalArgumentException
	 *             if registrationIds is {@literal null} or empty.
	 * @throws models.cloud.notifications.gcm.exceptions.InvalidRequestException
	 *             if GCM didn't returned a 200 status.
	 * @throws java.io.IOException
	 *             if message could not be sent or received.
	 */
	public MulticastResult sendNoRetry(Message message, List<String> registrationIds) throws IOException {
		if (nonNull(registrationIds).isEmpty()) {
			throw new IllegalArgumentException(
					"registrationIds cannot be empty");
		}
		
		Map<Object, Object> jsonRequest = new HashMap<>();
		setJsonField(jsonRequest, Constants.PARAM_TIME_TO_LIVE, message.getTimeToLive());
		setJsonField(jsonRequest, Constants.PARAM_COLLAPSE_KEY, message.getCollapseKey());
		setJsonField(jsonRequest, Constants.PARAM_DELAY_WHILE_IDLE,
				message.isDelayWhileIdle());
		jsonRequest.put(Constants.JSON_REGISTRATION_IDS, registrationIds);
		Map<String, String> payload = message.getData();
		if (!payload.isEmpty()) {
			jsonRequest.put(Constants.JSON_PAYLOAD, payload);
		}
		String requestBody = Json.toJson(jsonRequest).toString();
		logger.finest("JSON request: " + requestBody);
		HttpURLConnection conn = post(Constants.GCM_SEND_ENDPOINT, "application/json;charset=utf-8;",
				requestBody);
		int status = conn.getResponseCode();
		String responseBody;
		if (status != 200) {
			responseBody = getString(conn.getErrorStream());
			logger.finest("JSON error response: " + responseBody);
			throw new InvalidRequestException(status, responseBody);
		}
		responseBody = getString(conn.getInputStream());
		logger.finest("JSON response: " + responseBody);
		JsonNode jsonResponse;
		try {
			jsonResponse = Json.parse(responseBody);
			int success = getNumber(jsonResponse, Constants.JSON_SUCCESS).intValue();
			int failure = getNumber(jsonResponse, Constants.JSON_FAILURE).intValue();
			int canonicalIds = getNumber(jsonResponse, Constants.JSON_CANONICAL_IDS)
					.intValue();
			long multicastId = getNumber(jsonResponse, Constants.JSON_MULTICAST_ID)
					.longValue();
			MulticastResult.Builder builder = new MulticastResult.Builder(
					success, failure, canonicalIds, multicastId);
			JsonNode results = jsonResponse
					.get(Constants.JSON_RESULTS);
			if (results != null && results.isArray()) {
				for (int i = 0;i < results.size();i++) {
                    JsonNode jsonResult = results.get(i);

                    Result.Builder builder1 = new Result.Builder();

                    if (jsonResult.has(Constants.JSON_ERROR)) {
                        String error = jsonResult.get(Constants.JSON_ERROR).asText();
                        builder1.errorCode(error);
                    } else {
                        String messageId = jsonResult.get(Constants.JSON_MESSAGE_ID).asText();
                        String canonicalRegId = null;
                        if (jsonResult.has(Constants.TOKEN_CANONICAL_REG_ID)) {
                            canonicalRegId = jsonResult
                                    .get(Constants.TOKEN_CANONICAL_REG_ID).asText();
                        }
                        builder1.messageId(messageId)
                                .canonicalRegistrationId(canonicalRegId);
                    }

                    Result result = builder1.build();
					builder.addResult(result);
				}
			}
			return builder.build();
		} catch (CustomParserException e) {
			throw newIoException(responseBody, e);
		}
	}

	private IOException newIoException(String responseBody, Exception e) {
		// log exception, as IOException constructor that takes a message and
		// cause
		// is only available on Java 6
		String msg = "Error parsing JSON response (" + responseBody + ")";
		logger.log(Level.WARNING, msg, e);
		return new IOException(msg + ":" + e);
	}

	/**
	 * Sets a JSON field, but only if the value is not {@literal null}.
	 */
	private void setJsonField(Map<Object, Object> json, String field,
			Object value) {
		if (value != null) {
			json.put(field, value);
		}
	}

	private Number getNumber(JsonNode json, String field) {
		if (json == null || !json.has(field)) {
			throw new CustomParserException("Missing field: " + field);
		}

        JsonNode value = json.get(field);
		if (value == null || value.isNull() || !(value.isNumber())) {
			throw new CustomParserException("Field " + field
					+ " does not contain a number: " + value);
		}
		return value.asInt(0);
	}

	@SuppressWarnings("serial")
	class CustomParserException extends RuntimeException {
		CustomParserException(String message) {
			super(message);
		}
	}

	/**
	 * Make an HTTP post to a given URL.
	 * 
	 * @return HTTP response.
	 */

	protected HttpURLConnection post(String url, String contentType, String body) throws IOException {
		if (url == null || body == null) {
			throw new IllegalArgumentException("arguments cannot be null");
		}

        play.Logger.info(url);

		if (!url.startsWith("https://")) {
			logger.warning("URL does not use https: " + url);
		}
		logger.fine("Sending POST to " + url);
		logger.finest("POST body: " + body);
		byte[] bytes = body.getBytes(HTTP.UTF_8);
		HttpURLConnection conn = getConnection(url);
		conn.setDoOutput(true);
		conn.setUseCaches(false);
		conn.setFixedLengthStreamingMode(bytes.length);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", contentType);
		conn.setRequestProperty("Authorization", "key=" + key);
		OutputStream out = conn.getOutputStream();
		out.write(bytes);
		out.close();
		return conn;
	}

	/**
	 * Gets an {@link java.net.HttpURLConnection} given an URL.
	 */
	protected HttpURLConnection getConnection(String url) throws IOException {
		return (HttpURLConnection) new URL(url).openConnection();
	}

	/**
	 * Convenience method to convert an InputStream to a String.
	 * 
	 * <p>
	 * If the stream ends in a newline character, it will be stripped.
	 */
	protected static String getString(InputStream stream) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(nonNull(stream)));
		StringBuilder content = new StringBuilder();
		String newLine;
		do {
			newLine = reader.readLine();
			if (newLine != null) {
				content.append(newLine).append('\n');
			}
		} while (newLine != null);
		if (content.length() > 0) {
			// strip last newline
			content.setLength(content.length() - 1);
		}
		return content.toString();
	}

	static <T> T nonNull(T argument) {
		if (argument == null) {
			throw new IllegalArgumentException("argument cannot be null");
		}
		return argument;
	}

	void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

}
