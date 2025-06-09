package com.postsmith.auth.dto;

import lombok.Data;

@Data
public class UserSessionDto {
	private String userId;
	private String sessionId;
	private String userName;
	private String email;
	private String role;
}
