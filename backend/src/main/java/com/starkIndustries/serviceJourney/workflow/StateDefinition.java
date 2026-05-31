package com.starkIndustries.serviceJourney.workflow;

import java.util.List;
import lombok.Data;

@Data
public class StateDefinition {

    private List<String> onTransitionEvent;

    private List<String> onEndSession;

    private List<String> onTimeout;
}
