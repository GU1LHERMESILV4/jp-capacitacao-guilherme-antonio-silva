package br.com.indra.guilherme_antonio_silva.service;

import br.com.indra.guilherme_antonio_silva.service.dto.OrderStatusChangeEventDTO;

public interface OrderNotificationService {

    /**
     * Notifica mudança de status do pedido. Implementações podem ser síncronas ou assíncronas,
     * enviar e-mail, webhook, etc. Erros de notificação não devem impedir a mudança de status
     * do pedido (best-effort).
     */
    void notifyStatusChange(OrderStatusChangeEventDTO event);
}

