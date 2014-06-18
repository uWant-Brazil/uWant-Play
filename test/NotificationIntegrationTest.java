import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.AbstractApplication;
import models.classes.Mobile;
import models.classes.Token;
import models.classes.User;
import models.database.FinderFactory;
import models.database.IFinder;
import org.junit.Test;
import play.core.j.JavaResultExtractor;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.SimpleResult;
import play.test.FakeRequest;

import java.util.List;
import java.util.UUID;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.*;
import static play.test.Helpers.status;

/**
 * Created by felipebenezi on 18/06/14.
 */
public class NotificationIntegrationTest extends AbstractIntegrationTest {

    @Test
    public void registerMobileTest() {
        running(fakeApplication(), new Runnable() {

            @Override
            public void run() {
                FinderFactory factory = FinderFactory.getInstance();
                IFinder<User> finder = factory.get(User.class);
                User user = finder.selectUnique(Long.valueOf(1));
                List<Token> tokens = user.getTokens();
                Token token;
                if (tokens == null || tokens.size() == 0) {
                    token = new Token();
                    token.setContent(UUID.randomUUID().toString());
                    token.setUser(user);
                    token.setTarget(Token.Target.MOBILE);
                    token.save();
                    token.refresh();
                } else {
                    token = tokens.get(0);
                }

                String identifier = UUID.randomUUID().toString();
                int osOrdinal = Mobile.OS.ANDROID.ordinal();

                ObjectNode body = Json.newObject();
                body.put(AbstractApplication.ParameterKey.MOBILE_IDENTIFIER, identifier);
                body.put(AbstractApplication.ParameterKey.OS, osOrdinal);

                FakeRequest fakeRequest = new FakeRequest(POST, "/v1/mobile/notification/register")
                        .withHeader(AbstractApplication.HeaderKey.HEADER_AUTHENTICATION_TOKEN, token.getContent())
                        .withJsonBody(body);
                Result result = route(fakeRequest);

                boolean status = (status(result) == Http.Status.OK);

                assertThat(result).isNotNull();
                assertThat(status).isTrue();

                String responseBody = new String(JavaResultExtractor.getBody((SimpleResult) result));
                JsonNode jsonResponse = Json.parse(responseBody);

                assertStatusMessage(jsonResponse, status);
            }

        });
    }

}
