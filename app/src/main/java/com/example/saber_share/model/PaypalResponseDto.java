package com.example.saber_share.model;

/**
 * DTO de respuesta de PayPal.
 * approvalUrl: URL a abrir en el navegador para que el usuario apruebe el pago.
 * paymentId: ID del pago a guardar para la confirmación.
 */
public class PaypalResponseDto {
    private String approvalUrl;
    private String paymentId;
    private String estado;
    private String mensaje;

    public PaypalResponseDto() {}

    public String getApprovalUrl() { return approvalUrl; }
    public void setApprovalUrl(String approvalUrl) { this.approvalUrl = approvalUrl; }

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
}
