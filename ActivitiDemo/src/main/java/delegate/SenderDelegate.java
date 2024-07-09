package delegate;

// import com.tp.bpmUtils.activiti.delegate.service.IRbacService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 */
@Service("senderDelegate")
public class SenderDelegate implements ExecutionListener {

    //private Logger logger = LoggerFactory.getLogger(this.getClass());

    //@Autowired
    //private IRbacService rbacService;

    @Override
    public void notify(DelegateExecution execution) {
        // String sender = rbacService.getSender(execution);
        //logger.info("Get Sender: {}", "sender");
        execution.setVariable("sender", "sender");
    }
}
