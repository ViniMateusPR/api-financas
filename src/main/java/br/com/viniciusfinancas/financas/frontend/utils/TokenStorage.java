package br.com.viniciusfinancas.financas.frontend.utils;

public class TokenStorage {
    private static String token;
    private static int userId;

    public static void setToken(String token) {
        TokenStorage.token = token;
    }

    public static String getToken() {
        return token;
    }

    public static void setUserId(int userId) {
        TokenStorage.userId = userId;
    }

    public static int getUserId() {
        return userId;
    }

    // Métodos para limpar o token e o userId
    public static void clearToken() {
        token = null;
    }

    public static void clearUserId() {
        userId = 0;
    }
}
