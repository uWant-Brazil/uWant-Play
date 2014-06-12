package utils;

import models.classes.User;
import models.classes.Wishlist;

/**
 * Created by felipebenezi on 11/06/14.
 */
public abstract class WishListUtil {

    public static boolean isOwner(Wishlist wishList, User user) {
        return wishList != null && user != null && wishList.getUser().getId() == user.getId();
    }

}
