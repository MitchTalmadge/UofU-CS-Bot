package com.mitchtalmadge.uofu_cs_bot.domain.entity.repository;

import com.mitchtalmadge.uofu_cs_bot.domain.entity.InternalUser;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InternalUserRepository extends CrudRepository<InternalUser, Long> {

  Optional<InternalUser> findDistinctByDiscordUserId(Long discordUserId);

  Iterable<InternalUser> findAllByUnid(String unid);
}
