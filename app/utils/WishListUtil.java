package utils;

import com.fasterxml.jackson.databind.JsonNode;
import controllers.AbstractApplication;
import models.classes.*;
import models.cloud.forms.WishListViewModel;
import models.database.FinderFactory;
import models.database.IFinder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        return wishList != null
                && user != null
                && wishList.getUser().getId() == user.getId();
    }

    public static Map<Integer, Long> fillProducts(JsonNode body, WishList wishList) {
        Map<Integer, Long> productIds = new HashMap<Integer, Long>(25);
        if (body.hasNonNull(AbstractApplication.ParameterKey.PRODUCTS)) {
            JsonNode products = body.get(AbstractApplication.ParameterKey.PRODUCTS);
            if (products.isArray()) {
                for(int i = 0; i < products.size(); i++) {
                    JsonNode jsonProduct = products.get(i);
                    if (jsonProduct.hasNonNull(AbstractApplication.ParameterKey.NAME)
                            && jsonProduct.has(AbstractApplication.ParameterKey.NICK_NAME)
                            && jsonProduct.has(AbstractApplication.ParameterKey.MANUFACTURER)) {
                        Manufacturer manufacturer = null;

                        JsonNode jsonManufacturer = jsonProduct.get(AbstractApplication.ParameterKey.MANUFACTURER);
                        if (jsonManufacturer != null
                                && jsonManufacturer.hasNonNull(AbstractApplication.ParameterKey.NAME)) {
                            String name = jsonManufacturer.get(AbstractApplication.ParameterKey.NAME).asText();

                            manufacturer = new Manufacturer();
                            manufacturer.setName(name);
                            manufacturer.save();
                            manufacturer.refresh();
                        }

                        String name = jsonProduct.get(AbstractApplication.ParameterKey.NAME).asText();
                        String nickName = jsonProduct.get(AbstractApplication.ParameterKey.NICK_NAME).asText();

                        Product product = new Product();
                        product.setName(name);
                        product.setNickName(nickName);
                        product.setManufacturer(manufacturer);
                        product.save();
                        product.refresh();

                        productIds.put(i, product.getId());

                        WishListProduct wishListProduct = new WishListProduct();
                        wishListProduct.setProduct(product);
                        wishListProduct.setWishList(wishList);
                        wishListProduct.setStatus(WishListProduct.Status.ACTIVE);
                        wishListProduct.save();
                    }
                }
            }
        }
        return productIds;
    }

    public static List<WishListViewModel> getPerfilWishList(User user, String login, boolean isMe) {
        List<WishListViewModel> wishlistsVM = new ArrayList<WishListViewModel>(10);
        List<WishList> wishLists;
        if (!isMe) {
            FinderFactory factory = FinderFactory.getInstance();
            IFinder<User> finder = factory.get(User.class);
            IFinder<WishList> finderW = factory.get(WishList.class);
            user = finder.selectUnique(
                    new String[] {AbstractApplication.FinderKey.LOGIN},
                    new Object[] {login});

            wishLists = finderW.selectAll(
                    new String[]{AbstractApplication.FinderKey.USER_ID, AbstractApplication.FinderKey.STATUS},
                    new Object[]{user.getId(), WishList.Status.ACTIVE.ordinal()});
        } else {
            wishLists = user.getWishList();
        }

        for (WishList wishList : wishLists) {
            if (wishList.getStatus() == WishList.Status.ACTIVE) {
                wishlistsVM.add(new WishListViewModel(wishList));
            }
        }
        return wishlistsVM;
    }

}
