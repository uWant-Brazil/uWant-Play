package models.database;

import models.AbstractFactory;
import models.classes.*;

/**
 * Factory de Finder para banco de dados.
 */
public class FinderFactory extends AbstractFactory<Class<?>, IFinder> {

    /**
     * Singleton para o factory.
     */
    private static FinderFactory INSTANCE;

    private FinderFactory() {
        // Do nothing...
    }

    public static FinderFactory getInstance() {
        return (INSTANCE == null ? (INSTANCE = new FinderFactory()) : INSTANCE);
    }

    @Override
    public IFinder get(Class<?> id) {
        IFinder<?> finder = null;

        if (id == User.class) {
            finder = new UserFinder();
        } else if (id == Token.class) {
            finder = new TokenFinder();
        } else if (id == UserMailInteraction.class) {
            finder = new UserMailInteractionFinder();
        } else if (id == SocialProfile.Login.class) {
            finder = new SocialProfileLoginsFinder();
        } else if (id == WishList.class) {
            finder = new WishListFinder();
        } else if (id == SocialProfile.class) {
            finder = new SocialProfileFinder();
        } else if (id == Action.class) {
            finder = new ActionFinder();
        }

        return finder;
    }

}
