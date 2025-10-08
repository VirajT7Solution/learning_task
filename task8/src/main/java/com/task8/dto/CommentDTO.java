package com.task8.dto;


import jakarta.validation.constraints.NotBlank;

public class CommentDTO {
    @NotBlank
    private String comment;

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
