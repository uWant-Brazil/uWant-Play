package models.database;

import models.AbstractFactory;
import models.classes.*;
import models.classes.admin.Administrator;

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
        } else if (id == WishList.class) {
            finder = new WishListFinder();
        } else if (id == SocialProfile.class) {
            finder = new SocialProfileFinder();
        } else if (id == Action.class) {
            finder = new ActionFinder();
        } else if (id == FriendsCircle.class) {
            finder = new FriendsCircleFinder();
        } else if (id == ActionReport.class) {
            finder = new ActionReportFinder();
        } else if (id == Notification.class) {
            finder = new NotificationFinder();
        } else if (id == ActionShare.class) {
            finder = new ActionShareFinder();
        } else if (id == Want.class) {
            finder = new WantFinder();
        } else if (id == Comment.class) {
            finder = new CommentFinder();
        } else if (id == Product.class) {
            finder = new ProductFinder();
        } else if (id == Administrator.class) {
            finder = new AdministratorFinder();
        } else if (id == Mobile.class) {
            finder = new MobileFinder();
        } else if (id == WishListProduct.class) {
            finder = new WishListProductFinder();
        }

        return finder;
    }

}
