package com.example.saber_share.util.local;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Gestiona la sesión del usuario logueado.
 * Guarda idUsuario, nombre, email y rol en SharedPreferences.
 * Uso: SessionManager.getInstance(context).getUsuarioId()
 */
public class SessionManager {

    private static final String PREF_NAME = "SaberShareSession";
    private static final String KEY_ID = "usuarioId";
    private static final String KEY_NOMBRE = "nombre";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_ROL = "rol";
    private static final String KEY_LOGGED_IN = "isLoggedIn";

    private static SessionManager instance;
    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    private SessionManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public static SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context);
        }
        return instance;
    }

    public void guardarSesion(int id, String nombre, String email, String rol) {
        editor.putInt(KEY_ID, id);
        editor.putString(KEY_NOMBRE, nombre);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_ROL, rol);
        editor.putBoolean(KEY_LOGGED_IN, true);
        editor.apply();
    }

    public int getUsuarioId() {
        return prefs.getInt(KEY_ID, -1);
    }
    public String getNombre() {
        return prefs.getString(KEY_NOMBRE, "");
    }

    public String getEmail() {
        return prefs.getString(KEY_EMAIL, "");
    }

    public String getRol() {
        return prefs.getString(KEY_ROL, "USUARIO");
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_LOGGED_IN, false);
    }

    public boolean esChalan() {
        return "CHALAN".equals(getRol());
    }

    public boolean esAdmin() {
        return "ADMINISTRADOR".equals(getRol());
    }

    public void cerrarSesion() {
        editor.clear();
        editor.apply();
    }
}
