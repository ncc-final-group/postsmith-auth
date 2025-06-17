package com.postsmith.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserSessionDto {
	private String accessToken;
	private String userId;
	private String email;
	private String role;
	private String userNickname;
	private String profileImage;
}
