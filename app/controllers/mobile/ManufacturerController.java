package controllers.mobile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.AbstractApplication;
import models.classes.Manufacturer;
import models.classes.User;
import models.database.FinderFactory;
import models.database.IFinder;
import models.exceptions.AuthenticationException;
import models.exceptions.JSONBodyException;
import models.exceptions.UWException;
import play.db.ebean.Model;
import play.libs.F;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Security;
import security.MobileAuthenticator;
import utils.UserUtil;

import java.util.List;

@Security.Authenticated(MobileAuthenticator.class)
public class ManufacturerController extends AbstractApplication {

    public static F.Promise<Result> list() {
        return F.Promise.<Result>promise(() -> {
            final ObjectNode objectNode = Json.newObject();

            try {
                User user = authenticateToken();
                if (UserUtil.isAvailable(user)) {
                    JsonNode body = request().body().asJson();

                    if (body != null && body.hasNonNull(ParameterKey.MANUFACTURER)) {
                        String manufacturerName = body.get(ParameterKey.MANUFACTURER).asText();

                        FinderFactory factory = FinderFactory.getInstance();
                        IFinder<Manufacturer> ifinder = factory.get(Manufacturer.class);
                        Model.Finder<Long, Manufacturer> finder = ifinder.getFinder();

                        List<Manufacturer> manufacturers = finder.where()
                                .icontains(FinderKey.NAME, manufacturerName)
                                .findList();

                        objectNode.put(ParameterKey.STATUS, true);
                        objectNode.put(ParameterKey.MESSAGE, "Os fabricantes foram listados.");
                        objectNode.put(ParameterKey.MANUFACTURERS, Json.toJson(manufacturers));
                    } else {
                        throw new JSONBodyException();
                    }
                } else {
                    throw new AuthenticationException();
                }
            } catch (UWException e) {
                e.printStackTrace();
                objectNode.put(ParameterKey.STATUS, false);
                objectNode.put(ParameterKey.MESSAGE, e.getMessage());
                objectNode.put(ParameterKey.ERROR, e.getCode());
            }

            return ok(objectNode);
        });
    }

}
