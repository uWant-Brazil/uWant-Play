package controllers.mobile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.AbstractApplication;
import models.classes.SocialProfile;
import models.classes.User;
import models.database.FinderFactory;
import models.database.IFinder;
import models.exceptions.*;
import org.apache.http.impl.cookie.DateParseException;
import play.libs.Json;
import play.mvc.Result;
import utils.DateUtil;
import utils.RegexUtil;
import utils.UserUtil;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by felipebonezi on 22/05/14.
 */
public class UserController extends AbstractApplication {

    public static Result register() {
        ObjectNode jsonResponse = Json.newObject();
        try {
            JsonNode body = request().body().asJson();
            if (body != null) {
                if (body.hasNonNull(ParameterKey.LOGIN) && body.hasNonNull(ParameterKey.PASSWORD)
                        && body.hasNonNull(ParameterKey.FULL_NAME) && body.hasNonNull(ParameterKey.BIRTHDAY)
                        && body.hasNonNull(ParameterKey.GENDER) && body.hasNonNull(ParameterKey.MAIL)) {
                    String login = body.get(ParameterKey.LOGIN).asText();
                    String mail = body.get(ParameterKey.MAIL).asText();

                    if (login.isEmpty() || mail.isEmpty())
                        throw new JSONBodyException();

                    if (!RegexUtil.isValidMail(mail))
                        throw new InvalidMailException();

                    if (UserUtil.check(login, mail)) {
                        String password = body.get(ParameterKey.PASSWORD).asText();
                        String fullName = body.get(ParameterKey.FULL_NAME).asText();
                        String birthdayStr = body.get(ParameterKey.BIRTHDAY).asText();
                        int genderOrdinal = body.get(ParameterKey.GENDER).asInt();

                        if (password.isEmpty() || fullName.isEmpty() ||
                                (genderOrdinal != 0 && genderOrdinal != 1)
                                || birthdayStr.isEmpty())
                            throw new JSONBodyException();

                        String[] splitNames = UserUtil.partsOfName(fullName);
                        String firstName = splitNames[0];
                        String middleName = splitNames[1];
                        String lastName = splitNames[2];
                        User.Gender gender = User.Gender.values()[genderOrdinal];

                        Date birthdayDate = null;
                        try {
                            birthdayDate = DateUtil.parse(birthdayStr);
                        } catch (DateParseException e) {
                            e.printStackTrace();
                        }

                        Calendar birthday = null;
                        if (birthdayDate != null) {
                            birthday = Calendar.getInstance();
                            birthday.setTime(birthdayDate);
                        }

                        FinderFactory factory = FinderFactory.getInstance();
                        IFinder<User> finder = factory.get(User.class);
                        User user = finder.selectUnique(new String[] { FinderKey.LOGIN }, new Object[] { login });

                        if (user == null) {
                            user = finder.selectUnique(new String[] { FinderKey.MAIL }, new Object[] { mail });
                            if (user == null) {
                                user = new User();
                                user.setLogin(login);
                                user.setPassword(password);
                                user.setFirstName(firstName);
                                user.setMiddleName(middleName);
                                user.setLastName(lastName);
                                user.setMail(mail);
                                user.setBirthday(birthday);
                                user.setGender(gender);
                                user.setStatus(User.Status.PARTIAL_ACTIVE);
                                user.save();

                                UserUtil.confirmEmail(user);

                                if (body.hasNonNull(ParameterKey.SOCIAL_PROFILE)) {
                                    JsonNode nodeSocial = body.get(ParameterKey.SOCIAL_PROFILE);

                                    if (nodeSocial.hasNonNull(ParameterKey.TOKEN) && nodeSocial.hasNonNull(ParameterKey.SOCIAL_PROVIDER)) {
                                        String accessToken = nodeSocial.get(ParameterKey.TOKEN).asText();
                                        int providerOrdinal = nodeSocial.get(ParameterKey.SOCIAL_PROVIDER).asInt();

                                        IFinder<SocialProfile> finderProfile = factory.get(SocialProfile.class);
                                        SocialProfile socialProfile = finderProfile.selectUnique(new String[] { FinderKey.TOKEN, FinderKey.SOCIAL_PROVIDER }, new Object[] { accessToken, providerOrdinal });
                                        if (socialProfile != null) {
                                            socialProfile.setStatus(SocialProfile.Status.ACTIVE);
                                            socialProfile.setUser(user);
                                            socialProfile.update();
                                        }
                                    }
                                }
                            }

                            jsonResponse.put(ParameterKey.STATUS, true);
                            jsonResponse.put(ParameterKey.MESSAGE, "O usuário (" + login + ") foi registrado com sucesso.");
                        } else {
                            throw new UserAlreadyExistException();
                        }
                    } else {
                        throw new UserAlreadyExistException();
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
            jsonResponse.put(ParameterKey.MESSAGE, e.getMessage());
            jsonResponse.put(ParameterKey.ERROR, e.getCode());
        }
        return ok(jsonResponse);
    }

    public static Result exclude() {
        ObjectNode jsonResponse = Json.newObject();
        try {
            JsonNode body = request().body().asJson();

            if (body != null) {
                User user = authenticateToken();
                if (user != null) {
                    if (UserUtil.isAvailable(user)) {
                        user.setStatus(User.Status.REMOVED);
                        user.update();

                        jsonResponse.put(ParameterKey.STATUS, true);
                        jsonResponse.put(ParameterKey.MESSAGE, "Usuário excluido com sucesso.");
                        jsonResponse.put(ParameterKey.EXCLUDE, true);
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
