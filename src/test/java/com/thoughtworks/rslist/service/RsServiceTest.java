package com.thoughtworks.rslist.service;

import com.thoughtworks.rslist.domain.RsEvent;
import com.thoughtworks.rslist.domain.Trade;
import com.thoughtworks.rslist.domain.Vote;
import com.thoughtworks.rslist.dto.RsEventDto;
import com.thoughtworks.rslist.dto.TradeDto;
import com.thoughtworks.rslist.dto.UserDto;
import com.thoughtworks.rslist.dto.VoteDto;
import com.thoughtworks.rslist.repository.RsEventRepository;
import com.thoughtworks.rslist.repository.TradeRepository;
import com.thoughtworks.rslist.repository.UserRepository;
import com.thoughtworks.rslist.repository.VoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

class RsServiceTest {

  RsService rsService;

  @Mock
  RsEventRepository rsEventRepository;

  @Mock
  UserRepository userRepository;

  @Mock
  VoteRepository voteRepository;

  @Mock
  TradeRepository tradeRepository;

  LocalDateTime localDateTime;

  Vote vote;

  RsEvent rsEvent;

  @BeforeEach
  void setUp() {
    initMocks(this);
    rsService = new RsService(rsEventRepository, userRepository, voteRepository, tradeRepository);
    localDateTime = LocalDateTime.now();
    vote = Vote.builder().rsEventId(1).userId(1)
            .voteNum(3).time(localDateTime).build();
    rsEvent = RsEvent.builder().eventName("ForthEvent").keyword("Entertainment")
            .voteNum(0).userId(1).build();
  }

  @Test
  void shouldVoteSuccess() {
    UserDto userPO = UserDto.builder().id(1).voteNum(5)
            .userName("Mike").gender("male").age(20)
            .phone("13688896832").email("a@b.com").build();
    RsEventDto rsEventPO = RsEventDto.builder().id(1).voteNum(1)
            .keyword("FirstEvent").eventName("Economy")
            .user(userPO).build();

    when(userRepository.findById(anyInt())).thenReturn(Optional.of(userPO));
    when(rsEventRepository.findById(anyInt())).thenReturn(Optional.of(rsEventPO));

    rsService.vote(vote, 1);

    verify(userRepository).save(userPO);
    verify(rsEventRepository).save(rsEventPO);
    verify(voteRepository).save(VoteDto.builder()
            .localDateTime(localDateTime)
            .rsEvent(rsEventPO)
            .user(userPO)
            .num(3)
            .build());

    assertEquals(userPO.getVoteNum(), 2);
    assertEquals(rsEventPO.getVoteNum(), 4);
  }

  @Test
  void shouldThrowExceptionWhenUserNotExist() {
    // given
    when(rsEventRepository.findById(anyInt())).thenReturn(Optional.empty());
    when(userRepository.findById(anyInt())).thenReturn(Optional.empty());
    //when&then
    assertThrows(RuntimeException.class, () -> {
      rsService.vote(vote, 1);
    });
  }

  @Test
  void should_buy_event_when_rank_num_is_exist() {
    UserDto userDto = UserDto.builder().userName("Mike").age(20).phone("13386688553")
            .email("mike@thoughtworks.com").gender("male").voteNum(20).build();
    RsEventDto rsEventDto = RsEventDto.builder().eventName("FirstEvent").keyword("Economy")
            .voteNum(10).user(userDto).build();

    when(rsEventRepository.findByRankNum(anyInt())).thenReturn(Optional.of(rsEventDto));
    when(userRepository.findById(anyInt())).thenReturn(Optional.of(userDto));

    rsService.buy(100, 1, rsEvent);

    verify(rsEventRepository).save(rsEventDto);

    assertEquals(rsEventDto.getAmount(), 100);
    assertEquals(rsEventDto.getRankNum(), 1);
  }

  @Test
  void should_not_buy_event_when_rank_num_or_user_id_is_not_exist() {
    when(rsEventRepository.findByRankNum(anyInt())).thenReturn(Optional.empty());
    when(userRepository.findById(anyInt())).thenReturn(Optional.empty());

    assertThrows(RuntimeException.class, () -> {
      rsService.buy(100, 10, rsEvent);
    });
  }

}
