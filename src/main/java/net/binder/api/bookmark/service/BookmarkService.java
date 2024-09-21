package net.binder.api.bookmark.service;

import lombok.RequiredArgsConstructor;
import net.binder.api.bin.entity.Bin;
import net.binder.api.bin.service.BinService;
import net.binder.api.bookmark.dto.BookmarkProjection;
import net.binder.api.bookmark.dto.BookmarkResponse;
import net.binder.api.bookmark.entity.Bookmark;
import net.binder.api.bookmark.repository.BookmarkQueryRepository;
import net.binder.api.bookmark.repository.BookmarkRepository;
import net.binder.api.common.exception.BadRequestException;
import net.binder.api.member.entity.Member;
import net.binder.api.member.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BookmarkService {

    @Autowired
    private BinService binService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Autowired
    private BookmarkQueryRepository bookmarkQueryRepository;

    public Bookmark createBookMark(String email, Long binId){
        if(bookmarkRepository.existsByMember_EmailAndBin_Id(email, binId)){
            throw new BadRequestException("이미 북마크를 한 쓰레기통입니다.");
        }

        Member member = memberService.findByEmail(email);
        Bin bin = binService.findById(binId);
        Bookmark bookmark = Bookmark.builder()
                .member(member)
                .bin(bin)
                .build();
        Bookmark save = bookmarkRepository.save(bookmark);
        bin.increaseBookmark();

        return save;
    }

    public void deleteBookMark(String email, Long binId){
        if(!bookmarkRepository.existsByMember_EmailAndBin_Id(email, binId)){
            throw new BadRequestException("북마크를 하지 않은 쓰레기통입니다.");
        }
        bookmarkRepository.deleteByMember_EmailAndBin_Id(email, binId);
        Bin bin = binService.findById(binId);
        bin.decreaseBookmark();
    }

    public List<BookmarkResponse> getBookmarks(String email, Double longitude, Double latitude, Long bookmarkId){
         return bookmarkQueryRepository.findBookmarksByMember(email, longitude, latitude, bookmarkId)
                .orElseThrow(() -> new BadRequestException("북마크 내역이 존재하지 않습니다."));
    }
}
