package vine.vine.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vine.vine.service.Impl.TransferService;

@RestController
@RequestMapping("/api/v1/transfer")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    @PostMapping("/run")
    public ResponseEntity<String> triggerTransfer() {
        boolean result = transferService.ftpDataAndMugshotFiles();
        return ResponseEntity.ok("Transfer result: " + result);
    }

}
