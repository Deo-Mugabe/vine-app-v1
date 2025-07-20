package vine.vine.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vine.vine.domain.dto.response.NmmainResponse;
import vine.vine.service.NmmainService;

@RestController
@RequestMapping("/api/v1/nmmain")
@RequiredArgsConstructor
public class NmmainController {

    private final NmmainService nmmainService;

    @GetMapping
    public ResponseEntity<Page<NmmainResponse>> getAllNmmain(Pageable pageable){
        Page<NmmainResponse> responses = nmmainService.getAllNmmain(pageable);
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }
}
