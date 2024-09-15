package net.binder.api.bookmark.service;

import lombok.RequiredArgsConstructor;
import net.binder.api.bin.entity.Bin;
import net.binder.api.bin.service.BinService;
import net.binder.api.bookmark.entity.Bookmark;
import net.binder.api.bookmark.repository.BookmarkRepository;
import net.binder.api.member.entity.Member;
import net.binder.api.member.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class BookMarkService {

    @Autowired
    private BinService binService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private BookmarkRepository bookmarkRepository;

    public void saveBookMark(String email, Long binId){
        Member member = memberService.findByEmail(email);
        Bin bin = binService.findById(binId);

        Bookmark bookmark = Bookmark.builder()
                .member(member)
                .bin(bin)
                .build();
        bookmarkRepository.save(bookmark);
    }
}
