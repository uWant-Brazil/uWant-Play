package controllers.mobile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.AbstractApplication;
import models.classes.SocialProfile;
import models.classes.User;
import models.database.FinderFactory;
import models.database.IFinder;
import models.exceptions.AuthenticationException;
import models.exceptions.JSONBodyException;
import models.exceptions.UWException;
import models.exceptions.UnknownException;
import play.libs.Json;
import play.mvc.Result;
import utils.UserUtil;

/**
 * Created by felipebonezi on 24/05/14.
 */
public class SocialController extends AbstractApplication {

    public static Result signUp() {
        JsonNode body = request().body().asJson();
        ObjectNode jsonResponse = Json.newObject();
        try {
            if (body != null) {
                if (body.hasNonNull(ParameterKey.TOKEN) && body.hasNonNull(ParameterKey.SOCIAL_PROVIDER) && body.has(ParameterKey.LOGIN)) {
                    String accessToken = body.get(ParameterKey.TOKEN).asText();
                    String providerStr = body.get(ParameterKey.SOCIAL_PROVIDER).asText();
                    String email = body.get(ParameterKey.LOGIN).asText();
                    if (!accessToken.isEmpty() && !providerStr.isEmpty()) {
                        SocialProfile.Provider provider = SocialProfile.Provider.valueOf(providerStr);

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
                                generateToken(user);

                                if (profile.getStatus() == SocialProfile.Status.REMOVED) {
                                    profile.setStatus(SocialProfile.Status.ACTIVE);
                                    profile.update();
                                }

                                jsonResponse.put(ParameterKey.STATUS, true);
                                jsonResponse.put(ParameterKey.MESSAGE, "Este usuário já existe no sistema. Ele está autenticado para acessar o sistema.");
                                jsonResponse.put(ParameterKey.REGISTERED, true);
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
                                    } else if (UserUtil.isAvailable(user)) {
                                        // Este usuario ja esta registrado no sistema.
                                        // Efetuando rotina de autenticacao!
                                        generateToken(user);

                                        if (profile.getStatus() == SocialProfile.Status.REMOVED) {
                                            profile.setStatus(SocialProfile.Status.ACTIVE);
                                            profile.update();
                                        }

                                        jsonResponse.put(ParameterKey.STATUS, true);
                                        jsonResponse.put(ParameterKey.MESSAGE, "Este usuário já existe no sistema. Ele está autenticado para acessar o sistema.");
                                        jsonResponse.put(ParameterKey.REGISTERED, true);
                                    } else {
                                        throw new AuthenticationException();
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

}
