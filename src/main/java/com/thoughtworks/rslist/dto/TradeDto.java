package com.thoughtworks.rslist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "trade")
public class TradeDto {

    @Id
    @GeneratedValue
    private int id;

    private int amount;

    private int rankNum;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserDto user;

    @ManyToOne
    @JoinColumn(name = "rs_event_id")
    private RsEventDto rsEvent;
}
