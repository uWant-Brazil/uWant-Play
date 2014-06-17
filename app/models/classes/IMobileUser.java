package models.classes;

import java.util.List;

/**
 * Interface responsável por mapear usuários para dispositivos móveis.
 */
public interface IMobileUser {

    /**
     * Método responsável por retornar o login do usuário.
     * @return
     */
    String getLogin();

    /**
     * Método responsável por retornar o nome do usuário.
     * @return
     */
    String getName();

    /**
     * Método responsável por retornar o token do usuário.
     * @return
     */
    List<Token> getTokens();

}
