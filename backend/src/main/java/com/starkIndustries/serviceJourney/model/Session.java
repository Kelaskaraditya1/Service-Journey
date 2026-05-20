package com.starkIndustries.serviceJourney.model;

import java.time.Instant;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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
@Table(name = "Sessions")
public class Session {

  @Id
  public String sessionId;
  public String userId;
  public Instant startTime;
  public Instant endTime;

  // ======================================================================
  // DEPRECATED FIELDS — kept for backward compatibility and reference.
  // These are superseded by sessionStatus but NOT removed.
  // ======================================================================

  /**
   * @deprecated Use {@link #sessionStatus} instead.
   * Kept for backward compatibility with existing data.
   */
  public boolean expired;

  /**
   * @deprecated Use {@link #sessionStatus} instead.
   * Kept for backward compatibility with existing data.
   */
  @Enumerated(EnumType.STRING)
  public ExpiryReasons expiryReasons;

  // ======================================================================
  // EXISTING FIELDS
  // ======================================================================

  public String lastPage;
  public Long duration;
  public Instant lastActivityTime;

  @OneToMany(mappedBy = "session", cascade = CascadeType.ALL)
  public List<Event> events;

  // ======================================================================
  // NEW FIELDS — Added for event-transition architecture & Temporal prep
  // ======================================================================

  /**
   * Lifecycle status of this session.
   * Maps cleanly to future Temporal workflow states.
   */
  @Enumerated(EnumType.STRING)
  @Builder.Default
  public SessionStatus sessionStatus = SessionStatus.ACTIVE;

  /**
   * Placeholder for future Temporal workflow ID.
   * Will be populated when Temporal integration is added in Step 2.
   */
  @Column(nullable = true)
  public String workflowId;

  /**
   * Tracks the currently active event within this session.
   * Enables quick lookup without querying the events table.
   */
  @Column(nullable = true)
  public String activeEventId;

  /**
   * Running count of events created in this session.
   * Used to assign sequenceOrder to new events.
   */
  @Column(columnDefinition = "integer default 0")
  @Builder.Default
  public Integer eventCount = 0;

}
