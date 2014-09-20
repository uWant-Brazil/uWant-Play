package controllers.mobile;

import com.amazonaws.util.json.JSONArray;
import com.avaje.ebean.text.json.JsonElement;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.AbstractApplication;
import models.classes.*;
import models.database.FinderFactory;
import models.database.IFinder;
import models.exceptions.*;
import play.libs.F;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Security;
import security.MobileAuthenticator;
import utils.ActionUtil;
import utils.UserUtil;
import utils.WishListUtil;

import java.util.*;

/**
 * Controlador responsável pelo tratamento em requisições mobile relacionados a lista de desejos.
 */
@Security.Authenticated(MobileAuthenticator.class)
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

                            WishList wishList = new WishList();
                            wishList.setTitle(title);
                            wishList.setDescription(description);
                            wishList.setUser(user);
                            wishList.setStatus(WishList.Status.ACTIVE);
                            wishList.setUUID(UUID.randomUUID().toString());
                            wishList.save();
                            wishList.refresh();

                            Map<Integer, Long> productIds = fillProducts(body, wishList);

                            wishList.refresh();
                            ActionUtil.feed(wishList);

                            jsonResponse.put(ParameterKey.STATUS, true);
                            jsonResponse.put(ParameterKey.MESSAGE, "Lista de desejo (" + title + ") foi criada com sucesso.");
                            jsonResponse.put(ParameterKey.PRODUCTS, Json.toJson(productIds));
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

    private static Map<Integer, Long> fillProducts(JsonNode body, WishList wishList) {
        Map<Integer, Long> productIds = new HashMap<Integer, Long>(25);
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
                        if (body.hasNonNull(ParameterKey.ID)
                                && body.hasNonNull(ParameterKey.TITLE)
                                && body.hasNonNull(ParameterKey.DESCRIPTION)) {
                            Long id = body.get(ParameterKey.ID).asLong();
                            String title = body.get(ParameterKey.TITLE).asText();
                            String description = body.get(ParameterKey.DESCRIPTION).asText();

                            FinderFactory factory = FinderFactory.getInstance();
                            IFinder<WishList> finder = factory.get(WishList.class);
                            WishList wishList = finder.selectUnique(
                                    new String[] { FinderKey.ID },
                                    new Object[] { id });

                            if (wishList != null && WishListUtil.isOwner(wishList, user)) {
                                WishList wishListUpdated = new WishList();
                                wishListUpdated.setTitle(title);
                                wishListUpdated.setDescription(description);
                                wishListUpdated.update(wishList.getId());
                                wishList.refresh();

                                if (body.hasNonNull(ParameterKey.PRODUCTS_REMOVED)) {
                                    JsonNode jsonElement = body.get(ParameterKey.PRODUCTS_REMOVED);
                                    if (jsonElement.isArray()) {
                                        IFinder<Product> finderProduct = factory.get(Product.class);
                                        for (int i = 0;i < jsonElement.size();i++) {
                                            JsonNode node = jsonElement.get(i);
                                            if (node.hasNonNull(ParameterKey.ID)) {
                                                long productId = node.get(ParameterKey.ID).asLong(0);
                                                Product product = finderProduct.selectUnique(productId);
                                                if (product != null) {
                                                    WishListProduct wp = product.getWishListProducts().get(0);
                                                    WishListProduct wpUpdated = new WishListProduct();
                                                    wpUpdated.setStatus(WishListProduct.Status.REMOVED);
                                                    wpUpdated.update(wp.getId());
                                                }
                                            }
                                        }
                                    }
                                }

                                Map<Integer, Long> productIds = fillProducts(body, wishList);

                                jsonResponse.put(ParameterKey.STATUS, true);
                                jsonResponse.put(ParameterKey.MESSAGE, "A lista de desejos (" + title + ") foi editada com sucesso.");
                                jsonResponse.put(ParameterKey.PRODUCTS, Json.toJson(productIds));
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
                    JsonNode body = request().body().asJson();
                    FinderFactory factory = FinderFactory.getInstance();

                    long userId;
                    if (body != null && body.has(ParameterKey.ID)) {
                        userId = body.get(ParameterKey.ID).asLong();

                        FriendsCircle.FriendshipLevel fsl = UserUtil.getFriendshipLevel(user.getId(), userId);
                        if ((userId != user.getId())
                                && fsl != FriendsCircle.FriendshipLevel.MUTUAL) {
                            throw new UnauthorizedOperationException();
                        }

                        jsonResponse.put(ParameterKey.FRIENDSHIP_LEVEL, fsl.ordinal());
                    } else {
                        userId = user.getId();
                    }

                    IFinder<WishList> finder = factory.get(WishList.class);
                    List<WishList> wishLists = finder.selectAll(
                            new String[] { FinderKey.USER_ID , FinderKey.STATUS },
                            new Object[] { userId, WishList.Status.ACTIVE.ordinal() });

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
                            IFinder<WishList> finder = factory.get(WishList.class);
                            WishList wishList = finder.selectUnique(
                                    new String[] { FinderKey.ID },
                                    new Object[] { id });

                            if (wishList != null) {
                                IFinder<WishListProduct> wishlistProductIFinder = factory.get(WishListProduct.class);
                                List<WishListProduct> products = wishlistProductIFinder.selectAll(
                                        new String[] { FinderKey.WISHLIST_ID, FinderKey.STATUS },
                                        new Object[] { wishList.getId(), WishListProduct.Status.ACTIVE.ordinal() });

                                List<ObjectNode> arrayProducts = null;
                                if (products != null) {
                                    arrayProducts = new ArrayList<ObjectNode>(products.size() + 5);

                                    for (WishListProduct wProduct : products) {
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
                                        jsonProduct.put(ParameterKey.PICTURE, jsonMultimedia);
                                        jsonProduct.put(ParameterKey.MANUFACTURER, jsonManufacturer);

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
                            IFinder<WishList> finder = factory.get(WishList.class);
                            WishList wishList = finder.selectUnique(new String[] { FinderKey.ID }, new Object[] { id });

                            if (wishList != null && WishListUtil.isOwner(wishList, user)) {
                                String title = wishList.getTitle();

                                WishList wishListUpdated = new WishList();
                                wishListUpdated.setStatus(WishList.Status.REMOVED);
                                wishListUpdated.update(wishList.getId());

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
                            IFinder<WishList> finder = factory.get(WishList.class);
                            WishList wishList = finder.selectUnique(new String[] { FinderKey.ID }, new Object[] { idWishList });

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

                                            WishListProduct wishListProduct = new WishListProduct();
                                            wishListProduct.setProduct(product);
                                            wishListProduct.setWishList(wishList);
                                            wishListProduct.setStatus(WishListProduct.Status.ACTIVE);
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
                            IFinder<WishList> finder = factory.get(WishList.class);
                            WishList wishList = finder.selectUnique(
                                    new String[] { FinderKey.ID },
                                    new Object[] { idWishList });

                            if (wishList != null && WishListUtil.isOwner(wishList, user)) {
                                JsonNode products = body.get(ParameterKey.PRODUCTS);

                                if (products.isArray()) {
                                    IFinder<Product> finderProduct = factory.get(WishList.class);
                                    IFinder<WishListProduct> finderWishListProduct = factory.get(WishList.class);

                                    int countProductsRemoved = 0;
                                    for (int i = 0; i < products.size(); i++) {
                                        JsonNode jsonProduct = products.get(i);

                                        if (jsonProduct.hasNonNull(ParameterKey.ID)) {
                                            Long pId = jsonProduct.get(ParameterKey.ID).asLong();
                                            Product product  = finderProduct.selectUnique(
                                                    new String[] { FinderKey.ID },
                                                    new Object[] { pId });

                                            if (product != null && wishList != null) {
                                                WishListProduct wProduct = finderWishListProduct.selectUnique(
                                                        new String[] { FinderKey.WISHLIST_ID , FinderKey.PRODUCT_ID },
                                                        new Object[] { wishList.getId(), product.getId() });

                                                if (wProduct != null) {
                                                    wProduct.setStatus(WishListProduct.Status.REMOVED);
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

    /**
     * Método responsável por editar uma lista de produtos em uma lista de desejos já criada.
     * Apenas os donos da lista de desejos podem efetuar tal ação.
     * @return JSON
     */
    public static F.Promise<Result> editProductsWishList() {
        final ObjectNode jsonResponse = Json.newObject();
        try {
            User user = authenticateToken();
            if (UserUtil.isAvailable(user)) {
                JsonNode body = request().body().asJson();
                if (body != null) {
                    if (body.hasNonNull(ParameterKey.ID) && body.hasNonNull(ParameterKey.PRODUCTS)) {
                        final Long idWishList = body.get(ParameterKey.ID).asLong();

                        final FinderFactory factory = FinderFactory.getInstance();
                        final IFinder<WishList> finder = factory.get(WishList.class);
                        final WishList wishList = finder.selectUnique(
                                new String[] { FinderKey.ID },
                                new Object[] { idWishList });

                        if (wishList != null && WishListUtil.isOwner(wishList, user)) {
                            final JsonNode products = body.get(ParameterKey.PRODUCTS);
                            if (products.isArray()) {
                                F.Promise<Integer> promise = F.Promise.promise(new F.Function0<Integer>() {

                                    @Override
                                    public Integer apply() throws Throwable {
                                        IFinder<Product> finderProduct = factory.get(WishList.class);

                                        int countProductsEdited = 0;
                                        for(int i = 0; i < products.size(); i++) {
                                            JsonNode jsonProduct = products.get(i);

                                            if (jsonProduct.hasNonNull(ParameterKey.ID)
                                                    && jsonProduct.hasNonNull(ParameterKey.NAME)
                                                    && jsonProduct.has(ParameterKey.NICK_NAME)
                                                    && jsonProduct.hasNonNull(ParameterKey.MANUFACTURER)) {
                                                long productId = jsonProduct.get(ParameterKey.ID).asLong();
                                                Product productFounded = finderProduct.selectUnique(
                                                        new String[] { FinderKey.ID, FinderKey.WISHLIST_ID },
                                                        new Object[] { productId, idWishList });

                                                Manufacturer manufacturer = null;

                                                JsonNode jsonManufacturer = jsonProduct.get(ParameterKey.MANUFACTURER);
                                                if (jsonManufacturer != null
                                                        && jsonManufacturer.hasNonNull(ParameterKey.NAME)) {
                                                    if (jsonManufacturer.hasNonNull(ParameterKey.ID)) {
                                                        long manufacturerId = jsonManufacturer.get(ParameterKey.ID).asLong();

                                                        IFinder<Manufacturer> finderManufacturer = factory.get(Manufacturer.class);
                                                        manufacturer = finderManufacturer.selectUnique(manufacturerId);
                                                    }

                                                    String name = jsonManufacturer.get(ParameterKey.NAME).asText();

                                                    if (manufacturer == null) {
                                                        manufacturer = new Manufacturer();
                                                        manufacturer.setName(name);
                                                        manufacturer.save();
                                                    } else {
                                                        manufacturer.setName(name);
                                                        manufacturer.update();
                                                    }
                                                    manufacturer.refresh();
                                                }

                                                String name = jsonProduct.get(ParameterKey.NAME).asText();
                                                String nickName = jsonProduct.get(ParameterKey.NICK_NAME).asText();

                                                productFounded.setName(name);
                                                productFounded.setNickName(nickName);
                                                productFounded.setManufacturer(manufacturer);
                                                productFounded.update();

                                                countProductsEdited++;
                                            }
                                        }
                                        return countProductsEdited;
                                    }

                                });

                                return promise.map(new F.Function<Integer, Result>() {

                                    @Override
                                    public Result apply(Integer countProductsEdited) throws Throwable {
                                        jsonResponse.put(ParameterKey.STATUS, true);
                                        jsonResponse.put(ParameterKey.MESSAGE, countProductsEdited + " produtos foram editados na lista " + wishList.getTitle() + " com sucesso.");
                                        return ok(jsonResponse);
                                    }

                                });
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
                throw new UserDoesntExistException();
            }
        } catch (UWException e) {
            e.printStackTrace();
            jsonResponse.put(ParameterKey.STATUS, false);
            jsonResponse.put(ParameterKey.MESSAGE, e.getMessage());
            jsonResponse.put(ParameterKey.ERROR, e.getCode());
        }

        return F.Promise.promise(new F.Function0<Result>() {

            @Override
            public Result apply() throws Throwable {
                return ok(jsonResponse);
            }

        });
    };

}
