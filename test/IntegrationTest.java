import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.AbstractApplication;
import models.classes.SocialProfile;
import models.classes.Token;
import models.classes.User;
import models.classes.WishList;
import models.database.FinderFactory;
import models.database.IFinder;
import org.junit.Test;
import play.core.j.JavaResultExtractor;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.SimpleResult;
import play.test.FakeRequest;
import utils.DateUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.*;

public class IntegrationTest {

    @Test
    public void successfulRegisterTest() {
        running(fakeApplication(), new Runnable() {

            @Override
            public void run() {
                Calendar now = Calendar.getInstance();
                now.set(Calendar.YEAR, (1930 + (int)(Math.random() * (now.get(Calendar.YEAR) - 1930) + 1)));
                SimpleDateFormat formatter = new SimpleDateFormat(DateUtil.DATE_PATTERN);
                String birthday = formatter.format(now.getTime());

                int gender = (now.getTimeInMillis() % 8 < 5 ? User.Gender.MALE.ordinal() : User.Gender.FEMALE.ordinal());

                String uniqueIdentifier = UUID.randomUUID().toString();

                ObjectNode body = Json.newObject();
                body.put(AbstractApplication.ParameterKey.LOGIN, "test" + uniqueIdentifier);
                body.put(AbstractApplication.ParameterKey.PASSWORD, uniqueIdentifier);
                body.put(AbstractApplication.ParameterKey.FULL_NAME, "Teste " + uniqueIdentifier);
                body.put(AbstractApplication.ParameterKey.BIRTHDAY, birthday);
                body.put(AbstractApplication.ParameterKey.GENDER, gender);
                body.put(AbstractApplication.ParameterKey.MAIL, "mail-" + uniqueIdentifier + "@uwant-test.com.br");

                FakeRequest fakeRequest = new FakeRequest(POST, "/v1/mobile/user/register").withJsonBody(body);
                Result result = route(fakeRequest);

                boolean status = (status(result) == Http.Status.OK);

                assertThat(result).isNotNull();
                assertThat(status).isTrue();

                String responseBody = new String(JavaResultExtractor.getBody((SimpleResult) result));
                JsonNode jsonResponse = Json.parse(responseBody);

                assertStatusMessage(jsonResponse, status);
            }

        });
    }

    @Test
    public void successfulRegisterWithSocialTest() {
        running(fakeApplication(), new Runnable() {

            @Override
            public void run() {
                Calendar now = Calendar.getInstance();
                now.set(Calendar.YEAR, (1930 + (int)(Math.random() * (now.get(Calendar.YEAR) - 1930) + 1)));
                SimpleDateFormat formatter = new SimpleDateFormat(DateUtil.DATE_PATTERN);
                String birthday = formatter.format(now.getTime());

                int gender = (now.getTimeInMillis() % 8 < 5 ? User.Gender.MALE.ordinal() : User.Gender.FEMALE.ordinal());

                String uniqueIdentifier = UUID.randomUUID().toString();

                ObjectNode body = Json.newObject();
                body.put(AbstractApplication.ParameterKey.LOGIN, "test" + uniqueIdentifier);
                body.put(AbstractApplication.ParameterKey.PASSWORD, uniqueIdentifier);
                body.put(AbstractApplication.ParameterKey.FULL_NAME, "Teste " + uniqueIdentifier);
                body.put(AbstractApplication.ParameterKey.BIRTHDAY, birthday);
                body.put(AbstractApplication.ParameterKey.GENDER, gender);
                body.put(AbstractApplication.ParameterKey.MAIL, "mail-" + uniqueIdentifier + "@uwant-test.com.br");

                ObjectNode jsonSocialProfile = Json.newObject();
                jsonSocialProfile.put(AbstractApplication.ParameterKey.SOCIAL_PROVIDER, SocialProfile.Provider.FACEBOOK.ordinal());
                jsonSocialProfile.put(AbstractApplication.ParameterKey.TOKEN, uniqueIdentifier);

                body.put(AbstractApplication.ParameterKey.SOCIAL_PROFILE, jsonSocialProfile);

                FakeRequest fakeRequest = new FakeRequest(POST, "/v1/mobile/user/register").withJsonBody(body);
                Result result = route(fakeRequest);

                boolean status = (status(result) == Http.Status.OK);

                assertThat(result).isNotNull();
                assertThat(status).isTrue();

                String responseBody = new String(JavaResultExtractor.getBody((SimpleResult) result));
                JsonNode jsonResponse = Json.parse(responseBody);

                assertStatusMessage(jsonResponse, status);
            }

        });
    }

    @Test
    public void successfulLoginTest() {
        running(fakeApplication(), new Runnable() {

            @Override
            public void run() {
                FinderFactory factory = FinderFactory.getInstance();
                IFinder<User> finder = factory.get(User.class);
                User user = finder.selectUnique(Long.valueOf(1));

                String login = user.getLogin();
                String password = user.getPassword();

                ObjectNode body = Json.newObject();
                body.put(AbstractApplication.ParameterKey.LOGIN, login);
                body.put(AbstractApplication.ParameterKey.PASSWORD, password);

                FakeRequest fakeRequest = new FakeRequest(POST, "/v1/mobile/authorize").withJsonBody(body);
                Result result = route(fakeRequest);

                boolean status = (status(result) == Http.Status.OK);

                assertThat(result).isNotNull();
                assertThat(status).isTrue();

                assertAuthenticationHeader(result);

                String responseBody = new String(JavaResultExtractor.getBody((SimpleResult) result));
                JsonNode jsonResponse = Json.parse(responseBody);

                assertStatusMessage(jsonResponse, status);
            }

        });
    }

    @Test
    public void successfulLogoffTest() {
        running(fakeApplication(), new Runnable() {

            @Override
            public void run() {
                FinderFactory factory = FinderFactory.getInstance();
                IFinder<User> finder = factory.get(User.class);
                User user = finder.selectUnique(Long.valueOf(1));
                Token token = user.getToken();

                FakeRequest fakeRequest = new FakeRequest(POST, "/v1/mobile/logoff")
                        .withHeader(AbstractApplication.HeaderKey.HEADER_AUTHENTICATION_TOKEN, token.getContent());
                Result result = route(fakeRequest);

                boolean status = (status(result) == Http.Status.OK);

                assertThat(result).isNotNull();
                assertThat(status).isTrue();

                String responseBody = new String(JavaResultExtractor.getBody((SimpleResult) result));
                JsonNode jsonResponse = Json.parse(responseBody);

                assertStatusMessage(jsonResponse, status);
            }

        });
    }

    @Test
    public void successfulMailUserSearchTest() {
        running(fakeApplication(), new Runnable() {

            @Override
            public void run() {
                FinderFactory factory = FinderFactory.getInstance();
                IFinder<User> finder = factory.get(User.class);
                User user = finder.selectUnique(Long.valueOf(1));
                Token token = user.getToken();

                ObjectNode body = Json.newObject();
                body.put(AbstractApplication.ParameterKey.QUERY, user.getMail());

                FakeRequest fakeRequest = new FakeRequest(POST, "/v1/mobile/user/search")
                        .withHeader(AbstractApplication.HeaderKey.HEADER_AUTHENTICATION_TOKEN, token.getContent());
                Result result = route(fakeRequest);

                boolean status = (status(result) == Http.Status.OK);

                assertThat(result).isNotNull();
                assertThat(status).isTrue();

                String responseBody = new String(JavaResultExtractor.getBody((SimpleResult) result));
                JsonNode jsonResponse = Json.parse(responseBody);

                assertStatusMessage(jsonResponse, status);

                assertThat(jsonResponse.has(AbstractApplication.ParameterKey.USERS)).isTrue();

                JsonNode jsonUsers = jsonResponse.get(AbstractApplication.ParameterKey.USERS);
                assertThat(jsonUsers.isArray()).isTrue();

                for (int i = 0;i < jsonUsers.size();i++) {
                    JsonNode jsonUser = jsonUsers.get(i);
                    assertThat(jsonUser).isNotNull();
                    assertThat(jsonUser.hasNonNull(AbstractApplication.ParameterKey.LOGIN)).isTrue();
                    assertThat(jsonUser.hasNonNull(AbstractApplication.ParameterKey.MAIL)).isTrue();
                }
            }

        });
    }

    @Test
    public void successfulLoginUserSearchTest() {
        running(fakeApplication(), new Runnable() {

            @Override
            public void run() {
                FinderFactory factory = FinderFactory.getInstance();
                IFinder<User> finder = factory.get(User.class);
                User user = finder.selectUnique(Long.valueOf(1));
                Token token = user.getToken();

                ObjectNode body = Json.newObject();
                body.put(AbstractApplication.ParameterKey.QUERY, user.getLogin());

                FakeRequest fakeRequest = new FakeRequest(POST, "/v1/mobile/user/search")
                        .withHeader(AbstractApplication.HeaderKey.HEADER_AUTHENTICATION_TOKEN, token.getContent());
                Result result = route(fakeRequest);

                boolean status = (status(result) == Http.Status.OK);

                assertThat(result).isNotNull();
                assertThat(status).isTrue();

                String responseBody = new String(JavaResultExtractor.getBody((SimpleResult) result));
                JsonNode jsonResponse = Json.parse(responseBody);

                assertStatusMessage(jsonResponse, status);

                assertThat(jsonResponse.has(AbstractApplication.ParameterKey.USERS)).isTrue();

                JsonNode jsonUsers = jsonResponse.get(AbstractApplication.ParameterKey.USERS);
                assertThat(jsonUsers.isArray()).isTrue();

                for (int i = 0;i < jsonUsers.size();i++) {
                    JsonNode jsonUser = jsonUsers.get(i);
                    assertThat(jsonUser).isNotNull();
                    assertThat(jsonUser.hasNonNull(AbstractApplication.ParameterKey.LOGIN)).isTrue();
                    assertThat(jsonUser.hasNonNull(AbstractApplication.ParameterKey.MAIL)).isTrue();
                }
            }

        });
    }

    @Test
    public void successfulNameUserSearchTest() {
        running(fakeApplication(), new Runnable() {

            @Override
            public void run() {
                FinderFactory factory = FinderFactory.getInstance();
                IFinder<User> finder = factory.get(User.class);
                User user = finder.selectUnique(Long.valueOf(1));
                Token token = user.getToken();

                ObjectNode body = Json.newObject();
                body.put(AbstractApplication.ParameterKey.QUERY, user.getName());

                FakeRequest fakeRequest = new FakeRequest(POST, "/v1/mobile/user/search")
                        .withHeader(AbstractApplication.HeaderKey.HEADER_AUTHENTICATION_TOKEN, token.getContent());
                Result result = route(fakeRequest);

                boolean status = (status(result) == Http.Status.OK);

                assertThat(result).isNotNull();
                assertThat(status).isTrue();

                String responseBody = new String(JavaResultExtractor.getBody((SimpleResult) result));
                JsonNode jsonResponse = Json.parse(responseBody);

                assertStatusMessage(jsonResponse, status);

                assertThat(jsonResponse.has(AbstractApplication.ParameterKey.USERS)).isTrue();

                JsonNode jsonUsers = jsonResponse.get(AbstractApplication.ParameterKey.USERS);
                assertThat(jsonUsers.isArray()).isTrue();

                for (int i = 0;i < jsonUsers.size();i++) {
                    JsonNode jsonUser = jsonUsers.get(i);
                    assertThat(jsonUser).isNotNull();
                    assertThat(jsonUser.hasNonNull(AbstractApplication.ParameterKey.LOGIN)).isTrue();
                    assertThat(jsonUser.hasNonNull(AbstractApplication.ParameterKey.MAIL)).isTrue();
                }
            }

        });
    }

    @Test
    public void successfulLoginWithSocialTest() {
        running(fakeApplication(), new Runnable() {

            @Override
            public void run() {
                FinderFactory factory = FinderFactory.getInstance();
                IFinder<User> finder = factory.get(User.class);
                User user = finder.selectUnique(Long.valueOf(1));

                String login = user.getLogin();
                String password = user.getPassword();

                ObjectNode body = Json.newObject();
                body.put(AbstractApplication.ParameterKey.LOGIN, login);
                body.put(AbstractApplication.ParameterKey.PASSWORD, password);

                long i = System.currentTimeMillis();

                ObjectNode jsonSocial = Json.newObject();
                jsonSocial.put(AbstractApplication.ParameterKey.SOCIAL_PROVIDER, (i % 2 == 0 ? SocialProfile.Provider.FACEBOOK.ordinal() : (i % 3 == 0 ? SocialProfile.Provider.GOOGLE_PLUS.ordinal() : SocialProfile.Provider.TWITTER.ordinal())));
                jsonSocial.put(AbstractApplication.ParameterKey.TOKEN, UUID.randomUUID().toString());

                if (i % 2 == 0)
                    jsonSocial.put(AbstractApplication.ParameterKey.LOGIN, user.getMail());

                FakeRequest fakeRequest = new FakeRequest(POST, "/v1/mobile/social/signUp").withJsonBody(body);
                Result result = route(fakeRequest);

                boolean status = (status(result) == Http.Status.OK);

                assertThat(result).isNotNull();
                assertThat(status).isTrue();

                assertAuthenticationHeader(result);

                String responseBody = new String(JavaResultExtractor.getBody((SimpleResult) result));
                JsonNode jsonResponse = Json.parse(responseBody);

                assertStatusMessage(jsonResponse, status);

                JsonNode nodeMessage = jsonResponse.get(AbstractApplication.ParameterKey.REGISTERED);
                assertThat(nodeMessage.asBoolean()).isNotNull().isTrue();
            }

        });
    }

    @Test
    public void successfulRecoveryPassword() {
        running(fakeApplication(), new Runnable() {

            @Override
            public void run() {
                FinderFactory factory = FinderFactory.getInstance();
                IFinder<User> finder = factory.get(User.class);
                User user = finder.selectUnique(Long.valueOf(1));

                String login = user.getLogin();
                String password = user.getPassword();

                ObjectNode body = Json.newObject();
                body.put(AbstractApplication.ParameterKey.LOGIN, login);
                body.put(AbstractApplication.ParameterKey.PASSWORD, password);

                FakeRequest fakeRequest = new FakeRequest(POST, "/v1/mobile/recoveryPassword").withJsonBody(body);
                Result result = route(fakeRequest);

                boolean status = (status(result) == Http.Status.OK);

                assertThat(result).isNotNull();
                assertThat(status).isTrue();

                String responseBody = new String(JavaResultExtractor.getBody((SimpleResult) result));
                JsonNode jsonResponse = Json.parse(responseBody);

                assertStatusMessage(jsonResponse, status);
            }

        });
    }

    @Test
    public void successfulExcludeAccountTest() {
        running(fakeApplication(), new Runnable() {

            @Override
            public void run() {
                FinderFactory factory = FinderFactory.getInstance();
                IFinder<User> finder = factory.get(User.class);
                User user = finder.selectUnique(Long.valueOf(1));

                String mail = user.getMail();

                ObjectNode body = Json.newObject();
                body.put(AbstractApplication.ParameterKey.MAIL, mail);

                FakeRequest fakeRequest = new FakeRequest(POST, "/v1/mobile/user/exclude").withJsonBody(body);
                Result result = route(fakeRequest);

                boolean status = (status(result) == Http.Status.OK);

                assertThat(result).isNotNull();
                assertThat(status).isTrue();

                String responseBody = new String(JavaResultExtractor.getBody((SimpleResult) result));
                JsonNode jsonResponse = Json.parse(responseBody);

                assertStatusMessage(jsonResponse, status);
            }

        });
    }

    @Test
    public void successfulWishListCreationWithProductsTest() {
        running(fakeApplication(), new Runnable() {

            @Override
            public void run() {
                FinderFactory factory = FinderFactory.getInstance();
                IFinder<User> finder = factory.get(User.class);
                User user = finder.selectUnique(Long.valueOf(1));
                Token token = user.getToken();

                String identifier = UUID.randomUUID().toString();

                ObjectNode body = Json.newObject();
                body.put(AbstractApplication.ParameterKey.TITLE, "title -> " + identifier);
                body.put(AbstractApplication.ParameterKey.DESCRIPTION, "description -> " + identifier);

                int productsCount = (int) (Math.random() * 101);

                if (productsCount > 0) {
                    List<ObjectNode> arrayProducts = new ArrayList<ObjectNode>(productsCount + 5);
                    for (int i = 0;i < arrayProducts.size();i++) {
                        ObjectNode jsonProduct = Json.newObject();
                        jsonProduct.put(AbstractApplication.ParameterKey.NAME, "[" + identifier + "] name -> " + i);
                        jsonProduct.put(AbstractApplication.ParameterKey.NICK_NAME, "[" + identifier + "] nickname -> " + i);
                        jsonProduct.put(AbstractApplication.ParameterKey.MANUFACTURER, "[" + identifier + "] manufacturer -> " + i);
                        arrayProducts.add(jsonProduct);
                    }
                    body.put(AbstractApplication.ParameterKey.PRODUCTS, Json.toJson(arrayProducts));
                }

                FakeRequest fakeRequest = new FakeRequest(POST, "/v1/mobile/wishlist/create")
                        .withHeader(AbstractApplication.HeaderKey.HEADER_AUTHENTICATION_TOKEN, token.getContent())
                        .withJsonBody(body);
                Result result = route(fakeRequest);

                boolean status = (status(result) == Http.Status.OK);

                assertThat(result).isNotNull();
                assertThat(status).isTrue();

                String responseBody = new String(JavaResultExtractor.getBody((SimpleResult) result));
                JsonNode jsonResponse = Json.parse(responseBody);

                assertStatusMessage(jsonResponse, status);
            }

        });
    }

    @Test
    public void successfulWishListCreationWithProductsAndMultimediaTest() {
        running(fakeApplication(), new Runnable() {

            @Override
            public void run() {
                FinderFactory factory = FinderFactory.getInstance();
                IFinder<User> finder = factory.get(User.class);
                User user = finder.selectUnique(Long.valueOf(1));
                Token token = user.getToken();

                String identifier = UUID.randomUUID().toString();

                ObjectNode body = Json.newObject();
                body.put(AbstractApplication.ParameterKey.TITLE, "title -> " + identifier);
                body.put(AbstractApplication.ParameterKey.DESCRIPTION, "description -> " + identifier);

                int productsCount = (int) (Math.random() * 101);

                if (productsCount > 0) {
                    List<ObjectNode> arrayProducts = new ArrayList<ObjectNode>(productsCount + 5);
                    for (int i = 0;i < productsCount;i++) {
                        ObjectNode jsonProduct = Json.newObject();
                        jsonProduct.put(AbstractApplication.ParameterKey.NAME, "[" + identifier + "] name -> " + i);
                        jsonProduct.put(AbstractApplication.ParameterKey.NICK_NAME, "[" + identifier + "] nickname -> " + i);
                        jsonProduct.put(AbstractApplication.ParameterKey.MANUFACTURER, "[" + identifier + "] manufacturer -> " + i);
                        arrayProducts.add(jsonProduct);
                    }
                    body.put(AbstractApplication.ParameterKey.PRODUCTS, Json.toJson(arrayProducts));
                }

                FakeRequest fakeRequest = new FakeRequest(POST, "/v1/mobile/wishlist/create")
                        .withHeader(AbstractApplication.HeaderKey.HEADER_AUTHENTICATION_TOKEN, token.getContent())
                        .withJsonBody(body);
                Result result = route(fakeRequest);

                boolean status = (status(result) == Http.Status.OK);

                assertThat(result).isNotNull();
                assertThat(status).isTrue();

                String responseBody = new String(JavaResultExtractor.getBody((SimpleResult) result));
                JsonNode jsonResponse = Json.parse(responseBody);

                assertStatusMessage(jsonResponse, status);

                if (productsCount > 0) {
                    for (int i = 0;i < productsCount && i % 2 == 0;i++) {
//                        FakeRequest fakeRequestMultimedia = new FakeRequest(POST, "")
//                                .withHeader(AbstractApplication.HeaderKey.HEADER_AUTHENTICATION_TOKEN, token.getContent())
//                                .withRawBody();


                    }
                }
            }

        });
    }

    @Test
    public void successfulWishListEditionTest() {
        running(fakeApplication(), new Runnable() {

            @Override
            public void run() {
                FinderFactory factory = FinderFactory.getInstance();
                IFinder<User> finder = factory.get(User.class);
                User user = finder.selectUnique(Long.valueOf(1));
                Token token = user.getToken();

                String identifier = UUID.randomUUID().toString();

                List<WishList> wishLists = user.getWishList();
                if (wishLists.isEmpty())
                    throw new RuntimeException("A lista de desejos do usuário está vazia.");

                int index = (int) (Math.random() * (wishLists.size() - 1));
                WishList wishList = wishLists.get(index);

                ObjectNode body = Json.newObject();
                body.put(AbstractApplication.ParameterKey.ID, wishList.getId());
                body.put(AbstractApplication.ParameterKey.TITLE, "[" + identifier + "] edited title");
                body.put(AbstractApplication.ParameterKey.DESCRIPTION, wishList.getDescription());

                FakeRequest fakeRequest = new FakeRequest(POST, "/v1/mobile/wishlist/update")
                        .withHeader(AbstractApplication.HeaderKey.HEADER_AUTHENTICATION_TOKEN, token.getContent())
                        .withJsonBody(body);
                Result result = route(fakeRequest);

                boolean status = (status(result) == Http.Status.OK);

                assertThat(result).isNotNull();
                assertThat(status).isTrue();

                String responseBody = new String(JavaResultExtractor.getBody((SimpleResult) result));
                JsonNode jsonResponse = Json.parse(responseBody);

                assertStatusMessage(jsonResponse, status);
            }

        });
    }

    @Test
    public void successfulWishListRemoveTest() {
        running(fakeApplication(), new Runnable() {

            @Override
            public void run() {
                FinderFactory factory = FinderFactory.getInstance();
                IFinder<User> finder = factory.get(User.class);
                User user = finder.selectUnique(Long.valueOf(1));
                Token token = user.getToken();

                List<WishList> wishLists = user.getWishList();
                if (wishLists.isEmpty())
                    throw new RuntimeException("A lista de desejos do usuário está vazia.");

                int index = (int) (Math.random() * (wishLists.size() - 1));
                WishList wishList = wishLists.get(index);

                ObjectNode body = Json.newObject();
                body.put(AbstractApplication.ParameterKey.ID, wishList.getId());

                FakeRequest fakeRequest = new FakeRequest(POST, "/v1/mobile/wishlist/delete")
                        .withHeader(AbstractApplication.HeaderKey.HEADER_AUTHENTICATION_TOKEN, token.getContent())
                        .withJsonBody(body);
                Result result = route(fakeRequest);

                boolean status = (status(result) == Http.Status.OK);

                assertThat(result).isNotNull();
                assertThat(status).isTrue();

                String responseBody = new String(JavaResultExtractor.getBody((SimpleResult) result));
                JsonNode jsonResponse = Json.parse(responseBody);

                assertStatusMessage(jsonResponse, status);
            }

        });
    }

    @Test
    public void successfulWishListTest() {
        running(fakeApplication(), new Runnable() {

            @Override
            public void run() {
                FinderFactory factory = FinderFactory.getInstance();
                IFinder<User> finder = factory.get(User.class);
                User user = finder.selectUnique(Long.valueOf(1));
                Token token = user.getToken();

                FakeRequest fakeRequest = new FakeRequest(POST, "/v1/mobile/wishlist/list")
                        .withHeader(AbstractApplication.HeaderKey.HEADER_AUTHENTICATION_TOKEN, token.getContent());
                Result result = route(fakeRequest);

                boolean status = (status(result) == Http.Status.OK);

                assertThat(result).isNotNull();
                assertThat(status).isTrue();

                String responseBody = new String(JavaResultExtractor.getBody((SimpleResult) result));
                JsonNode jsonResponse = Json.parse(responseBody);

                assertStatusMessage(jsonResponse, status);

                assertThat(jsonResponse.hasNonNull(AbstractApplication.ParameterKey.WISHLIST)).isTrue();

                JsonNode arrayWishList = jsonResponse.get(AbstractApplication.ParameterKey.WISHLIST);
                assertThat(arrayWishList).isNotNull();
                assertThat(arrayWishList.isArray()).isTrue();

                for (int i = 0;i < arrayWishList.size();i++) {
                    JsonNode jsonWishList = arrayWishList.get(i);
                    assertThat(jsonWishList).isNotNull();
                    assertThat(jsonWishList.isArray()).isFalse();
                    assertThat(jsonWishList.hasNonNull(AbstractApplication.ParameterKey.ID)).isTrue();
                    assertThat(jsonWishList.hasNonNull(AbstractApplication.ParameterKey.TITLE)).isTrue();
                    assertThat(jsonWishList.has(AbstractApplication.ParameterKey.DESCRIPTION)).isTrue();
                }
            }

        });
    }

    private void assertAuthenticationHeader(Result result) {
        String authenticationToken = header(AbstractApplication.HeaderKey.HEADER_AUTHENTICATION_TOKEN, result);
        assertThat(authenticationToken).isNotNull().isNotEmpty();
    }

    private void assertStatusMessage(JsonNode jsonResponse, boolean status) {
        assertThat(jsonResponse.hasNonNull(AbstractApplication.ParameterKey.STATUS)).isTrue();
        assertThat(jsonResponse.hasNonNull(AbstractApplication.ParameterKey.MESSAGE)).isTrue();

        JsonNode nodeStatus = jsonResponse.get(AbstractApplication.ParameterKey.STATUS);
        assertThat(nodeStatus.asBoolean() == status).isTrue();

        JsonNode nodeMessage = jsonResponse.get(AbstractApplication.ParameterKey.MESSAGE);
        assertThat(nodeMessage.asText()).isNotNull().isNotEmpty();
    }

}
