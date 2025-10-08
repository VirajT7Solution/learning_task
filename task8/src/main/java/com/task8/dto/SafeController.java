package com.task8.dto;


import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/safe")
public class SafeController {

    private final PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

    @PostMapping("/comment")
    public CommentDTO addComment(@RequestBody CommentDTO dto) {
        // Sanitize comment before returning
        String safeComment = policy.sanitize(dto.getComment());
        CommentDTO safeDto = new CommentDTO();
        safeDto.setComment(safeComment);
        return safeDto;
    }
}
