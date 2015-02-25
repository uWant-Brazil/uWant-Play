package controllers.mobile;

import com.avaje.ebean.Expr;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.AbstractApplication;
import models.classes.*;
import models.database.FinderFactory;
import models.database.IFinder;
import models.exceptions.*;
import play.db.ebean.Model;
import play.db.ebean.Transactional;
import play.i18n.Messages;
import play.libs.F;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Security;
import security.MobileAuthenticator;
import utils.ActionUtil;
import utils.UserUtil;
import utils.WishListUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
    @Transactional
    public static F.Promise<Result> create() {
        return F.Promise.<Result>promise(() -> {
            ObjectNode jsonResponse = Json.newObject();
            try {
                User user = authenticateToken();
                if (user != null) {
                    if (UserUtil.isAvailable(user)) {
                        JsonNode body = request().body().asJson();
                        if (body != null) {
                            if (body.hasNonNull(ParameterKey.TITLE)
                                    && body.hasNonNull(ParameterKey.DESCRIPTION)) {
                                String title = body.get(ParameterKey.TITLE).asText();
                                String description = body.get(ParameterKey.DESCRIPTION).asText();

                                WishList wishList = new WishList();
                                wishList.setTitle(title);
                                wishList.setDescription(description);
                                wishList.setUser(user);
                                wishList.setStatus(WishList.Status.ACTIVE);
                                wishList.setUUID(UUID.randomUUID().toString());
                                wishList.save();

                                jsonResponse.put(ParameterKey.STATUS, true);
                                jsonResponse.put(ParameterKey.MESSAGE, Messages.get(MessageKey.WishList.CREATE_SUCCESS));
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
        });
    }

    /**
     * Método responsável pela atualização de uma lista de desejos - sem alterar os produtos na mesma.
     * Apenas os donos da lista de desejos podem efetuar tal ação.
     * @return JSON
     */
    @Transactional
    public static F.Promise<Result> update() {
        return F.Promise.<Result>promise(() -> {
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
                                        new String[]{FinderKey.ID},
                                        new Object[]{id});

                                if (wishList != null && WishListUtil.isOwner(wishList, user)) {
                                    WishList wishListUpdated = new WishList();
                                    wishListUpdated.setTitle(title);
                                    wishListUpdated.setDescription(description);
                                    wishListUpdated.update(wishList.getId());

                                    jsonResponse.put(ParameterKey.STATUS, true);
                                    jsonResponse.put(ParameterKey.MESSAGE, Messages.get(MessageKey.WishList.UPDATE_SUCCESS, title));
                                } else {
                                    throw new WishListDoesntExistException();
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
        });
    }

    /**
     * Método responsável pela listagem de uma lista de desejos do usuário.
     * Os produtos deverão ser buscado em outra requsição - <link>products()</link>
     * @return JSON
     */
    public static F.Promise<Result> list() {
        return F.Promise.<Result>promise(() -> {
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
                                new String[]{FinderKey.USER_ID, FinderKey.STATUS},
                                new Object[]{userId, WishList.Status.ACTIVE.ordinal()});

                        if (wishLists != null) {
                            jsonResponse.put(ParameterKey.STATUS, true);
                            jsonResponse.put(ParameterKey.MESSAGE, Messages.get(MessageKey.WishList.LIST_SUCCESS));
                            jsonResponse.put(ParameterKey.WISHLIST, Json.toJson(wishLists));
                        } else {
                            throw new WishListDoesntExistException();
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
        });
    }

    /**
     * Método responsável por retornar os produtos de uma lista de desejos.
     * @return JSON
     */
    public static F.Promise<Result> products() {
        return F.Promise.<Result>promise(() -> {
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
                                        new String[]{FinderKey.ID},
                                        new Object[]{id});

                                if (wishList != null && WishListUtil.isOwner(wishList, user)) {
                                    IFinder<WishListProduct> wlFinder = factory.get(WishListProduct.class);
                                    List<WishListProduct> products = wlFinder.selectAll(
                                            new String[]{FinderKey.WISHLIST_ID, FinderKey.STATUS},
                                            new Object[]{wishList.getId(), WishListProduct.Status.ACTIVE.ordinal()});

                                    List<ObjectNode> arrayProducts = null;
                                    if (products != null) {
                                        arrayProducts = new ArrayList<>(products.size() + 5);

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
                                    jsonResponse.put(ParameterKey.MESSAGE, Messages.get(MessageKey.WishList.PRODUCTS_SUCCESS));
                                    jsonResponse.put(ParameterKey.PRODUCTS, Json.toJson(arrayProducts));
                                } else {
                                    throw new WishListDoesntExistException();
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
        });
    }

    public static F.Promise<Result> productsSave() {
        return F.Promise.<Result>promise(() -> {
            ObjectNode jsonResponse = Json.newObject();
            try {
                User user = authenticateToken();
                if (user != null) {
                    if (UserUtil.isAvailable(user)) {
                        JsonNode body = request().body().asJson();
                        if (body != null) {
                            if (body.hasNonNull(ParameterKey.ID)) {
                                long id = body.get(ParameterKey.ID).asLong();

                                FinderFactory factory = FinderFactory.getInstance();
                                IFinder<WishList> finder = factory.get(WishList.class);
                                WishList wishList = finder.selectUnique(
                                        new String[]{FinderKey.ID},
                                        new Object[]{id});

                                if (wishList != null && WishListUtil.isOwner(wishList, user)) {
                                    WishListUtil.fillProducts(body, wishList);
                                    ActionUtil.feed(wishList);
                                } else {
                                    throw new WishListDoesntExistException();
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
                    throw new AuthenticationException();
                }
            } catch (UWException e) {
                e.printStackTrace();
                jsonResponse.put(ParameterKey.STATUS, false);
                jsonResponse.put(ParameterKey.MESSAGE, e.getMessage());
                jsonResponse.put(ParameterKey.ERROR, e.getCode());
            }

            return ok(jsonResponse);
        });
    }

    /**
     * Método responsável por 'deletar' a lista de desejos do usuário.
     * Apenas os donos da lista de desejos podem efetuar tal ação.
     * @return JSON
     */
    @Transactional
    public static F.Promise<Result> delete() {
        return F.Promise.<Result>promise(() -> {
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
                                        new String[]{FinderKey.ID},
                                        new Object[]{id});

                                if (wishList != null && WishListUtil.isOwner(wishList, user)) {
                                    String title = wishList.getTitle();

                                    WishList wishListUpdated = new WishList();
                                    wishListUpdated.setStatus(WishList.Status.REMOVED);
                                    wishListUpdated.update(wishList.getId());

                                    jsonResponse.put(ParameterKey.STATUS, true);
                                    jsonResponse.put(ParameterKey.MESSAGE, Messages.get(MessageKey.WishList.DELETE_SUCCESS, title));
                                } else {
                                    throw new WishListDoesntExistException();
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
        });
    }

}
