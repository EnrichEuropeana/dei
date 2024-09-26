package pl.psnc.dei.service;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum TokenType {
	ACCESS_TOKEN("access_token"),

	REFRESH_TOKEN("refresh_token");

	private String token;

	public String getValue() {
		return token;
	}
}
