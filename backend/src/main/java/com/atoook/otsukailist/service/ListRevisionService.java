package com.atoook.otsukailist.service;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.atoook.otsukailist.exception.ResourceNotFoundException;
import com.atoook.otsukailist.repository.ItemListRepository;

@Service
@RequiredArgsConstructor
public class ListRevisionService {

    private final ItemListRepository itemListRepo;
    private final static String MSG_NOT_FOUND = "%s が見つかりません";

    /**
     * listId の revision を原子的に +1 し、更新後revisionを返す。
     * 呼び出しは「更新系ServiceのTxの中」で行う想定。
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public long incrementAndGet(UUID listId) {
        int updated = itemListRepo.incrementRevision(listId);
        if (updated != 1) {
            throw new ResourceNotFoundException(String.format(MSG_NOT_FOUND, "リスト"));
        }
        return itemListRepo.findRevision(listId).orElseThrow();
    }
}
