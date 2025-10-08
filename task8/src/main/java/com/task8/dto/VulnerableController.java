package com.task8.dto;


import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/vulnerable")
public class VulnerableController {

    @PostMapping("/comment")
    public CommentDTO addComment(@RequestBody CommentDTO dto) {
        // Simply returns whatever the user sends
        // If front-end renders this as HTML, script executes!
        return dto;
    }

    @GetMapping("/view")
    public ResponseEntity<String> viewComment(@RequestBody CommentDTO dto) {
        if (dto == null) {
            return ResponseEntity.notFound().build();
        }

        String html = "<!doctype html>\n" +
                "<html>\n<head><meta charset=\"utf-8\"><title>Vulnerable Comment</title></head>\n" +
                "<body>\n" +
                "<h3>Comment id= </h3>\n" +
                // DANGEROUS: inserting unescaped content into HTML
                "<div id=\"comment\">" + dto.getComment() + "</div>\n" +
                "</body>\n</html>";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML_VALUE)
                .body(html);
    }

}
