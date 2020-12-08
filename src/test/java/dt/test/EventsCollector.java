package dt.test;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class EventsCollector {

    public final List<String> steps = new ArrayList<>();

    @EventListener
    public void onApplicationEvent(DelegateExecution event) {
        steps.add(event.getEventName() + ":" + event.getCurrentActivityId());
    }
}
