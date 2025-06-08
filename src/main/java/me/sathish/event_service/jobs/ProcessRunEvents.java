package me.sathish.event_service.jobs;

import me.sathish.event_service.jobs.domainrecs.GarminRunDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ProcessRunEvents {
    private static final Logger logger = LoggerFactory.getLogger(ProcessRunEvents.class);
    private final java.util.Set<GarminRunDTO> processedRuns =
            java.util.Collections.newSetFromMap(new java.util.concurrent.ConcurrentHashMap<>());

    @Transactional
    @RabbitListener(queues = "garminrun-run-queue")
    //    @Scheduled(cron = "${processRunEvents.fixedRate}")
    //    @SchedulerLock(name = "processNewOrders")
    public void processRunEvents(GarminRunDTO garminRunDTO) {
        //        LockAssert.assertLocked();
        if (garminRunDTO == null) {
            logger.warn("Received null GarminRunDTO, skipping processing.");
            return;
        }
        processedRuns.add(garminRunDTO);
        logger.info("Total processed runs count: {}", processedRuns.size());
        // Add your event processing logic here
    }
}
