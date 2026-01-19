package com.atoook.otsukailist.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.atoook.otsukailist.model.Member;

public interface MemberRepository extends JpaRepository<Member, UUID> {
    List<Member> findByItemListId(UUID listId);

    Optional<Member> findByIdAndItemListId(UUID id, UUID listId);

    boolean existsByIdAndItemListId(UUID id, UUID listId);

}
