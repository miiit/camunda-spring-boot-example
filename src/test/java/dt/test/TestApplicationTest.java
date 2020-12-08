package dt.test;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.BaseDelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateListener;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.spring.boot.starter.event.EventPublisherPlugin;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = TestApplication.class)
public class TestApplicationTest {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private EventsCollector eventsCollector;

    @Test
    public void shouldShowExecutionListenerAddedTwiceForTestTaskWhichIsWrong() {
        ProcessDefinition processDefinition = repositoryService.getProcessDefinition(repositoryService.createProcessDefinitionQuery().singleResult().getId());
        List<DelegateListener<? extends BaseDelegateExecution>> delegateListeners = ((ProcessDefinitionEntity) processDefinition).getChildActivity("TestTask").getListeners().get("start");
        List<DelegateListener<? extends BaseDelegateExecution>> springEventListeners = delegateListeners.stream()
                .filter(delegateListener -> delegateListener.getClass().getPackage().getName().equals(EventPublisherPlugin.class.getPackage().getName()))
                .collect(Collectors.toList());

        assertEquals(2, springEventListeners.size()); //Execution listener is added twice to TestTask activity which is wrong
        assertSame(springEventListeners.get(0), springEventListeners.get(1)); //Same instance added twice
    }

    @Test
    public void shouldShowDuplicateEndTestTaskEventWhichIsWrong() throws InterruptedException {
        runtimeService.startProcessInstanceByKey("BoundaryEventCaseProcess");
        Thread.sleep(2000);
        System.out.println(eventsCollector.steps);
        long countEndTestTask = eventsCollector.steps.stream().filter(s -> s.equals("end:TestTask")).count();

        assertEquals(2, countEndTestTask); //Shows duplicate end TestTask event which is wrong.
    }
}