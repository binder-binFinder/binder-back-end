package net.binder.api.filtering.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import net.binder.api.filtering.entity.Curse;
import net.binder.api.filtering.repository.CurseRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class CurseManager {

    private final CurseRepository curseRepository;

    @Transactional
    public void addWords(List<String> newCurseWords) {

        List<Curse> newCurses = newCurseWords.stream()
                .map(Curse::new)
                .toList();

        curseRepository.saveAll(newCurses);
    }
}
