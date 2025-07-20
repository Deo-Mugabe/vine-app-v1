package vine.vine.service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import vine.vine.domain.dto.response.NmmainResponse;
import vine.vine.repository.*;
import vine.vine.service.NmmainService;

@Service
@RequiredArgsConstructor
public class NmmainServiceImpl implements NmmainService {

    private final NmmainRepository nmmainRepository;
    private final ChargesRepository chargesRepository;
    private final JmmainRepository jmmainRepository;
    private final Systab1Repository systab1Repository;
    private final JreleaseRepository jreleaseRepository;
    private final JfachistRepository jfachistRepository;


    @Override
    public Page<NmmainResponse> getAllNmmain(Pageable pageable) {
        return nmmainRepository.findAll(pageable)
                .map(NmmainResponse::from);
    }

   public String getPrisonQuery(){
        StringBuilder sb = new StringBuilder();

        return sb.toString();
   }
}
