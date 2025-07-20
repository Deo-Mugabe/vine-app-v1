package vine.vine.service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import vine.vine.domain.dto.response.JmmainResponse;
import vine.vine.repository.JmmainRepository;
import vine.vine.service.JmmainService;

@Service
@RequiredArgsConstructor
public class JmmainServiceImpl implements JmmainService {

    private final JmmainRepository jmmainRepository;

    @Override
    public Page<JmmainResponse> getAllJmmain(Pageable pageable) {
        return jmmainRepository.findAll(pageable)
                .map(JmmainResponse::from);
    }
}
