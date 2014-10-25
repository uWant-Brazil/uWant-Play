package controllers.mobile;

import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.AbstractApplication;
import models.classes.Multimedia;
import models.classes.Product;
import models.classes.User;
import models.database.FinderFactory;
import models.database.IFinder;
import models.exceptions.AuthenticationException;
import models.exceptions.MultipartBodyException;
import models.exceptions.UWException;
import play.filters.csrf.AddCSRFToken;
import play.i18n.Messages;
import play.libs.F;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;
import security.MobileAuthenticator;
import utils.CDNUtil;
import utils.UserUtil;

import java.io.File;
import java.util.Map;

/**
 * Controlador responsável pelo tratamento de requisições mobile para CDN.
 */
@Security.Authenticated(MobileAuthenticator.class)
public class CDNController extends AbstractApplication {

    /**
     * Método responsável por realizar o tratamento do arquivo para envio a CDN.
     * Após o envio do arquivo, o sistema irá registrar uma entidade Multimedia para
     * consultas futuras pelos usuários.
     * @return JSON
     */
    public static F.Promise<Result> retrieve() {
        return F.Promise.<Result>promise(() -> {
            final ObjectNode jsonResponse = Json.newObject();
            try {
                final User user = authenticateToken();
                if (user != null && UserUtil.isAvailable(user)) {
                    String contentType = request().getHeader(HeaderKey.CONTENT_TYPE);
                    if (contentType.startsWith(HeaderKey.MULTIPART_FORM_DATA)) {
                        Http.MultipartFormData multipartFormData = request().body().asMultipartFormData();
                        Map<String, String[]> formEncoded = multipartFormData.asFormUrlEncoded();

                        if (formEncoded != null) {
                            Http.MultipartFormData.FilePart filePart = multipartFormData.getFile(ParameterKey.MULTIMEDIA);
                            final File file = filePart.getFile();

                            Multimedia multimedia;
                            if (formEncoded.containsKey(ParameterKey.MULTIMEDIA_PRODUCT)) {
                                long productId = Long.valueOf(formEncoded.get(ParameterKey.MULTIMEDIA_PRODUCT)[0]);

                                FinderFactory factory = FinderFactory.getInstance();
                                IFinder<Product> finder = factory.get(Product.class);
                                final Product product = finder.selectUnique(productId);

                                multimedia = CDNUtil.sendFile(file);
                                product.setMultimedia(multimedia);
                                product.update();
                            } else if (formEncoded.containsKey(ParameterKey.MULTIMEDIA_USER_PICTURE)) {
                                multimedia = CDNUtil.sendFile(file);
                                user.setPicture(multimedia);
                                user.update();
                            } else {
                                throw new MultipartBodyException();
                            }

                            jsonResponse.put(ParameterKey.STATUS, true);
                            jsonResponse.put(ParameterKey.MESSAGE, Messages.get(MessageKey.CDN.RETRIEVE_SUCCESS));
                            jsonResponse.put(ParameterKey.MULTIMEDIA, Json.toJson(multimedia));
                        } else {
                            throw new MultipartBodyException();
                        }
                    } else {
                        throw new MultipartBodyException();
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
        });
    }

}
