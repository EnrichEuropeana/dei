package pl.psnc.dei.service;

public enum TokenType {
	ACCESS_TOKEN("access_token"),

	REFRESH_TOKEN("refresh_token");

	private String token;

	TokenType(String token) {
		this.token = token;
	}

	public String getValue() {
		return token;
	}
}
