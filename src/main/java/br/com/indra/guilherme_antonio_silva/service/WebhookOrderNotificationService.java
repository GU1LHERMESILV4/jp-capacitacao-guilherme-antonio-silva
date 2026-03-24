package br.com.indra.guilherme_antonio_silva.service;

import br.com.indra.guilherme_antonio_silva.dto.OrderStatusChangeEventDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class WebhookOrderNotificationService implements OrderNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(WebhookOrderNotificationService.class);

    private final RestTemplate restTemplate;

    @Value("${notification.order.webhook.url:}")
    private String webhookUrl;

    public WebhookOrderNotificationService() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public void notifyStatusChange(OrderStatusChangeEventDTO event) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            // Notificação desabilitada por configuração
            logger.debug("Webhook de notificação de pedido não configurado. Evento ignorado: {}", event);
            return;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<OrderStatusChangeEventDTO> request = new HttpEntity<>(event, headers);
            restTemplate.postForEntity(webhookUrl, request, Void.class);
        } catch (RestClientException ex) {
            // Best-effort: apenas registra erro, não reverte transação de pedido
            logger.error("Falha ao enviar webhook de notificação de pedido para URL {}", webhookUrl, ex);
        }
    }
}

