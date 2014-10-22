package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import models.classes.Token;
import models.classes.User;
import models.database.FinderFactory;
import models.database.IFinder;
import models.exceptions.TokenException;
import org.joda.time.Days;
import play.cache.Cache;
import play.cache.Cached;
import play.data.Form;
import play.libs.F;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import views.html.uwant_sobre;

import java.util.UUID;

/**
 * Controlador-pai <GOD> de todos os controladores.
 */
public class AbstractApplication extends Controller {

    /**
     * Mensagem default para sessões inválidas na web.
     */
    private static final String DEFAULT_INVALID_WEB_SESSION_MESSAGE = "Você tem certeza que está no lugar certo? :-)";

    /**
     * Classe estática responsável por manter todas as chaves de acesso à cabeçalhos HTTP.
     */
    public static class HeaderKey {
        public static final String HEADER_AUTHENTICATION_TOKEN = "Authentication";
        public static final String CONTENT_TYPE = "Content-Type";
        public static final String MULTIPART_FORM_DATA = "multipart/form-data";
    }

    /**
     * Classe estática responsável por manter todas as chaves de acesso/envio ao body HTTP.
     */
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
        public static final String TOKEN = "access_token";
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
        public static final String FILENAME = "fileName";
        public static final String MOBILE_IDENTIFIER = "mobileIdentifier";
        public static final String OS = "os";
        public static final String WHEN = "when";
        public static final String ACTIONS = "actions";
        public static final String TYPE = "type";
        public static final String USER_FROM = "userFrom";
        public static final String EXTRA = "extra";
        public static final String IDENTIFIER = "identifier";
        public static final String USER = "user";
        public static final String FRIENDS = "friends";
        public static final String FRIENDSHIP_LEVEL = "friendshipLevel";
        public static final String PERFIL = "perfil";
        public static final String ACTION_ID = "actionId";
        public static final String COMMENT = "comment";
        public static final String CONTACTS = "contacts";
        public static final String COMMENTS = "comments";
        public static final String COMMENTS_COUNT = "commentsCount";
        public static final String MULTIMEDIAS = "multimedias";
        public static final String PICTURE = "picture";
        public static final String COUNT = "count";
        public static final String WANT = "want";
        public static final String SHARE = "share";
        public static final String UWANT = "uWant";
        public static final String USHARE = "uShare";
        public static final String USER_ID = "userId";
        public static final String WISHLIST_ID = "wishlistId";
        public static final String LINKED = "linked";
        public static final String PRODUCTS_REMOVED = "productsRemoved";
    }

    /**
     * Classe estática responsável por manter todas as chaves de acesso as colunas do Finder - BD.
     */
    public static class FinderKey {
        public static final String ID = "id";
        public static final String TOKEN = "access_token";
        public static final String CONTENT = "content";
        public static final String LOGIN = "login";
        public static final String PASSWORD = "password";
        public static final String MAIL = "mail";
        public static final String SOCIAL_PROVIDER = "provider";
        public static final String STATUS = "status";
        public static final String NAME = "name";
        public static final String USER_ID = "user_id";
        public static final String WISHLIST_ID = "wishlist_id";
        public static final String PRODUCT_ID = "product_id";
        public static final String HASH = "hash";
        public static final String TYPE = "type";
        public static final String REQUESTER_ID = "requester_id";
        public static final String TARGET_ID = "target_id";
        public static final String ACTION_ID = "action_id";
        public static final String MODIFIED_AT = "modified_at";
        public static final String CREATED_AT = "created_at";
        public static final String IDENTIFIER = "identifier";
        public static final String UUID = "uuid";
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
    public static void generateToken(User user, Token.Target target) {
        UUID uuid = UUID.randomUUID();
        String token = uuid.toString();

        saveToken(token, user, target);

        response().setHeader(HeaderKey.HEADER_AUTHENTICATION_TOKEN, token);
    }

    /**
     * Persistência do token gerado pelo sistema.
     * @param tokenContent
     * @param user
     * @param target
     */
    private static void saveToken(String tokenContent, User user, Token.Target target) {
        Token token = new Token();
        token.setContent(tokenContent);
        token.setUser(user);
        token.setTarget(target);
        token.save();

        user.refresh();
    }

    /**
     * Retorna o usuário para determinado token.
     * @param token
     * @return
     */
    public static Token listToken(String token) {
        FinderFactory factory = FinderFactory.getInstance();
        IFinder<Token> finder = factory.get(Token.class);
        return finder.selectUnique(new String[] { FinderKey.CONTENT }, new String[] { token });
    }

    /**
     * Remoção do token do usuário referenciado no HTTP Header.
     * @param user - Usuário logado
     */
    public static void removeToken(User user) {
        String tokenContent = request().getHeader(HeaderKey.HEADER_AUTHENTICATION_TOKEN);
        Token token = listToken(tokenContent);

        if (token != null) {
            token.refresh();
            token.delete();
            user.refresh();
        }
    }

    /**
     * Obtém o token que foi enviado no cabeçalho do body no HTTP.
     * @param request
     * @return token
     */
    public static String getToken(Http.Request request) {
        return request.getHeader(HeaderKey.HEADER_AUTHENTICATION_TOKEN);
    }

    /**
     * Método default quando uma sessão for inválida no mobile.
     * @return JSON
     */
    public static Result invalidMobileSession() {
        ObjectNode jsonResponse = Json.newObject();
        jsonResponse.put(ParameterKey.ERROR, -999);
        jsonResponse.put(ParameterKey.MESSAGE, "Você não está autorizado a realizar este tipo de ação.");
        return ok(jsonResponse);
    }

    /**
     * Método default quando uma sessão for inválida na web.
     * @param message - Mensagem que será exibida.
     * @return JSON
     */
    public static F.Promise<Result> invalidWebSession(String message) {
        String key = String.format("session.invalid.%s", message);
        F.Promise<Result> result = (F.Promise<Result>) Cache.get(key);

        if (result == null) {
            result = F.Promise.promise(() -> ok(views.html.unauthorized.render(message)));
            Cache.set(key, result, Days.days(1).toStandardSeconds().getSeconds()); // Cache diário.
        }

        return result;
    }

    /**
     * Método default quando uma sessão for inválida na web.
     * @return JSON
     */
    public static F.Promise<Result> invalidWebSession() {
        return invalidWebSession(DEFAULT_INVALID_WEB_SESSION_MESSAGE);
    }

    /**
     * Método responsável por exibir a view contendo o 'Sobre' do app.
     * @return HTML
     */
    @Cached(key = "about")
    public static F.Promise<Result> about() {
        return F.Promise.promise(() -> ok(uwant_sobre.render()));
    }

    /**
     * Método para verificar se o formulário web não contém erros de validações.
     * @param form - Formulário web
     * @return true or false
     */
    public static boolean isValidForm(Form<?> form) {
        return !(form.hasErrors() || form.hasGlobalErrors());
    }

    /**
     * Método responsável por renderizar a página inicial do uWant.
     * @return HTML
     */
    @Cached(key = "homepage")
    public static F.Promise<Result> index() {
        return F.Promise.promise(() -> ok(views.html.index.render()));
    }

}
