package net.binder.api.binmodification.service;

import lombok.RequiredArgsConstructor;
import net.binder.api.bin.entity.BinType;
import net.binder.api.binmodification.entity.Status;
import net.binder.api.binmodification.dto.BinUpdateRequest;
import net.binder.api.binmodification.entity.BinModification;
import net.binder.api.binmodification.repository.BinModificationRepository;
import net.binder.api.member.entity.Member;
import net.binder.api.member.service.MemberService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class BinModificationService {

    private final BinModificationRepository binModificationRepository;
    private final MemberService memberService;

    public void create(BinUpdateRequest binUpdateRequest, String email) {
        BinType type = BinType.getType(binUpdateRequest.getType());
        Member mem = memberService.findByEmail(email);


        BinModification registration = BinModification.builder()
                .title(binUpdateRequest.getTitle())
                .address(binUpdateRequest.getAddress())
                .latitude(binUpdateRequest.getLatitude())
                .longitude(binUpdateRequest.getLongitude())
                .imageUrl(binUpdateRequest.getImageUrl())
                .type(type)
                .member(mem)
                .status(Status.PENDING)
                .build();
        binModificationRepository.save(registration);
    }
}
