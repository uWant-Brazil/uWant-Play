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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Cleibson Gomes on 01/06/14.
 *
 * @see 1.0
 */
public class WishListController extends AbstractApplication {

    public static Result create() {
        ObjectNode jsonResponse = Json.newObject();

        try {
            JsonNode body = request().body().asJson();
            if (body != null) {
                User user = authenticateToken();
                    if (user != null) {
                        if (UserUtil.isAvailable(user)) {
                            if (body.hasNonNull(ParameterKey.TITLE) && body.hasNonNull(ParameterKey.DESCRIPTION)) {
                                String title = body.get(ParameterKey.TITLE).asText();
                                String description = body.get(ParameterKey.DESCRIPTION).asText();

                                WishList wishList = new WishList();
                                wishList.setTitle(title);
                                wishList.setDescription(description);
                                wishList.setUser(user);
                                wishList.save();
                                wishList.refresh();

                                if (body.hasNonNull(ParameterKey.PRODUCTS)) {
                                  JsonNode products = body.get(ParameterKey.PRODUCTS);
                                    if (products.isArray()) {
                                      for(int i = 0; i < products.size(); i++) {

                                          JsonNode product = products.get(i);

                                          if (product.hasNonNull(ParameterKey.NAME) && product.has(ParameterKey.NICK_NAME) && product.hasNonNull(ParameterKey.MANUFACTURER)) {

                                              // criando frabicante do produto
                                              JsonNode manufacturerJson = product.get(ParameterKey.MANUFACTURER);
                                              Manufacturer manufacturer = null;

                                              if (manufacturerJson.hasNonNull(ParameterKey.NAME)) {
                                                  String nameFacturer = manufacturerJson.get(ParameterKey.NAME).asText();
                                                  manufacturer = new Manufacturer();
                                                  manufacturer.setName(nameFacturer);
                                                  manufacturer.save();
                                                  manufacturer.refresh();

                                                  if (manufacturer != null) {
                                                      String name = product.get(ParameterKey.NAME).asText();
                                                      String nickName = product.get(ParameterKey.NICK_NAME).asText();

                                                      Product newProduct = new Product();
                                                      newProduct.setName(name);
                                                      newProduct.setNickName(nickName == null ? "" : nickName);
                                                      newProduct.setManufacturer(manufacturer);
                                                      newProduct.save();
                                                      newProduct.refresh();

                                                      WishListProduct wishListProduct = new WishListProduct();
                                                      wishListProduct.setProduct(newProduct);
                                                      wishListProduct.setWishList(wishList);
                                                      wishListProduct.setStatus(WishListProduct.Status.ACTIVE);
                                                      wishListProduct.save();

                                                  }
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
        ObjectNode jsonResponse = Json.newObject();

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
                            IFinder<WishList> finder = factory.get(WishList.class);
                            WishList wishList = finder.selectUnique(new String[] { FinderKey.ID }, new Object[] { id });

                            if (wishList != null) {
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

    public static Result list() {
        ObjectNode jsonResponse = Json.newObject();

        try {
            JsonNode body = request().body().asJson();
            if (body != null) {
                User user = authenticateToken();
                if (user != null) {
                    if (UserUtil.isAvailable(user)) {
                        FinderFactory factory = FinderFactory.getInstance();
                        IFinder<WishList> finder = factory.get(WishList.class);
                        List<WishList> wishList = finder.selectAll(
                                new String[] { FinderKey.USER_ID , FinderKey.STATUS},
                                new Object[] { user.getId(), WishList.Status.ACTIVE.ordinal() });
                        if (wishList != null) {
                            jsonResponse.put(ParameterKey.STATUS, true);
                            jsonResponse.put(ParameterKey.MESSAGE, "A consulta foi realizada com sucesso.");
                            jsonResponse.put(ParameterKey.WISHLIST, Json.toJson(wishList));
                        } else {
                            throw new WishListDontExistException();
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

    public static Result getProductsByWishList() {
        ObjectNode jsonResponse = Json.newObject();

        try {
            JsonNode body = request().body().asJson();
            if (body != null) {
                User user = authenticateToken();
                if (user != null) {
                    if (UserUtil.isAvailable(user)) {

                        if (body.hasNonNull(ParameterKey.ID)) {
                            Long id = body.get(ParameterKey.ID).asLong();
                            FinderFactory factory = FinderFactory.getInstance();
                            IFinder<WishList> finder = factory.get(WishList.class);
                            WishList wishList = finder.selectUnique(new String[] { FinderKey.ID }, new Object[] { id });
                            if (wishList != null) {
                                IFinder<WishListProduct> wishlistProductIFinder = factory.get(WishListProduct.class);
                                List<WishListProduct> wishListProducts = wishlistProductIFinder.selectAll(
                                        new String[] { "wishlist_id" },
                                        new Object[] { wishList.getId() }
                                );

                                if (wishListProducts != null) {
                                    jsonResponse.put(ParameterKey.STATUS, true);
                                    jsonResponse.put(ParameterKey.MESSAGE, "A consulta foi realizada com sucesso.");
                                    jsonResponse.put(ParameterKey.WISHLIST, Json.toJson(wishList));
                                } else {
                                    jsonResponse.put(ParameterKey.STATUS, false);
                                    jsonResponse.put(ParameterKey.MESSAGE, "Não existe produtos vinculados a lista de desejo: " + wishList.getTitle());
                                }

                            } else {
                                throw new WishListDontExistException();
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

    public static Result delete() {
        ObjectNode jsonResponse = Json.newObject();

        try {
            JsonNode body = request().body().asJson();
            if (body != null) {
                User user = authenticateToken();
                if (user != null) {
                    if (UserUtil.isAvailable(user)) {
                        if (body.hasNonNull(ParameterKey.ID)) {
                            Long id = body.get(ParameterKey.ID).asLong();
                            FinderFactory factory = FinderFactory.getInstance();
                            IFinder<WishList> finder = factory.get(WishList.class);
                            WishList wishList = finder.selectUnique(new String[] { FinderKey.ID }, new Object[] { id });
                            if (wishList != null) {
                                String title = wishList.getTitle();
                                wishList.delete();

                                jsonResponse.put(ParameterKey.STATUS, true);
                                jsonResponse.put(ParameterKey.MESSAGE, "Lista de desejo (" + title + ") foi excluida com sucesso.");
                            } else {
                                throw new WishListDontExistException();
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

    public static Result addProductsWishList() {
        ObjectNode jsonResponse = Json.newObject();

        try {
            JsonNode body = request().body().asJson();
            int countProductsAdd = 0;

            if (body != null) {
                User user = authenticateToken();
                if (user != null) {
                    if (UserUtil.isAvailable(user)) {

                        if (body.hasNonNull(ParameterKey.ID) && body.hasNonNull(ParameterKey.PRODUCTS)) {

                            Long idWishList = body.get(ParameterKey.ID).asLong();
                            FinderFactory factory = FinderFactory.getInstance();
                            IFinder<WishList> finder = factory.get(WishList.class);
                            WishList wishList = finder.selectUnique(new String[] { FinderKey.ID }, new Object[] { idWishList });

                            List<WishListProduct> wishListProducts = null;
                            if (wishList != null) {

                                if (wishListProducts == null) new ArrayList<WishListProduct>();
                                JsonNode products = body.get(ParameterKey.PRODUCTS);

                                if (products.isArray()) {
                                    for(int i = 0; i < products.size(); i++) {

                                        JsonNode product = products.get(i);

                                        if (product.hasNonNull(ParameterKey.NAME)
                                                && product.has(ParameterKey.NICK_NAME)
                                                && product.hasNonNull(ParameterKey.MANUFACTURER)) {

                                            JsonNode manufacturerJson = product.get(ParameterKey.MANUFACTURER);
                                            Manufacturer manufacturer = null;

                                                if (manufacturerJson.hasNonNull(ParameterKey.NAME)) {
                                                    String nameFacturer = manufacturerJson.get(ParameterKey.NAME).asText();
                                                    manufacturer = new Manufacturer();
                                                    manufacturer.setName(nameFacturer);
                                                    manufacturer.save();
                                                    manufacturer.refresh();

                                                    if (manufacturer != null) {
                                                        String name = product.get(ParameterKey.NAME).asText();
                                                        String nickName = product.get(ParameterKey.NICK_NAME).asText();

                                                        Product newProduct = new Product();
                                                        newProduct.setName(name);
                                                        newProduct.setNickName(nickName == null ? "" : nickName);
                                                        newProduct.setManufacturer(manufacturer);
                                                        newProduct.save();
                                                        newProduct.refresh();

                                                        WishListProduct wishListProduct = new WishListProduct();
                                                        wishListProduct.setProduct(newProduct);
                                                        wishListProduct.setWishList(wishList);
                                                        wishListProduct.setStatus(WishListProduct.Status.ACTIVE);
                                                        wishListProduct.save();
                                                        countProductsAdd++;
                                                }
                                            }
                                        }

                                    }

                                    jsonResponse.put(ParameterKey.STATUS, true);
                                    jsonResponse.put(ParameterKey.MESSAGE, countProductsAdd + " products added to wish list " + wishList.getTitle() + ".");
                                } else {
                                    jsonResponse.put(ParameterKey.STATUS, false);
                                    jsonResponse.put(ParameterKey.MESSAGE, "Produtos não encontrados");
                                }

                            } else {
                                jsonResponse.put(ParameterKey.STATUS, false);
                                jsonResponse.put(ParameterKey.MESSAGE, "Lista de desejo não existe");
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

    public static Result removeProductsWishList() {
        ObjectNode jsonResponse = Json.newObject();

        try {
            JsonNode body = request().body().asJson();
            int countProductsRemoved = 0;

            if (body != null) {
                User user = authenticateToken();
                if (user != null) {
                    if (UserUtil.isAvailable(user)) {

                        if (body.hasNonNull(ParameterKey.ID) && body.hasNonNull(ParameterKey.PRODUCTS)) {

                            Long idWishList = body.get(ParameterKey.ID).asLong();
                            FinderFactory factory = FinderFactory.getInstance();
                            IFinder<WishList> finder = factory.get(WishList.class);
                            WishList wishList = finder.selectUnique(new String[] { FinderKey.ID }, new Object[] { idWishList });

                            if (wishList != null) {
                                JsonNode products = body.get(ParameterKey.PRODUCTS);

                                if (products.isArray()) {
                                    IFinder<Product> finderProduct = factory.get(WishList.class);
                                    IFinder<WishListProduct> finderWishListProduct = factory.get(WishList.class);
                                    for(int i = 0; i < products.size(); i++) {

                                        JsonNode product = products.get(i);

                                        if (product.hasNonNull(ParameterKey.ID)) {
                                            Long idProduct = product.get(ParameterKey.ID).asLong();
                                            Product productIndex  = finderProduct.selectUnique(new String[] { FinderKey.ID }, new Object[] { idProduct });

                                            if (productIndex != null && wishList != null) {

                                                WishListProduct wishListProduct = finderWishListProduct.selectUnique(
                                                        new String[] { "wishlist_id" , "product_id"},
                                                        new Object[] { wishList.getId(), productIndex.getId()});

                                                if (wishListProduct != null) {
                                                    wishListProduct.setStatus(WishListProduct.Status.REMOVED);
                                                    wishListProduct.save();
                                                    countProductsRemoved++;
                                                }

                                            }
                                        }
                                    }

                                    jsonResponse.put(ParameterKey.STATUS, true);
                                    jsonResponse.put(ParameterKey.MESSAGE, countProductsRemoved + " products removed to wish list " + wishList.getTitle() + ".");
                                } else {
                                    jsonResponse.put(ParameterKey.STATUS, false);
                                    jsonResponse.put(ParameterKey.MESSAGE, "Produtos não encontrados");
                                }

                            } else {
                                jsonResponse.put(ParameterKey.STATUS, false);
                                jsonResponse.put(ParameterKey.MESSAGE, "Lista de desejo não existe");
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
