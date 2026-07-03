package com.glm.order.dto;

import jakarta.validation.constraints.NotBlank;

public record MeetupRequest(@NotBlank String meetupNote) {}
