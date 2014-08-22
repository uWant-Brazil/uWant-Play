package utils;

import models.classes.User;
import models.classes.WishList;

/**
 * Classe utilitária para ações relacionadas a lista de desejos (WISHLIST).
 */
public abstract class WishListUtil {

    /**
     * Método responsável por verificar se a lista de desejos pertence ao usuário.
     *
     * @param wishList - Lista de desejos
     * @param user - Usuário
     * @return true se sim, false se não
     */
    public static boolean isOwner(WishList wishList, User user) {
        return wishList != null && user != null && wishList.getUser().getId() == user.getId();
    }

}
