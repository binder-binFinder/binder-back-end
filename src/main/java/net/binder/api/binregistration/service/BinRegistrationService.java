package net.binder.api.binregistration.service;

import lombok.RequiredArgsConstructor;
import net.binder.api.bin.entity.BinType;
import net.binder.api.binregistration.dto.BinCreateRequest;
import net.binder.api.binregistration.entity.BinRegistration;
import net.binder.api.binregistration.entity.Status;
import net.binder.api.binregistration.repository.BinRegistrationRepository;
import net.binder.api.member.entity.Member;
import net.binder.api.member.service.MemberService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class BinRegistrationService {

    private final BinRegistrationRepository binRegistrationRepository;

    private final MemberService memberService;


    public void create(BinCreateRequest binCreateRequest, String email) {
        BinType type = BinType.getType(binCreateRequest.getType());
        Member mem = memberService.findByEmail(email);

        BinRegistration registration = BinRegistration.builder()
                .title(binCreateRequest.getTitle())
                .address(binCreateRequest.getAddress())
                .latitude(binCreateRequest.getLatitude())
                .longitude(binCreateRequest.getLongitude())
                .imageUrl(binCreateRequest.getImageUrl())
                .type(type)
                .member(mem)
                .status(Status.PENDING)
                .build();
        binRegistrationRepository.save(registration);
    }
}
