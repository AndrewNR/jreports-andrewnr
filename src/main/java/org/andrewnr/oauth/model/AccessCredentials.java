package org.andrewnr.oauth.model;

/**
 * JDO bean for app engine to persist
 * 
 * @author Jeff Douglas
 */
public class AccessCredentials {
	
	public static final String KEY_OAUTH_CREDENTIALS = "OAuthCredentials";
	
	private Long id;
	private String accessToken;
	private String accessTokenSecret;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getAccessTokenSecret() {
		return accessTokenSecret;
	}

	public void setAccessTokenSecret(String accessTokenSecret) {
		this.accessTokenSecret = accessTokenSecret;
	}

}