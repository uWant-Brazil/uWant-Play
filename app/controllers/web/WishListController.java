package controllers.web;

import controllers.AbstractApplication;
import models.classes.WishList;
import models.cloud.forms.WishListViewModel;
import models.database.FinderFactory;
import models.database.IFinder;
import play.data.Form;
import play.libs.F;
import play.mvc.Result;

/**
 * Controlador responsável pelo tratamento em requisições mobile relacionados a lista de desejos.
 */
public class WishListController extends AbstractApplication {

    public static F.Promise<Result> view() {
        Form<WishListViewModel> form = Form.form(WishListViewModel.class).bindFromRequest(request());
        if (isValidForm(form)) {
            final WishListViewModel model = form.get();
            return F.Promise.promise(new F.Function0<WishList>() {

                @Override
                public WishList apply() throws Throwable {
                    FinderFactory factory = FinderFactory.getInstance();
                    IFinder<WishList> finder = factory.get(WishList.class);
                    return finder.selectUnique(new String[] { FinderKey.UUID }, new Object[] { model.getUUID() });
                }

            }).map(new F.Function<WishList, Result>() {

                @Override
                public Result apply(WishList wishList) throws Throwable {
                    Result result;
                    if (wishList == null) {
                        result = invalidWebSession().get(1000);
                    } else {
                        result = ok(views.html.view_wishlist.render(wishList));
                    }
                    return result;
                }

            });
        }
        return invalidWebSession();
    }

}
