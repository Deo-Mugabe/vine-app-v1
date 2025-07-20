package vine.vine.service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import vine.vine.domain.dto.response.Systab1Response;
import vine.vine.repository.Systab1Repository;
import vine.vine.service.Systab1Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class Systab1ServiceImpl implements Systab1Service {

    private final Systab1Repository systab1repo;

    @Override
    public Page<Systab1Response> getAllSystab1(Pageable pageable) {
        return systab1repo.findAll(pageable)
                .map(Systab1Response::from);
    }
}
