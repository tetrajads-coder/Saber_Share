package com.example.saber_share.util.local;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.example.saber_share.Cuenta;

import java.util.HashMap;

public class SessionManager {
    //https://www.youtube.com/watch?v=SLkQIlfRWgM pa que vean que si le muevo de que aca de aca ps de aca del yo mero
    private static final String PREF_NAME = "SaberShareSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_ID = "idUsuario";
    private static final String KEY_USUARIO = "usuario";
    private static final String KEY_PASSWORD = "password";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void createLoginSession(String usuario, String password, int id) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USUARIO, usuario);
        editor.putString(KEY_PASSWORD, password);
        editor.putInt(KEY_ID, id);
        editor.commit();
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
        HashMap<String, String> user = new HashMap<String, String>();
        user.put(KEY_USUARIO, pref.getString(KEY_USUARIO, null));
        user.put(KEY_PASSWORD, pref.getString(KEY_PASSWORD, null));
        return user;
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