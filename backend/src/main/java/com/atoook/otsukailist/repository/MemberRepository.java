package com.atoook.otsukailist.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.atoook.otsukailist.model.Member;

@Repository
public interface MemberRepository extends JpaRepository<Member, UUID> {
    List<Member> findByItemListId(UUID listId);

    Optional<Member> findByIdAndItemListId(UUID id, UUID listId);

    boolean existsByIdAndItemListId(UUID id, UUID listId);

}
