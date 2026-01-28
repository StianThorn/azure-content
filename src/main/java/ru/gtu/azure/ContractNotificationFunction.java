package ru.gtu.azure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.TimerTrigger;
import ru.gtu.azure.model.ContractAlert;
import ru.gtu.azure.model.NotificationContext;
import ru.gtu.azure.service.ContractService;
import ru.gtu.azure.service.NotificationService;
import ru.gtu.azure.service.OpenAIService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

public class ContractNotificationFunction {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @FunctionName("ContractNotification")
    public void run(
            @TimerTrigger(name = "contractNotificationTimer", schedule = "0 0 9 * * *") String timerInfo,
            final ExecutionContext context
    ) {
        Logger logger = context.getLogger();
        logger.info("ContractNotification triggered: " + timerInfo);

        ContractService contractService = new ContractService(objectMapper, logger);
        OpenAIService openAIService = new OpenAIService(objectMapper, logger);
        NotificationService notificationService = new NotificationService(objectMapper, logger);

        List<ContractAlert> alerts = contractService.loadExpiringContracts();
        if (alerts.isEmpty()) {
            logger.info("No contract alerts to process.");
            return;
        }

        for (ContractAlert alert : alerts) {
            String message = openAIService.buildHumanMessage(alert);
            NotificationContext notificationContext = new NotificationContext(alert, message, LocalDateTime.now());
            notificationService.sendAll(notificationContext);
        }
    }
}
