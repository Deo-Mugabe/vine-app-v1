package vine.vine.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vine.vine.domain.dto.response.JmmainResponse;

public interface JmmainService {
    Page<JmmainResponse> getAllJmmain(Pageable pageable);
}
