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

  /** The screen/page name this event represents */
  public String page;

  public Instant enterTime;
  public Instant exitTime;

  /** Duration in milliseconds spent on this page */
  public Long timeSpent;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "session_id")
  @JsonIgnore
  public Session session;

  // ======================================================================
  // NEW FIELDS — Added for event sequencing & transition architecture
  // ======================================================================

  /**
   * Order of this event within the session (1-based).
   * Used for reconstructing the user's navigation journey in sequence.
   */
  @Column(columnDefinition = "integer default 0")
  @Builder.Default
  public Integer sequenceOrder = 0;

  /**
   * Lifecycle status of this event — ACTIVE while user is on the page,
   * COMPLETED once they navigate away or session ends.
   */
  @Enumerated(EnumType.STRING)
  @Builder.Default
  public EventStatus status = EventStatus.ACTIVE;

  /**
   * Links to the previous event in this session's sequence.
   * Null for the first event. Enables linked-list traversal of the journey.
   */
  @Column(nullable = true)
  public String previousEventId;

}
