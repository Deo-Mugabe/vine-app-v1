package vine.vine.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vine.vine.domain.dto.response.Systab1Response;
import vine.vine.service.Systab1Service;

import java.util.List;

@RestController
@RequestMapping("/api/v1/systab1")
@RequiredArgsConstructor
public class Systab1Controller {

    private final Systab1Service systab1Service;


    @GetMapping
    public ResponseEntity<Page<Systab1Response>> getAllSystab1(Pageable pageable) {
        Page<Systab1Response> responses = systab1Service.getAllSystab1(pageable);
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

}
