package com.thoughtworks.rslist.service;

import com.thoughtworks.rslist.domain.RsEvent;
import com.thoughtworks.rslist.domain.Vote;
import com.thoughtworks.rslist.dto.RsEventDto;
import com.thoughtworks.rslist.dto.TradeDto;
import com.thoughtworks.rslist.dto.UserDto;
import com.thoughtworks.rslist.dto.VoteDto;
import com.thoughtworks.rslist.repository.RsEventRepository;
import com.thoughtworks.rslist.repository.TradeRepository;
import com.thoughtworks.rslist.repository.UserRepository;
import com.thoughtworks.rslist.repository.VoteRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RsService {
  final RsEventRepository rsEventRepository;
  final UserRepository userRepository;
  final VoteRepository voteRepository;
  final TradeRepository tradeRepository;

  public RsService(RsEventRepository rsEventRepository, UserRepository userRepository, VoteRepository voteRepository, TradeRepository tradeRepository) {
    this.rsEventRepository = rsEventRepository;
    this.userRepository = userRepository;
    this.voteRepository = voteRepository;
    this.tradeRepository = tradeRepository;
  }

  public void vote(Vote vote, int rsEventId) {
    Optional<RsEventDto> rsEventDto = rsEventRepository.findById(rsEventId);
    Optional<UserDto> userDto = userRepository.findById(vote.getUserId());
    if (!rsEventDto.isPresent()
        || !userDto.isPresent()
        || vote.getVoteNum() > userDto.get().getVoteNum()) {
      throw new RuntimeException();
    }
    VoteDto voteDto =
        VoteDto.builder()
            .localDateTime(vote.getTime())
            .num(vote.getVoteNum())
            .rsEvent(rsEventDto.get())
            .user(userDto.get())
            .build();
    voteRepository.save(voteDto);
    UserDto user = userDto.get();
    user.setVoteNum(user.getVoteNum() - vote.getVoteNum());
    userRepository.save(user);
    RsEventDto rsEvent = rsEventDto.get();
    rsEvent.setVoteNum(rsEvent.getVoteNum() + vote.getVoteNum());
    rsEventRepository.save(rsEvent);
  }

  public ResponseEntity buy(int amount, int rank, RsEvent rsEvent) {
    Optional<RsEventDto> eventOptional = rsEventRepository.findByRankNum(rank);
    Optional<UserDto> userOptional = userRepository.findById(rsEvent.getUserId());
    if (!eventOptional.isPresent() || !userOptional.isPresent()
                                   || eventOptional.get().getAmount() > amount) {
      return ResponseEntity.badRequest().build();
      // throw new RuntimeException();
    }
    RsEventDto rsEventDto = eventOptional.get();
    rsEventDto.setRankNum(rank);
    rsEventDto.setVoteNum(rsEvent.getVoteNum());
    rsEventDto.setEventName(rsEvent.getEventName());
    rsEventDto.setKeyword(rsEvent.getKeyword());
    rsEventDto.setUser(userOptional.get());
    rsEventDto.setAmount(amount);
    TradeDto tradeDto = TradeDto.builder().amount(amount).rankNum(rank).rsEvent(rsEventDto)
            .user(userOptional.get()).build();
    rsEventRepository.save(rsEventDto);
    tradeRepository.save(tradeDto);
    return ResponseEntity.ok().build();
  }

  public Optional<RsEventDto> findByRankNum(int rank) {
    return rsEventRepository.findByRankNum(rank);
  }
}
