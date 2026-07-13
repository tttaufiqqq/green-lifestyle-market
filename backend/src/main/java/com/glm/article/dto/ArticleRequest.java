package com.glm.article.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ArticleRequest(
    @NotBlank @Size(max = 150) String title,
    @NotBlank @Size(max = 300) String excerpt,
    @NotBlank String bodyMd,
    String coverImage,
    @Pattern(regexp = "DRAFT|PUBLISHED") String status
) {}
