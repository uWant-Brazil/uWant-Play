package controllers.mobile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.AbstractApplication;
import models.classes.SocialProfile;
import models.classes.Token;
import models.classes.User;
import models.database.FinderFactory;
import models.database.IFinder;
import models.exceptions.*;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Security;
import security.MobileAuthenticator;
import utils.UserUtil;

/**
 * Controlador responsável pelo tratamento de requisições mobile relacionados a Redes Sociais.
 */
public class SocialController extends AbstractApplication {

    /**
     * Método responsável pela análise de registro ou autenticação através da rede social.
     * Caso o usuário já tenha efetuado o cadastro e vinculado a rede social ao seu usuário
     * do sistema, o mesmo irá gerar um token de autenticação.
     * Caso contrário, será aberto uma solicitação de cadastro no sistema antes de efetuar
     * a autenticação do usuário.
     * @return JSON
     */
    public static Result signUp() {
        JsonNode body = request().body().asJson();
        ObjectNode jsonResponse = Json.newObject();
        try {
            if (body != null) {
                if (body.hasNonNull(ParameterKey.TOKEN) && body.hasNonNull(ParameterKey.SOCIAL_PROVIDER)) {
                    String accessToken = body.get(ParameterKey.TOKEN).asText();
                    int providerOrdinal = body.get(ParameterKey.SOCIAL_PROVIDER).asInt();

                    SocialProfile.Provider[] providers = SocialProfile.Provider.values();
                    if (!accessToken.isEmpty() && providerOrdinal >= 0 && providerOrdinal < providers.length) {
                        String email = null;
                        if (body.has(ParameterKey.LOGIN)) {
                            email = body.get(ParameterKey.LOGIN).asText();
                        }

                        SocialProfile.Provider provider = providers[providerOrdinal];

                        FinderFactory factory = FinderFactory.getInstance();
                        IFinder<SocialProfile> finder = factory.get(SocialProfile.class);
                        SocialProfile profile = finder.selectUnique(new String[] { FinderKey.TOKEN, FinderKey.SOCIAL_PROVIDER, FinderKey.STATUS }, new Object[] { accessToken, provider, SocialProfile.Status.ACTIVE.ordinal() });
                        if (profile != null) {
                            User user = profile.getUser();
                            if (user == null) {
                                // Este usuario esta em processo de registro, mas nao finalizou ainda...
                                jsonResponse.put(ParameterKey.STATUS, true);
                                jsonResponse.put(ParameterKey.MESSAGE, "Este usuário ainda não efetuou o cadastro no sistema, mas já possui a rede social vinculada.");
                                jsonResponse.put(ParameterKey.REGISTERED, false);
                            } else if (UserUtil.isAvailable(user)) {
                                // Este usuario ja esta registrado no sistema.
                                // Efetuando rotina de autenticacao!
                                generateToken(user, Token.Target.MOBILE);

                                if (profile.getStatus() == SocialProfile.Status.REMOVED) {
                                    profile.setStatus(SocialProfile.Status.ACTIVE);
                                    profile.update();
                                }

                                jsonResponse.put(ParameterKey.STATUS, true);
                                jsonResponse.put(ParameterKey.MESSAGE, "Este usuário já existe no sistema. Ele está autenticado para acessar o sistema.");
                                jsonResponse.put(ParameterKey.REGISTERED, true);
                                jsonResponse.put(ParameterKey.USER, Json.toJson(user));
                            } else {
                                throw new AuthenticationException();
                            }
                        } else {
                            IFinder<SocialProfile.Login> finderLogin = factory.get(SocialProfile.Login.class);
                            SocialProfile.Login login = finderLogin.selectUnique(new String[] { FinderKey.LOGIN }, new String[] { email });

                            if (login != null) {
                                profile = login.getProfile();
                                if (profile != null) {
                                    profile.setAccessToken(accessToken);
                                    profile.update();

                                    User user = profile.getUser();
                                    if (user == null) {
                                        // Este usuario esta em processo de registro, mas nao finalizou ainda...
                                        jsonResponse.put(ParameterKey.STATUS, true);
                                        jsonResponse.put(ParameterKey.MESSAGE, "Este usuário ainda não efetuou o cadastro no sistema, mas já possui a rede social vinculada.");
                                        jsonResponse.put(ParameterKey.REGISTERED, false);
                                    } else {
                                        // Este usuario ja esta registrado no sistema.
                                        // Efetuando rotina de autenticacao!
                                        generateToken(user, Token.Target.MOBILE);

                                        if (profile.getStatus() == SocialProfile.Status.REMOVED) {
                                            profile.setUser(null); // Remove o antigo usuário da rede social vinculada.
                                            profile.setStatus(SocialProfile.Status.WAITING_REGISTRATION);
                                            profile.update();

                                            jsonResponse.put(ParameterKey.MESSAGE, "Este usuário ainda não efetuou o cadastro no sistema, mas já possui a rede social vinculada.");
                                            jsonResponse.put(ParameterKey.REGISTERED, false);
                                        } else {
                                            jsonResponse.put(ParameterKey.MESSAGE, "Este usuário já existe no sistema. Ele está autenticado para acessar o sistema.");
                                            jsonResponse.put(ParameterKey.REGISTERED, true);
                                            jsonResponse.put(ParameterKey.USER, Json.toJson(user));
                                        }

                                        jsonResponse.put(ParameterKey.STATUS, true);
                                    }
                                } else {
                                    throw new UnknownException();
                                }
                            } else {
                                // Este usuario esta acessando o sistema pela primeira vez!
                                // Registrando a rede social dele enquanto ele se registra no sistema...
                                profile = new SocialProfile();
                                profile.setAccessToken(accessToken);
                                profile.setStatus(SocialProfile.Status.WAITING_REGISTRATION);
                                profile.setProvider(provider);
                                profile.save();

                                login = new SocialProfile.Login();
                                login.setLogin(email);
                                login.setProfile(profile);
                                login.save();

                                jsonResponse.put(ParameterKey.STATUS, true);
                                jsonResponse.put(ParameterKey.MESSAGE, "Este usuário é novo no sistema, efetue o registro do mesmo.");
                                jsonResponse.put(ParameterKey.REGISTERED, false);
                            }
                        }
                    } else {
                        throw new JSONBodyException();
                    }
                } else {
                    throw new JSONBodyException();
                }
            } else {
                throw new JSONBodyException();
            }
        } catch (UWException e) {
            e.printStackTrace();
            jsonResponse.put(ParameterKey.STATUS, false);
            jsonResponse.put(ParameterKey.ERROR, e.getCode());
            jsonResponse.put(ParameterKey.MESSAGE, e.getMessage());
        }

        return ok(jsonResponse);
    }

    @Security.Authenticated(MobileAuthenticator.class)
    public static Result link() {
        ObjectNode jsonResponse = Json.newObject();
        try {
            User user = authenticateToken();
            if (user != null && UserUtil.isAvailable(user)) {
                JsonNode body = request().body().asJson();
                if (body != null) {
                    if (body.hasNonNull(ParameterKey.TOKEN) && body.hasNonNull(ParameterKey.SOCIAL_PROVIDER)) {
                        String accessToken = body.get(ParameterKey.TOKEN).asText();
                        int providerOrdinal = body.get(ParameterKey.SOCIAL_PROVIDER).asInt();

                        SocialProfile.Provider[] providers = SocialProfile.Provider.values();
                        if (!accessToken.isEmpty() && providerOrdinal >= 0 && providerOrdinal < providers.length) {
                            String email = null;
                            if (body.has(ParameterKey.LOGIN)) {
                                email = body.get(ParameterKey.LOGIN).asText();
                            }

                            SocialProfile.Provider provider = providers[providerOrdinal];

                            FinderFactory factory = FinderFactory.getInstance();
                            IFinder<SocialProfile> finder = factory.get(SocialProfile.class);
                            SocialProfile profile = finder.selectUnique(new String[]{FinderKey.TOKEN, FinderKey.SOCIAL_PROVIDER, FinderKey.STATUS}, new Object[]{accessToken, provider, SocialProfile.Status.ACTIVE.ordinal()});
                            if (profile != null) {
                                User userProfile = profile.getUser();
                                if (userProfile.getId() == user.getId()) {
                                    SocialProfile profileUpdated = new SocialProfile();
                                    profileUpdated.setStatus(SocialProfile.Status.REMOVED);
                                    profileUpdated.update(profile.getId());

                                    jsonResponse.put(ParameterKey.STATUS, true);
                                    jsonResponse.put(ParameterKey.MESSAGE, "A conta virtual do usuário acaba de ser desvinculada.");
                                    jsonResponse.put(ParameterKey.LINKED, false);
                                } else {
                                    throw new SocialProfileAlreadyExistException();
                                }
                            } else {
                                IFinder<SocialProfile.Login> finderLogin = factory.get(SocialProfile.Login.class);
                                SocialProfile.Login login = finderLogin.selectUnique(new String[]{FinderKey.LOGIN}, new String[]{email});

                                if (login != null) {
                                    profile = login.getProfile();
                                    if (profile != null) {
                                        SocialProfile.Provider socialProvider = profile.getProvider();

                                        if (socialProvider == provider) {
                                            User userProfile = profile.getUser();
                                            if (userProfile.getId() == user.getId()) {
                                                SocialProfile profileUpdated = new SocialProfile();
                                                profileUpdated.setStatus(SocialProfile.Status.REMOVED);
                                                profileUpdated.update(profile.getId());

                                                jsonResponse.put(ParameterKey.STATUS, true);
                                                jsonResponse.put(ParameterKey.MESSAGE, "A conta virtual do usuário acaba de ser desvinculada.");
                                                jsonResponse.put(ParameterKey.LINKED, false);
                                            } else {
                                                throw new SocialProfileAlreadyExistException();
                                            }
                                        }
                                    } else {
                                        throw new UnknownException();
                                    }
                                }

                                // Este usuario esta vinculando sua rede social ao sistema pela primeira vez!
                                // Registrando a rede social dele...
                                profile = new SocialProfile();
                                profile.setAccessToken(accessToken);
                                profile.setStatus(SocialProfile.Status.ACTIVE);
                                profile.setUser(user);
                                profile.setProvider(provider);
                                profile.save();

                                login = new SocialProfile.Login();
                                login.setLogin(email);
                                login.setProfile(profile);
                                login.save();

                                jsonResponse.put(ParameterKey.STATUS, true);
                                jsonResponse.put(ParameterKey.MESSAGE, "A conta virtual do usuário acaba de ser vinculada.");
                                jsonResponse.put(ParameterKey.LINKED, true);
                            }
                        } else {
                            throw new JSONBodyException();
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
        } catch (UWException e) {
            e.printStackTrace();
            jsonResponse.put(ParameterKey.STATUS, false);
            jsonResponse.put(ParameterKey.ERROR, e.getCode());
            jsonResponse.put(ParameterKey.MESSAGE, e.getMessage());
        }

        return ok(jsonResponse);
    }

}
