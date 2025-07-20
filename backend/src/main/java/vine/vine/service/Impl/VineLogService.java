package vine.vine.service.Impl;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import vine.vine.repository.ChargesRepository;
import vine.vine.repository.JfachistRepository;
import vine.vine.repository.JmmainRepository;
import vine.vine.repository.JreleaseRepository;
import vine.vine.repository.NmmainRepository;
import vine.vine.repository.Systab1Repository;

@Service
@RequiredArgsConstructor
public class VineLogService {

    private final ChargesRepository chargesRepository;
    private final JmmainRepository jmmainRepository;
    private final NmmainRepository nmmainRepository;
    private final Systab1Repository systab1Repository;
    private final JfachistRepository JfachistRepository;
    private final JreleaseRepository jreleaseRepository;

}
