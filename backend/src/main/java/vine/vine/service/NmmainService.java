package vine.vine.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vine.vine.domain.dto.response.NmmainResponse;

public interface NmmainService {
    Page<NmmainResponse> getAllNmmain(Pageable pageable);
}
