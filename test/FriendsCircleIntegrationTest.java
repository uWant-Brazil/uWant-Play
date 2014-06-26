import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.AbstractApplication;
import models.classes.Action;
import models.classes.Mobile;
import models.classes.Token;
import models.classes.User;
import models.database.FinderFactory;
import models.database.IFinder;
import org.junit.Test;
import play.Logger;
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

/**
 * Created by felipebenezi on 18/06/14.
 */
public class FriendsCircleIntegrationTest extends AbstractIntegrationTest {

    @Test
    public void joinCircleTest() {
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

                User userTarget = finder.selectUnique(Long.valueOf(2));
                List<Token> tokensTarget = userTarget.getTokens();
                Token tokenTarget;
                if (tokensTarget == null || tokensTarget.size() == 0) {
                    tokenTarget = new Token();
                    tokenTarget.setContent(UUID.randomUUID().toString());
                    tokenTarget.setUser(userTarget);
                    tokenTarget.setTarget(Token.Target.MOBILE);
                    tokenTarget.save();
                    tokenTarget.refresh();
                } else {
                    tokenTarget = tokensTarget.get(0);
                }

                ObjectNode body = Json.newObject();
                body.put(AbstractApplication.ParameterKey.LOGIN, userTarget.getLogin());

                FakeRequest fakeRequest = new FakeRequest(POST, "/v1/mobile/user/circle/join")
                        .withHeader(AbstractApplication.HeaderKey.HEADER_AUTHENTICATION_TOKEN, token.getContent())
                        .withJsonBody(body);
                Result result = route(fakeRequest);

                boolean status = (status(result) == OK);

                assertThat(result).isNotNull();
                assertThat(status).isTrue();

                String responseBody = new String(JavaResultExtractor.getBody((SimpleResult) result));
                JsonNode jsonResponse = Json.parse(responseBody);

                assertStatusMessage(jsonResponse, status);
                assertThat(jsonResponse.hasNonNull(AbstractApplication.ParameterKey.FRIENDS)).isTrue();

                // ---- Accepting ---

                body = Json.newObject();
                body.put(AbstractApplication.ParameterKey.LOGIN, user.getLogin());

                fakeRequest = new FakeRequest(POST, "/v1/mobile/user/circle/join")
                        .withHeader(AbstractApplication.HeaderKey.HEADER_AUTHENTICATION_TOKEN, tokenTarget.getContent())
                        .withJsonBody(body);
                result = route(fakeRequest);

                status = (status(result) == OK);

                assertThat(result).isNotNull();
                assertThat(status).isTrue();

                responseBody = new String(JavaResultExtractor.getBody((SimpleResult) result));
                jsonResponse = Json.parse(responseBody);

                assertStatusMessage(jsonResponse, status);
                assertThat(jsonResponse.hasNonNull(AbstractApplication.ParameterKey.FRIENDS)).isTrue();
            }

        });
    }
}
