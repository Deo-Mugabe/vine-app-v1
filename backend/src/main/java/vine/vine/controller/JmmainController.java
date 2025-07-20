package vine.vine.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vine.vine.domain.dto.response.JmmainResponse;
import vine.vine.service.JmmainService;

@RestController
@RequestMapping("/api/v1/jmmain")
@RequiredArgsConstructor
public class JmmainController {

    private final JmmainService jmmainService;

    @GetMapping
    public ResponseEntity<Page<JmmainResponse>> getAllJmmain(Pageable pageable){
        Page<JmmainResponse> responses = jmmainService.getAllJmmain(pageable);
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }
}
