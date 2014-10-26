package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import models.classes.Token;
import models.classes.User;
import models.database.FinderFactory;
import models.database.IFinder;
import models.exceptions.TokenException;
import org.joda.time.Days;
import org.joda.time.Hours;
import play.cache.Cache;
import play.cache.Cached;
import play.data.Form;
import play.i18n.Messages;
import play.libs.F;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import views.html.uwant_sobre;

import java.util.UUID;

/**
 * Controlador-pai <GOD> de todos os controladores.
 */
public class AbstractApplication extends Controller {

    /**
     * Mensagem default para sessões inválidas na web.
     */
    private static final String DEFAULT_INVALID_WEB_SESSION_MESSAGE = Messages.get(MessageKey.Abstract.WEB_SESSION_INVALID);

    /**
     * Mensagem default para sessões inválidas no app mobile.
     */
    private static final String DEFAULT_INVALID_MOBILE_SESSION = Messages.get(MessageKey.Abstract.MOBILE_SESSION_INVALID);

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
        public static final String FACEBOOK_ID = "facebookId";
        public static final String ACTION = "action";
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
        public static final String FACEBOOK_ID = "facebook_id";
    }

    public static class MessageKey {
        public static final class Abstract {
            public static final String WEB_SESSION_INVALID = "abstract.websession.invalid";
            public static final String MOBILE_SESSION_INVALID = "abstract.mobilesession.invalid";
        }
        public static final class Action {
            public static final String FEEDS_SUCCESS = "action.feeds.success";
            public static final String COMMENT_SUCCESS = "action.comment.success";
            public static final String COMMENTS_SUCCESS = "action.comments.success";
            public static final String WANT_SUCCESS = "action.want.success";
            public static final String REPORT_SUCCESS = "action.report.success";
            public static final String TOGGLE_BLOCK_SUCCESS = "action.toogleblock.success";
            public static final String SHARE_SUCCESS = "action.share.success";
        }
        public static final class Authentication {
            public static final String RECOVERY_PASSWORD_SUCCESS = "authentication.recoverypassword.success";
            public static final String LOGOFF_SUCCESS = "authentication.logoff.success";
            public static final String AUTHORIZE_SUCCESS = "authentication.authorize.success";
        }
        public static final class CDN {
            public static final String RETRIEVE_SUCCESS = "cdn.retrieve.success";
        }
        public static final class Notification {
            public static final String LIST_ACTIONS_SUCCESS = "notification.listactions.success";
            public static final String REGISTER_SUCCESS = "notification.register.success";
        }
        public static final class Social {
            public static final String LINK_SUCCESS = "social.link.success";
            public static final String UNLINK_SUCCESS = "social.unlink.success";
            public static final String SIGNUP_WAITING_REGISTRATION_SUCCESS = "social.signup.waitingregistration.success";
            public static final String SIGNUP_REGISTER_SUCCESS = "social.signup.register.success";
            public static final String SIGNUP_AUTHORIZE_SUCCESS = "social.signup.authorize.success";
        }
        public static final class User {
            public static final String LIST_CIRCLE_SUCCESS = "user.listcircle.success";
            public static final String ANALYZE_CONTACTS_SUCCESS = "user.analyzecontacts.success";
            public static final String LEAVE_CIRCLE_SUCCESS = "user.leavecircle.success";
            public static final String JOIN_CIRCLE_SUCCESS = "user.joincircle.success";
            public static final String LIST_SUCCESS = "user.list.success";
            public static final String SEARCH_SUCCESS = "user.search.success";
            public static final String EXCLUDE_SUCCESS = "user.exclude.success";
            public static final String REGISTER_SUCCESS = "user.register.success";
        }
        public static final class WishList {
            public static final String DELETE_SUCCESS = "wishlist.delete.success";
            public static final String PRODUCTS_SUCCESS = "wishlist.products.success";
            public static final String LIST_SUCCESS = "wishlist.list.success";
            public static final String UPDATE_SUCCESS = "wishlist.update.success";
            public static final String CREATE_SUCCESS = "wishlist.create.success";
        }
        public static final class Global {
            public static final String MOBILE_SESSION_ERROR = "global.mobilesession.error";
        }
        public static final class Deadbolt {
            public static final String WEB_SESSION_INVALID = "deadbolt.websession.invalid";
        }
        public static final class Exception {
            public static final String AUTHENTICATION = "exception.authentication";
            public static final String INDEX_OUT_OF_BOUNDS = "exception.indexoutofbounds";
            public static final String INVALID_DATE = "exception.invaliddate";
            public static final String INVALID_MAIL = "exception.invalidmail";
            public static final String JSON_BODY = "exception.jsonbody";
            public static final String MULTIPART_BODY = "exception.multipartbody";
            public static final String SOCIAL_PROFILE = "exception.socialprofile";
            public static final String TOKEN = "exception.token";
            public static final String UNAUTHORIZED_OPERATION = "exception.unauthorizedoperation";
            public static final String UNAVAILABLE_BLOCK_FRIEND = "exception.unavailableblockfriend";
            public static final String UNCONFIRMED_MAIL_1 = "exception.unconfirmedmail_1";
            public static final String UNCONFIRMED_MAIL_2 = "exception.unconfirmedmail_2";
            public static final String UNKNOWN = "exception.unknown";
            public static final String USER_EXIST = "exception.userexist";
            public static final String USER_DONT_EXIST = "exception.userdontexist";
            public static final String USER_WITHOUT_MOBILE = "exception.userwithoutmobile";
            public static final String WISHLIST_DONT_EXIST = "exception.wishlistdontexist";
        }

        public static final String ADDED = "message.notification.added";
        public static final String WISH = "message.notification.wish";
        public static final String WISHES = "message.notification.wishes";
        public static final String IN_YOUR_LIST = "message.notification.inyourlist";
        public static final String SHARE_YOUR_ACTION = "message.notification.shareyouraction";
        public static final String MENTION_YOU = "message.notification.mentionyou";
        public static final String COMMENT = "message.notification.comment";
        public static final String ADD_CIRCLE = "message.notification.addcircle";
        public static final String ACCEPT_CIRCLE = "message.notification.acceptcircle";
        public static final String REPORT_1 = "message.notification.report_1";
        public static final String REPORT_2 = "message.notification.report_2";
        public static final String WANT = "message.notification.want";
        public static final String MAIL_CONFIRMATION_SUBJECT = "message.mail.confirmation";
        public static final String MAIL_RECOVERY_PASSWORD_SUBJECT = "message.mail.recoverypassword";
    }

    /**
     * Responsável por autenticar um token existente no cabeçalho HTTP/HTTPS.
     * @return
     * @throws TokenException
     */
    public static User authenticateToken() throws TokenException {
        String tokenContent = getTokenAtHeader();
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
        token.refresh();
        user.refresh();

        Cache.set(tokenContent, token, Hours.ONE.toStandardSeconds().getSeconds()); // Cache de hora em hora.
    }

    /**
     * Retorna o usuário para determinado token.
     * @param token
     * @return
     */
    public static Token listToken(String token) {
        Token tokenCached = (Token) Cache.get(token);
        if (tokenCached == null) {
            FinderFactory factory = FinderFactory.getInstance();
            IFinder<Token> finder = factory.get(Token.class);
            tokenCached = finder.selectUnique(new String[]{FinderKey.CONTENT}, new String[]{token});
        }
        return tokenCached;
    }

    /**
     * Remoção do token do usuário referenciado no HTTP Header.
     * @param user - Usuário logado
     */
    public static void removeToken(User user) {
        String tokenContent = request().getHeader(HeaderKey.HEADER_AUTHENTICATION_TOKEN);
        Token token = listToken(tokenContent);

        if (token != null) {
            Cache.remove(tokenContent);
            token.refresh();
            token.delete();
            user.refresh();
        }
    }

    /**
     * Obtém o token que foi enviado no cabeçalho do body no HTTP.
     * @return token
     */
    public static String getTokenAtHeader() {
        return getTokenAtHeader(request());
    }

    /**
     * Obtém o token que foi enviado no cabeçalho do body no HTTP.
     * @return token
     */
    public static String getTokenAtHeader(Http.Request request) {
        return request.getHeader(HeaderKey.HEADER_AUTHENTICATION_TOKEN);
    }

    /**
     * Método default quando uma sessão for inválida no mobile.
     * @return JSON
     */
    public static F.Promise<Result> invalidMobileSession() {
        return invalidMobileSession(DEFAULT_INVALID_MOBILE_SESSION, -999);
    }

    /**
     * Método default quando uma sessão for inválida no mobile.
     * @param message
     * @param error
     * @return JSON
     */
    public static F.Promise<Result> invalidMobileSession(String message, int error) {
        F.Promise<Result> result = (F.Promise<Result>) Cache.get(String.format("mobile.session.invalid.%s", message));
        if (result == null) {
            final ObjectNode jsonResponse = Json.newObject();
            jsonResponse.put(ParameterKey.STATUS, false);
            jsonResponse.put(ParameterKey.ERROR, error);
            jsonResponse.put(ParameterKey.MESSAGE, message);
            result = F.Promise.<Result>pure(ok(jsonResponse));

            Cache.set("mobile.session.invalid", result, Days.ONE.toStandardSeconds().getSeconds()); // Cache diário.
        }
        return result;
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
            result = F.Promise.<Result>pure(ok(views.html.unauthorized.render(message)));
            Cache.set(key, result, Days.ONE.toStandardSeconds().getSeconds()); // Cache diário.
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
        return F.Promise.<Result>pure(ok(uwant_sobre.render()));
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
    //@Cached(key = "homepage")
    public static F.Promise<Result> index() {
        return F.Promise.<Result>pure(ok(views.html.index.render()));
    }

}
