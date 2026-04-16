package com.example.saber_share.model;

public class PaypalRequestDto {

    private Integer usuarioId;
    private Integer itemId;
    private String  tipo;
    private String  returnUrl;
    private String  cancelUrl;

    // Deep links de la app
    private static final String RETURN_URL = "sabersha://paypal-return";
    private static final String CANCEL_URL = "sabersha://paypal-cancel";

    public PaypalRequestDto() {}

    /** Constructor usado en Comprar.java — llena automáticamente las URLs */
    public PaypalRequestDto(int usuarioId, int itemId, String tipo) {
        this.usuarioId = usuarioId;
        this.itemId    = itemId;
        this.tipo      = tipo;
        this.returnUrl = RETURN_URL;
        this.cancelUrl = CANCEL_URL;
    }

    public Integer getUsuarioId()           { return usuarioId; }
    public void    setUsuarioId(Integer v)  { this.usuarioId = v; }

    public Integer getItemId()              { return itemId; }
    public void    setItemId(Integer v)     { this.itemId = v; }

    public String getTipo()                 { return tipo; }
    public void   setTipo(String v)         { this.tipo = v; }

    public String getReturnUrl()            { return returnUrl; }
    public void   setReturnUrl(String v)    { this.returnUrl = v; }

    public String getCancelUrl()            { return cancelUrl; }
    public void   setCancelUrl(String v)    { this.cancelUrl = v; }
}