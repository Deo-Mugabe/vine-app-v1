package vine.vine.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vine.vine.domain.dto.response.Systab1Response;

import java.util.List;

public interface Systab1Service {

    Page<Systab1Response> getAllSystab1(Pageable pageable);
}
