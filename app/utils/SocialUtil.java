package utils;

import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.AbstractApplication;
import models.classes.SocialProfile;
import models.classes.User;
import models.exceptions.SocialProfileAlreadyExistException;
import play.i18n.Messages;

/**
 * Created by Felipe Bonezi on 25/10/2014.
 */
public abstract class SocialUtil {

    public static void unlink(ObjectNode jsonResponse, User user, SocialProfile profile) throws SocialProfileAlreadyExistException {
        User userProfile = profile.getUser();
        if (userProfile.getId() == user.getId()) {
            SocialProfile profileUpdated = new SocialProfile();
            profileUpdated.setStatus(SocialProfile.Status.REMOVED);
            profileUpdated.update(profile.getId());

            jsonResponse.put(AbstractApplication.ParameterKey.STATUS, true);
            jsonResponse.put(AbstractApplication.ParameterKey.MESSAGE, Messages.get(AbstractApplication.MessageKey.Social.UNLINK_SUCCESS));
            jsonResponse.put(AbstractApplication.ParameterKey.LINKED, false);
        } else {
            throw new SocialProfileAlreadyExistException();
        }
    }

    public static void link(ObjectNode jsonResponse, User user, String accessToken, String email, SocialProfile.Provider provider) {
        SocialProfile profile;
        SocialProfile.Login login;// Este usuario esta vinculando sua rede social ao sistema pela primeira vez!
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

        jsonResponse.put(AbstractApplication.ParameterKey.STATUS, true);
        jsonResponse.put(AbstractApplication.ParameterKey.MESSAGE, Messages.get(AbstractApplication.MessageKey.Social.LINK_SUCCESS));
        jsonResponse.put(AbstractApplication.ParameterKey.LINKED, true);
    }

}
