package vine.vine.controller;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import vine.vine.service.ChargesService;

@RestController
@RequestMapping("/api/v1/charges")
@RequiredArgsConstructor
public class ChargesController {

    private final ChargesService chargesService;

    // @GetMapping
    // private ResponseEntity<Page<ChargesResponse>> getAllCharges(Pageable pageable) {
    //     Page<ChargesResponse> responses = chargesService.getAllCharges(pageable);
    //     return new ResponseEntity<>(responses, HttpStatus.OK);
    // }

//    @GetMapping("/generate/{bookId}")
//    public ResponseEntity<String> generateByBookingId(@PathVariable Long bookId) {
//        try {
//            String content = chargesService.getPrisonerChargesByBookingId(bookId);
//            return ResponseEntity.ok("File generated for booking ID " + bookId + " with " + content.split("\n").length + " lines.");
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
//        }
//    }

//    @GetMapping("/generate")
//    public ResponseEntity<String> processBookings(LocalDateTime lastRunTime) {
//        try {
//            String content = chargesService.processBookings(lastRunTime);
//            return ResponseEntity.ok("File generated " + content.split("\n").length + " lines.");
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
//        }
//    }



}
