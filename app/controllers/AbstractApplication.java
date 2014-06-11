package controllers;

import models.classes.Token;
import models.classes.User;
import models.database.FinderFactory;
import models.database.IFinder;
import models.exceptions.TokenException;
import play.mvc.*;

import java.util.UUID;

public class AbstractApplication extends Controller {

    public static class HeaderKey {
        public static final String HEADER_AUTHENTICATION_TOKEN = "Authentication";
        public static final String CONTENT_TYPE = "Content-Type";
        public static final String MULTIPART_FORM_DATA = "";
    }

    public static class ParameterKey {
        public static final String STATUS = "status";
        public static final String MESSAGE = "message";
        public static final String ERROR = "error";
        public static final String LOGIN = "login";
        public static final String PASSWORD = "password";
        public static final String FULL_NAME = "fullName";
        public static final String GENDER = "gender";
        public static final String MAIL = "mail";
        public static final String BIRTHDAY = "birthday";
        public static final String SOCIAL_PROFILE = "socialProfile";
        public static final String SOCIAL_PROVIDER = "socialProvider";
        public static final String TOKEN = "token";
        public static final String REGISTERED = "registered";
        public static final String EXCLUDE = "exclude";
        public static final String QUERY = "query";
        public static final String START_INDEX = "startIndex";
        public static final String END_INDEX = "endIndex";
        public static final String USERS = "users";
        public static final String TITLE = "title";
        public static final String DESCRIPTION = "description";
        public static final String ID = "id";
        public static final String WISHLIST = "wishlist";
        public static final String PRODUCTS = "products";
        public static final String NAME = "name";
        public static final String NICK_NAME = "nickName";
        public static final String MANUFACTURER = "manufacturer";
        public static final String MULTIMEDIA_PRODUCT = "multimediaProduct";
        public static final String MULTIMEDIA_USER_PICTURE = "multimediaUserPicture";
        public static final String MULTIMEDIA = "multimedia";
        public static final String URL = "url";
    }
    public static class FinderKey {

        public static final String ID = "_id";
        public static final String TOKEN = "token";
        public static final String CONTENT = "content";
        public static final String LOGIN = "login";
        public static final String PASSWORD = "password";
        public static final String MAIL = "mail";
        public static final String SOCIAL_PROVIDER = "socialProvider";
        public static final String STATUS = "status";
        public static final String NAME = "name";
        public static final String USER_ID = "user_id";
    }

    /**
     * Responsável por autenticar um token existente no cabeçalho HTTP/HTTPS.
     * @return
     * @throws TokenException
     */
    public static User authenticateToken() throws TokenException {
        String tokenContent = request().getHeader(HeaderKey.HEADER_AUTHENTICATION_TOKEN);
        Token token = listToken(tokenContent);

        if (token == null)
            throw new TokenException();

        return token.getUser();
    }

    /**
     * Responsável por gerar token para futura autenticação em métodos necessários.
     * @param user
     */
    public static void generateToken(User user) {
        UUID uuid = UUID.randomUUID();
        String token = uuid.toString();

        saveToken(token, user);

        response().setHeader(HeaderKey.HEADER_AUTHENTICATION_TOKEN, token);
    }

    /**
     * Persistência do token gerado pelo sistema.
     * @param tokenContent
     * @param user
     */
    private static void saveToken(String tokenContent, User user) {
        Token token = new Token();
        token.setContent(tokenContent);
        token.setUser(user);
        token.save();

        user.refresh();
    }

    /**
     * Retorna o usuário para determinado token.
     * @param token
     * @return
     */
    private static Token listToken(String token) {
        FinderFactory factory = FinderFactory.getInstance();
        IFinder<Token> finder = factory.get(Token.class);
        return finder.selectUnique(new String[] { FinderKey.CONTENT }, new String[] { token });
    }

    /**
     * Remoção do token do usuário.
     * @param user
     */
    public static void removeToken(User user) {
        Token token = user.getToken();
        token.delete();

        user.refresh();
    }

}
