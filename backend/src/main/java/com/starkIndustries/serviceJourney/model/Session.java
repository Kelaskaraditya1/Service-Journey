package com.starkIndustries.serviceJourney.model;

import java.time.Instant;
import java.util.List;

import jakarta.persistence.CascadeType;
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
  public boolean expired;

  @Enumerated(EnumType.STRING)
  public ExpiryReasons expiryReasons;
  public String lastPage;
  public Long duration;
  public Instant lastActivityTime;

  @OneToMany(mappedBy = "session",cascade = CascadeType.ALL)
  public List<Event> events;

}
