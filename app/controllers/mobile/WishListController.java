package controllers.mobile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.AbstractApplication;
import models.classes.*;
import models.database.FinderFactory;
import models.database.IFinder;
import models.exceptions.*;
import play.libs.Json;
import play.mvc.Result;
import utils.UserUtil;
import utils.WishListUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Controlador responsável pelo tratamento em requisições mobile relacionados a lista de desejos.
 */
public class WishListController extends AbstractApplication {

    /**
     * Método responsável pela criação de uma lista de desejos com/sem produtos vinculados ao mesmo.
     * Caso a lista já possua produtos vinculados, o mesmo serão salvos a partir de um relacionamento no BD.
     * @return JSON
     */
    public static Result create() {
        ObjectNode jsonResponse = Json.newObject();
        try {
            User user = authenticateToken();
            if (user != null) {
                if (UserUtil.isAvailable(user)) {
                    JsonNode body = request().body().asJson();
                    if (body != null) {
                        if (body.hasNonNull(ParameterKey.TITLE) && body.hasNonNull(ParameterKey.DESCRIPTION)) {
                            String title = body.get(ParameterKey.TITLE).asText();
                            String description = body.get(ParameterKey.DESCRIPTION).asText();

                            Wishlist wishList = new Wishlist();
                            wishList.setTitle(title);
                            wishList.setDescription(description);
                            wishList.setUser(user);
                            wishList.setStatus(Wishlist.Status.ACTIVE);
                            wishList.save();
                            wishList.refresh();

                            if (body.hasNonNull(ParameterKey.PRODUCTS)) {
                                JsonNode products = body.get(ParameterKey.PRODUCTS);
                                if (products.isArray()) {
                                    for(int i = 0; i < products.size(); i++) {
                                        JsonNode jsonProduct = products.get(i);
                                        if (jsonProduct.hasNonNull(ParameterKey.NAME) && jsonProduct.has(ParameterKey.NICK_NAME) && jsonProduct.has(ParameterKey.MANUFACTURER)) {
                                            Manufacturer manufacturer = null;

                                            JsonNode jsonManufacturer = jsonProduct.get(ParameterKey.MANUFACTURER);
                                            if (jsonManufacturer != null && jsonManufacturer.hasNonNull(ParameterKey.NAME)) {
                                                String name = jsonManufacturer.get(ParameterKey.NAME).asText();

                                                manufacturer = new Manufacturer();
                                                manufacturer.setName(name);
                                                manufacturer.save();
                                                manufacturer.refresh();
                                            }

                                            String name = jsonProduct.get(ParameterKey.NAME).asText();
                                            String nickName = jsonProduct.get(ParameterKey.NICK_NAME).asText();

                                            Product product = new Product();
                                            product.setName(name);
                                            product.setNickName(nickName);
                                            product.setManufacturer(manufacturer);
                                            product.save();
                                            product.refresh();

                                            WishlistProduct wishListProduct = new WishlistProduct();
                                            wishListProduct.setProduct(product);
                                            wishListProduct.setWishList(wishList);
                                            wishListProduct.setStatus(WishlistProduct.Status.ACTIVE);
                                            wishListProduct.save();
                                        }
                                    }
                                }
                            }

                            jsonResponse.put(ParameterKey.STATUS, true);
                            jsonResponse.put(ParameterKey.MESSAGE, "Lista de desejo (" + title + ") foi criada com sucesso.");
                        } else {
                            throw new JSONBodyException();
                        }
                    } else {
                        throw new JSONBodyException();
                    }
                } else {
                    throw new AuthenticationException();
                }
            } else {
                throw new TokenException();
            }
        } catch (UWException e) {
            e.printStackTrace();
            jsonResponse.put(ParameterKey.STATUS, false);
            jsonResponse.put(ParameterKey.MESSAGE, e.getMessage());
            jsonResponse.put(ParameterKey.ERROR, e.getCode());
        }

        return ok(jsonResponse);
    }

    /**
     * Método responsável pela atualização de uma lista de desejos - sem alterar os produtos na mesma.
     * Apenas os donos da lista de desejos podem efetuar tal ação.
     * @return JSON
     */
    public static Result update() {
        ObjectNode jsonResponse = Json.newObject();
        try {
            User user = authenticateToken();
            if (user != null) {
                if (UserUtil.isAvailable(user)) {
                    JsonNode body = request().body().asJson();
                    if (body != null) {
                        if (body.hasNonNull(ParameterKey.ID) && body.hasNonNull(ParameterKey.TITLE) && body.hasNonNull(ParameterKey.DESCRIPTION)) {
                            Long id = body.get(ParameterKey.ID).asLong();
                            String title = body.get(ParameterKey.TITLE).asText();
                            String description = body.get(ParameterKey.DESCRIPTION).asText();

                            FinderFactory factory = FinderFactory.getInstance();
                            IFinder<Wishlist> finder = factory.get(Wishlist.class);
                            Wishlist wishList = finder.selectUnique(
                                    new String[] { FinderKey.ID },
                                    new Object[] { id });

                            if (wishList != null && WishListUtil.isOwner(wishList, user)) {
                                wishList.setTitle(title);
                                wishList.setDescription(description);
                                wishList.setUser(user);
                                wishList.update();

                                jsonResponse.put(ParameterKey.STATUS, true);
                                jsonResponse.put(ParameterKey.MESSAGE, "A lista de desejos (" + title + ") foi editada com sucesso.");
                            } else {
                                throw new WishListDontExistException();
                            }
                        } else {
                            throw new JSONBodyException();
                        }
                    } else {
                        throw new JSONBodyException();
                    }
                } else {
                    throw new AuthenticationException();
                }
            } else {
                throw new TokenException();
            }
        } catch (UWException e) {
            e.printStackTrace();
            jsonResponse.put(ParameterKey.STATUS, false);
            jsonResponse.put(ParameterKey.MESSAGE, e.getMessage());
            jsonResponse.put(ParameterKey.ERROR, e.getCode());
        }

        return ok(jsonResponse);
    }

    /**
     * Método responsável pela listagem de uma lista de desejos do usuário.
     * Os produtos deverão ser buscado em outra requsição - <link>getProductByWishList()</link>
     * @return JSON
     */
    public static Result list() {
        ObjectNode jsonResponse = Json.newObject();
        try {
            User user = authenticateToken();
            if (user != null) {
                if (UserUtil.isAvailable(user)) {
                    FinderFactory factory = FinderFactory.getInstance();
                    IFinder<Wishlist> finder = factory.get(Wishlist.class);
                    List<Wishlist> wishLists = finder.selectAll(
                            new String[] { FinderKey.USER_ID , FinderKey.STATUS },
                            new Object[] { user.getId(), Wishlist.Status.ACTIVE.ordinal() });

                    if (wishLists != null) {
                        jsonResponse.put(ParameterKey.STATUS, true);
                        jsonResponse.put(ParameterKey.MESSAGE, "A consulta foi realizada com sucesso.");
                        jsonResponse.put(ParameterKey.WISHLIST, Json.toJson(wishLists));
                    } else {
                        throw new WishListDontExistException();
                    }
                } else {
                    throw new AuthenticationException();
                }
            } else {
                throw new TokenException();
            }
        } catch (UWException e) {
            e.printStackTrace();
            jsonResponse.put(ParameterKey.STATUS, false);
            jsonResponse.put(ParameterKey.MESSAGE, e.getMessage());
            jsonResponse.put(ParameterKey.ERROR, e.getCode());
        }

        return ok(jsonResponse);
    }

    /**
     * Método responsável por retornar os produtos de uma lista de desejos.
     * @return JSON
     */
    public static Result getProductsByWishList() {
        ObjectNode jsonResponse = Json.newObject();
        try {
            User user = authenticateToken();
            if (user != null) {
                if (UserUtil.isAvailable(user)) {
                    JsonNode body = request().body().asJson();
                    if (body != null) {

                        if (body.hasNonNull(ParameterKey.ID)) {
                            Long id = body.get(ParameterKey.ID).asLong();

                            FinderFactory factory = FinderFactory.getInstance();
                            IFinder<Wishlist> finder = factory.get(Wishlist.class);
                            Wishlist wishList = finder.selectUnique(
                                    new String[] { FinderKey.ID },
                                    new Object[] { id });

                            if (wishList != null) {
                                IFinder<WishlistProduct> wishlistProductIFinder = factory.get(WishlistProduct.class);
                                List<WishlistProduct> products = wishlistProductIFinder.selectAll(
                                        new String[] { FinderKey.WISHLIST_ID },
                                        new Object[] { wishList.getId() });

                                List<ObjectNode> arrayProducts = null;
                                if (products != null) {
                                    arrayProducts = new ArrayList<ObjectNode>(products.size() + 5);

                                    for (WishlistProduct wProduct : products) {
                                        Product product = wProduct.getProduct();

                                        long pId = product.getId();
                                        String pName = product.getName();
                                        String pNickName = product.getNickName();

                                        ObjectNode jsonManufacturer = null;
                                        Manufacturer manufacturer = product.getManufacturer();
                                        if (manufacturer != null) {
                                            long mId = manufacturer.getId();
                                            String mName = manufacturer.getName();

                                            jsonManufacturer = Json.newObject();
                                            jsonManufacturer.put(ParameterKey.ID, mId);
                                            jsonManufacturer.put(ParameterKey.NAME, mName);
                                        }

                                        ObjectNode jsonMultimedia = null;
                                        Multimedia multimedia = product.getMultimedia();
                                        if (multimedia != null) {
                                            String fileName = multimedia.getFileName();
                                            String url = multimedia.getUrl();

                                            jsonMultimedia = Json.newObject();
                                            jsonMultimedia.put(ParameterKey.FILENAME, fileName);
                                            jsonMultimedia.put(ParameterKey.URL, url);
                                        }

                                        ObjectNode jsonProduct = Json.newObject();
                                        jsonProduct.put(ParameterKey.ID, pId);
                                        jsonProduct.put(ParameterKey.NAME, pName);
                                        jsonProduct.put(ParameterKey.NICK_NAME, pNickName);

                                        arrayProducts.add(jsonProduct);
                                    }
                                }

                                jsonResponse.put(ParameterKey.STATUS, true);
                                jsonResponse.put(ParameterKey.MESSAGE, "A consulta foi realizada com sucesso.");
                                jsonResponse.put(ParameterKey.PRODUCTS, Json.toJson(arrayProducts));
                            } else {
                                throw new WishListDontExistException();
                            }
                        } else {
                            throw new JSONBodyException();
                        }
                } else {
                    throw new JSONBodyException();
                }
            } else {
                throw new AuthenticationException();
            }
        } else {
            throw new TokenException();
        }
        } catch (UWException e) {
            e.printStackTrace();
            jsonResponse.put(ParameterKey.STATUS, false);
            jsonResponse.put(ParameterKey.MESSAGE, e.getMessage());
            jsonResponse.put(ParameterKey.ERROR, e.getCode());
        }
        return ok(jsonResponse);
    }

    /**
     * Método responsável por 'deletar' a lista de desejos do usuário.
     * Apenas os donos da lista de desejos podem efetuar tal ação.
     * @return JSON
     */
    public static Result delete() {
        ObjectNode jsonResponse = Json.newObject();
        try {
            User user = authenticateToken();
            if (user != null) {
                if (UserUtil.isAvailable(user)) {
                    JsonNode body = request().body().asJson();
                    if (body != null) {
                        if (body.hasNonNull(ParameterKey.ID)) {
                            Long id = body.get(ParameterKey.ID).asLong();

                            FinderFactory factory = FinderFactory.getInstance();
                            IFinder<Wishlist> finder = factory.get(Wishlist.class);
                            Wishlist wishList = finder.selectUnique(new String[] { FinderKey.ID }, new Object[] { id });

                            if (wishList != null && WishListUtil.isOwner(wishList, user)) {
                                String title = wishList.getTitle();
                                wishList.setStatus(Wishlist.Status.REMOVED);
                                wishList.update();

                                jsonResponse.put(ParameterKey.STATUS, true);
                                jsonResponse.put(ParameterKey.MESSAGE, "A lista de desejos (" + title + ") foi excluida com sucesso.");
                            } else {
                                throw new WishListDontExistException();
                            }
                        } else {
                            throw new JSONBodyException();
                        }
                    } else {
                        throw new JSONBodyException();
                    }
                } else {
                    throw new AuthenticationException();
                }
            } else {
                throw new TokenException();
            }
        } catch (UWException e) {
            e.printStackTrace();
            jsonResponse.put(ParameterKey.STATUS, false);
            jsonResponse.put(ParameterKey.MESSAGE, e.getMessage());
            jsonResponse.put(ParameterKey.ERROR, e.getCode());
        }
        return ok(jsonResponse);
    }

    /**
     * Método responsável por adicionar um produto em uma lista de desejos já criada.
     * Apenas os donos da lista de desejos podem efetuar tal ação.
     * @return JSON
     */
    public static Result addProductsWishList() {
        ObjectNode jsonResponse = Json.newObject();
        try {
            User user = authenticateToken();
            if (user != null) {
                if (UserUtil.isAvailable(user)) {
                    JsonNode body = request().body().asJson();
                    if (body != null) {
                        if (body.hasNonNull(ParameterKey.ID) && body.hasNonNull(ParameterKey.PRODUCTS)) {
                            Long idWishList = body.get(ParameterKey.ID).asLong();

                            FinderFactory factory = FinderFactory.getInstance();
                            IFinder<Wishlist> finder = factory.get(Wishlist.class);
                            Wishlist wishList = finder.selectUnique(new String[] { FinderKey.ID }, new Object[] { idWishList });

                            if (wishList != null && WishListUtil.isOwner(wishList, user)) {
                                JsonNode products = body.get(ParameterKey.PRODUCTS);
                                if (products.isArray()) {
                                    int countProductsAdd = 0;
                                    for(int i = 0; i < products.size(); i++) {
                                        JsonNode jsonProduct = products.get(i);

                                        if (jsonProduct.hasNonNull(ParameterKey.NAME)
                                                && jsonProduct.has(ParameterKey.NICK_NAME)
                                                && jsonProduct.hasNonNull(ParameterKey.MANUFACTURER)) {
                                            Manufacturer manufacturer = null;

                                            JsonNode jsonManufacturer = jsonProduct.get(ParameterKey.MANUFACTURER);
                                            if (jsonManufacturer != null && jsonManufacturer.hasNonNull(ParameterKey.NAME)) {
                                                String name = jsonManufacturer.get(ParameterKey.NAME).asText();

                                                manufacturer = new Manufacturer();
                                                manufacturer.setName(name);
                                                manufacturer.save();
                                                manufacturer.refresh();
                                            }

                                            String name = jsonProduct.get(ParameterKey.NAME).asText();
                                            String nickName = jsonProduct.get(ParameterKey.NICK_NAME).asText();

                                            Product product = new Product();
                                            product.setName(name);
                                            product.setNickName(nickName);
                                            product.setManufacturer(manufacturer);
                                            product.save();
                                            product.refresh();

                                            WishlistProduct wishListProduct = new WishlistProduct();
                                            wishListProduct.setProduct(product);
                                            wishListProduct.setWishList(wishList);
                                            wishListProduct.setStatus(WishlistProduct.Status.ACTIVE);
                                            wishListProduct.save();
                                        }
                                    }

                                    jsonResponse.put(ParameterKey.STATUS, true);
                                    jsonResponse.put(ParameterKey.MESSAGE, "Foram adicionados (" + countProductsAdd + ") produtos a sua lista de desejos (" + wishList.getTitle() + ").");
                                } else {
                                    throw new JSONBodyException();
                                }
                            } else {
                                throw new WishListDontExistException();
                            }
                        } else {
                            throw new JSONBodyException();
                        }
                    } else {
                        throw new JSONBodyException();
                    }
                } else {
                    throw new AuthenticationException();
                }
            } else {
                throw new TokenException();
            }
        } catch (UWException e) {
            e.printStackTrace();
            jsonResponse.put(ParameterKey.STATUS, false);
            jsonResponse.put(ParameterKey.MESSAGE, e.getMessage());
            jsonResponse.put(ParameterKey.ERROR, e.getCode());
        }
        return ok(jsonResponse);
    }

    /**
     * Método responsável por remover um produto em uma lista de desejos já criada.
     * Apenas os donos da lista de desejos podem efetuar tal ação.
     * @return JSON
     */
    public static Result removeProductsWishList() {
        ObjectNode jsonResponse = Json.newObject();
        try {
            User user = authenticateToken();
            if (user != null) {
                if (UserUtil.isAvailable(user)) {
                    JsonNode body = request().body().asJson();
                    if (body != null) {
                        if (body.hasNonNull(ParameterKey.ID) && body.hasNonNull(ParameterKey.PRODUCTS)) {
                            Long idWishList = body.get(ParameterKey.ID).asLong();

                            FinderFactory factory = FinderFactory.getInstance();
                            IFinder<Wishlist> finder = factory.get(Wishlist.class);
                            Wishlist wishList = finder.selectUnique(
                                    new String[] { FinderKey.ID },
                                    new Object[] { idWishList });

                            if (wishList != null && WishListUtil.isOwner(wishList, user)) {
                                JsonNode products = body.get(ParameterKey.PRODUCTS);

                                if (products.isArray()) {
                                    IFinder<Product> finderProduct = factory.get(Wishlist.class);
                                    IFinder<WishlistProduct> finderWishListProduct = factory.get(Wishlist.class);

                                    int countProductsRemoved = 0;
                                    for (int i = 0; i < products.size(); i++) {
                                        JsonNode jsonProduct = products.get(i);

                                        if (jsonProduct.hasNonNull(ParameterKey.ID)) {
                                            Long pId = jsonProduct.get(ParameterKey.ID).asLong();
                                            Product product  = finderProduct.selectUnique(
                                                    new String[] { FinderKey.ID },
                                                    new Object[] { pId });

                                            if (product != null && wishList != null) {
                                                WishlistProduct wProduct = finderWishListProduct.selectUnique(
                                                        new String[] { FinderKey.WISHLIST_ID , FinderKey.PRODUCT_ID },
                                                        new Object[] { wishList.getId(), product.getId() });

                                                if (wProduct != null) {
                                                    wProduct.setStatus(WishlistProduct.Status.REMOVED);
                                                    wProduct.update();
                                                    countProductsRemoved++;
                                                }
                                            }
                                        }
                                    }

                                    jsonResponse.put(ParameterKey.STATUS, true);
                                    jsonResponse.put(ParameterKey.MESSAGE, countProductsRemoved + " products removed to wish list " + wishList.getTitle() + ".");
                                } else {
                                    throw new JSONBodyException();
                                }
                            } else {
                                throw new WishListDontExistException();
                            }
                        } else {
                            throw new JSONBodyException();
                        }
                    } else {
                        throw new JSONBodyException();
                    }
                } else {
                    throw new AuthenticationException();
                }
            } else {
                throw new TokenException();
            }
        } catch (UWException e) {
            e.printStackTrace();
            jsonResponse.put(ParameterKey.STATUS, false);
            jsonResponse.put(ParameterKey.MESSAGE, e.getMessage());
            jsonResponse.put(ParameterKey.ERROR, e.getCode());
        }
        return ok(jsonResponse);
    }
}
