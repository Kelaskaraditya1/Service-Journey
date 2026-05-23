package com.starkIndustries.serviceJourney.model;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "Events")
public class Event {

  @Id
  public String eventId;
  public String page;
  public Instant enterTime;
  public Instant exitTime;
  public Long timeSpent;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "session_id")
  @JsonIgnore
  public Session session;


  @Column(columnDefinition = "integer default 0")
  @Builder.Default
  public Integer sequenceOrder = 0;


  @Enumerated(EnumType.STRING)
  @Builder.Default
  public EventStatus status = EventStatus.ACTIVE;


  @Column(nullable = true)
  public String previousEventId;

}
