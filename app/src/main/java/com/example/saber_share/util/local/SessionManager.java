package com.example.saber_share.util.local;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.example.saber_share.Cuenta;

import java.util.HashMap;

public class SessionManager {

    private static final String PREF_NAME = "SaberShareSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_ID = "idUsuario";
    private static final String KEY_USUARIO = "usuario";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_NOMBRE = "nombre";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;

    private static final String KEY_LAST_CHAT_ID = "last_chat_id";
    private static final String KEY_LAST_CHAT_NAME = "last_chat_name";

    public void setLastChat(int receptorId, String receptorNombre) {
        editor.putInt(KEY_LAST_CHAT_ID, receptorId);
        editor.putString(KEY_LAST_CHAT_NAME, receptorNombre);
        editor.apply();
    }

    public int getLastChatId() {
        return pref.getInt(KEY_LAST_CHAT_ID, -1);
    }

    public String getLastChatName() {
        return pref.getString(KEY_LAST_CHAT_NAME, "");
    }

    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void createLoginSession(String usuario, String password, int id, String nombre) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USUARIO, usuario);
        editor.putString(KEY_PASSWORD, password);
        editor.putInt(KEY_ID, id);
        editor.putString(KEY_NOMBRE, nombre);
        editor.commit();
    }

    public void createLoginSession(String usuario, String password, int id) {
        createLoginSession(usuario, password, id, "");
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void checkLogin() {
        if (!this.isLoggedIn()) {
            Intent i = new Intent(context, Cuenta.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
    }

    public HashMap<String, String > getUserDetails() {
        HashMap<String, String> user = new HashMap<>();
        user.put(KEY_USUARIO, pref.getString(KEY_USUARIO, null));
        user.put(KEY_PASSWORD, pref.getString(KEY_PASSWORD, null));
        user.put(KEY_NOMBRE, pref.getString(KEY_NOMBRE, null));
        return user;
    }

    public String getNombre() {
        return pref.getString(KEY_NOMBRE, "");
    }

    public void logoutUser() {
        editor.clear();
        editor.commit();
        Intent i = new Intent(context, Cuenta.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }

    public int getUserId() {
        return pref.getInt(KEY_ID, -1);
    }
}
