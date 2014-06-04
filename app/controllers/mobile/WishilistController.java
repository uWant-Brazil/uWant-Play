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
import scala.util.parsing.json.JSONArray;
import utils.UserUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Cleibson Gomes on 01/06/14.
 *
 * @see 1.0
 */
public class WishilistController extends AbstractApplication {

    public static Result create() {
        ObjectNode jsonResponse = new Json().newObject();

        try {
            JsonNode body = request().body().asJson();
            if (body != null) {
                User user = authenticateToken();
                    if (user != null) {
                        if (UserUtil.isAvailable(user)) {
                            if (body.hasNonNull(ParameterKey.TITLE) && body.hasNonNull(ParameterKey.DESCRIPTION)) {
                                String title = body.get(ParameterKey.TITLE).asText();
                                String description = body.get(ParameterKey.DESCRIPTION).asText();

                                Wishlist wishlist = new Wishlist();
                                wishlist.setTitle(title);
                                wishlist.setDescription(description);
                                wishlist.setUser(user);
                                wishlist.save();
                                wishlist.refresh();

                                if (body.hasNonNull(ParameterKey.PRODUCTS)) {
                                    JsonNode products = Json.parse(body.get(ParameterKey.PRODUCTS).asText());
                                  FinderFactory factory = FinderFactory.getInstance();
                                  IFinder<Manufacturer> finder = factory.get(Manufacturer.class);
                                    if (products.isArray()) {
                                      for(int i = 0; i < products.size(); i++) {

                                          JsonNode product = products.get(i);

                                          if (product.hasNonNull(ParameterKey.NAME) && product.hasNonNull(ParameterKey.NICK_NAME) && product.hasNonNull(ParameterKey.MANUFACTURER)) {

                                              // Buscando Fabricante do produto
                                              Long idManufacturer = product.get(ParameterKey.MANUFACTURER).asLong();
                                              Manufacturer manufacturer = finder.selectUnique(new String[] { FinderKey.ID }, new Object[] { idManufacturer });

                                              if (manufacturer != null) {
                                                  String name = product.get(ParameterKey.NAME).asText();
                                                  String nickName = product.get(ParameterKey.NICK_NAME).asText();

                                                  Product newProduct = new Product();
                                                  newProduct.setName(name);
                                                  newProduct.setNickName(nickName);
                                                  newProduct.setManufacturer(manufacturer);
                                                  newProduct.save();
                                                  newProduct.refresh();

                                                  WishlistProduct wishlistProduct = new WishlistProduct();
                                                  wishlistProduct.setProduct(newProduct);
                                                  wishlistProduct.setWishlist(wishlist);
                                                  wishlistProduct.save();

                                              }
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
                            throw new AuthenticationException();
                        }
                    } else {
                        throw new TokenException();
                    }
            } else {
                throw new JSONBodyException();
            }
        } catch (UWException e) {
            e.printStackTrace();
            jsonResponse.put(ParameterKey.STATUS, false);
            jsonResponse.put(ParameterKey.MESSAGE, e.getMessage());
            jsonResponse.put(ParameterKey.ERROR, e.getCode());
        }

        return ok(jsonResponse);
    }

    public static Result update() {
        ObjectNode jsonResponse = new Json().newObject();

        try {
            JsonNode body = request().body().asJson();
            if (body != null) {
                User user = authenticateToken();
                if (user != null) {
                    if (UserUtil.isAvailable(user)) {
                        if (body.hasNonNull(ParameterKey.ID) && body.hasNonNull(ParameterKey.TITLE) && body.hasNonNull(ParameterKey.DESCRIPTION)) {
                            Long id = body.get(ParameterKey.ID).asLong();
                            String title = body.get(ParameterKey.TITLE).asText();
                            String description = body.get(ParameterKey.DESCRIPTION).asText();

                            FinderFactory factory = FinderFactory.getInstance();
                            IFinder<Wishlist> finder = factory.get(Wishlist.class);
                            Wishlist wishlist = finder.selectUnique(new String[] { FinderKey.ID }, new Object[] { id });

                            if (wishlist != null) {
                                wishlist.setTitle(title);
                                wishlist.setDescription(description);
                                wishlist.setUser(user);
                                wishlist.save();

                                jsonResponse.put(ParameterKey.STATUS, true);
                                jsonResponse.put(ParameterKey.MESSAGE, "Lista de desejo (" + title + ") foi criada com sucesso.");
                            } else {
                                throw new WishlistDoesntExistException();
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
            } else {
                throw new JSONBodyException();
            }
        } catch (UWException e) {
            e.printStackTrace();
            jsonResponse.put(ParameterKey.STATUS, false);
            jsonResponse.put(ParameterKey.MESSAGE, e.getMessage());
            jsonResponse.put(ParameterKey.ERROR, e.getCode());
        }

        return ok(jsonResponse);
    }

    public static Result reload() {
        ObjectNode jsonResponse = new Json().newObject();

        try {
            JsonNode body = request().body().asJson();
            if (body != null) {
                User user = authenticateToken();
                if (user != null) {
                    if (UserUtil.isAvailable(user)) {
                        List<Wishlist> wishlistList = user.getWishlist();
                        if (wishlistList != null) {
                            JsonNode wishlistNode = Json.toJson(wishlistList);

                            jsonResponse.put(ParameterKey.STATUS, true);
                            jsonResponse.put(ParameterKey.MESSAGE, "A consulta foi realizada com sucesso.");
                            jsonResponse.put(ParameterKey.WISHLIST, wishlistNode);
                        } else {
                            throw new WishlistDoesntExistException();
                        }
                    } else {
                        throw new AuthenticationException();
                    }
                } else {
                    throw new TokenException();
                }
            } else {
                throw new JSONBodyException();
            }
        } catch (UWException e) {
            e.printStackTrace();
            jsonResponse.put(ParameterKey.STATUS, false);
            jsonResponse.put(ParameterKey.MESSAGE, e.getMessage());
            jsonResponse.put(ParameterKey.ERROR, e.getCode());
        }

        return ok(jsonResponse);
    }

    public static Result delete() {
        ObjectNode jsonResponse = new Json().newObject();

        try {
            JsonNode body = request().body().asJson();
            if (body != null) {
                User user = authenticateToken();
                if (user != null) {
                    if (UserUtil.isAvailable(user)) {
                        if (body.hasNonNull(ParameterKey.ID)) {
                            Long id = body.get(ParameterKey.ID).asLong();
                            FinderFactory factory = FinderFactory.getInstance();
                            IFinder<Wishlist> finder = factory.get(Wishlist.class);
                            Wishlist wishlist = finder.selectUnique(new String[] { FinderKey.ID }, new Object[] { id });
                            if (wishlist != null) {
                                String title = wishlist.getTitle();
                                wishlist.delete();

                                jsonResponse.put(ParameterKey.STATUS, true);
                                jsonResponse.put(ParameterKey.MESSAGE, "Lista de desejo (" + title + ") foi excluida com sucesso.");
                            } else {
                                throw new WishlistDoesntExistException();
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
            } else {
                throw new JSONBodyException();
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
