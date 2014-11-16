package utils;

import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.AbstractApplication;
import models.classes.SocialProfile;
import models.classes.User;
import models.exceptions.SocialProfileAlreadyExistException;
import play.i18n.Messages;

/**
 * Classe utilitária para ações relacionadas as redes sociais que o sistema suporta.
 */
public abstract class SocialUtil {

    /**
     * Método responsável por 'remover' uma determinada rede social que está associada a um usuário.
     * @param jsonResponse
     * @param profile
     * @throws SocialProfileAlreadyExistException
     */
    public static void unlink(ObjectNode jsonResponse, SocialProfile profile) throws SocialProfileAlreadyExistException {
        SocialProfile profileUpdated = new SocialProfile();
        profileUpdated.setStatus(SocialProfile.Status.REMOVED);
        profileUpdated.update(profile.getId());

        jsonResponse.put(AbstractApplication.ParameterKey.STATUS, true);
        jsonResponse.put(AbstractApplication.ParameterKey.MESSAGE, Messages.get(AbstractApplication.MessageKey.Social.UNLINK_SUCCESS));
        jsonResponse.put(AbstractApplication.ParameterKey.LINKED, false);
    }

    /**
     * Método responsável por 'adicionar' uma determinada rede social a um usuário.
     * @param jsonResponse
     * @param user
     * @param accessToken
     * @param email
     * @param facebookId
     * @param provider
     * @throws SocialProfileAlreadyExistException
     */
    public static void link(ObjectNode jsonResponse, User user, String accessToken, String email, String facebookId, SocialProfile.Provider provider) {
        SocialProfile profile = new SocialProfile();
        profile.setAccessToken(accessToken);
        profile.setStatus(SocialProfile.Status.ACTIVE);
        profile.setUser(user);
        profile.setProvider(provider);
        profile.setLogin(email);
        profile.setFacebookId(facebookId);
        profile.save();

        jsonResponse.put(AbstractApplication.ParameterKey.STATUS, true);
        jsonResponse.put(AbstractApplication.ParameterKey.MESSAGE, Messages.get(AbstractApplication.MessageKey.Social.LINK_SUCCESS));
        jsonResponse.put(AbstractApplication.ParameterKey.LINKED, true);
    }

}
