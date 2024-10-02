package net.binder.api.bookmark.service;

import lombok.RequiredArgsConstructor;
import net.binder.api.bin.entity.Bin;
import net.binder.api.bin.service.BinService;
import net.binder.api.bookmark.dto.BookmarkResponse;
import net.binder.api.bookmark.entity.Bookmark;
import net.binder.api.bookmark.repository.BookmarkQueryRepository;
import net.binder.api.bookmark.repository.BookmarkRepository;
import net.binder.api.common.exception.BadRequestException;
import net.binder.api.member.entity.Member;
import net.binder.api.member.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
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

        Bin bin = binService.findById(binId);
        Member member = memberService.findByEmail(email);
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

    public List<BookmarkResponse> getAllBookmarks(
            String email,
            Double longitude,
            Double latitude,
            Long bookmarkId,
            Double lastDistance){

        if (longitude < 124 || longitude > 133 || latitude < 33 || latitude > 44) {
            throw new BadRequestException("잘못된 좌표입니다.");
        }
        if(bookmarkId != null && lastDistance == null || bookmarkId==null && lastDistance !=null ){
            throw new BadRequestException("마지막 북마크 id와 거리를 함께 보내주셔야 합니다.");
        }

        return bookmarkQueryRepository.findBookmarksByMember(email, longitude, latitude, bookmarkId, lastDistance)
                .orElseThrow(() -> new BadRequestException("북마크 내역이 존재하지 않습니다."));
    }
}
