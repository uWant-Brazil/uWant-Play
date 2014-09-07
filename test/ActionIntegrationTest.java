import com.avaje.ebean.ExpressionList;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.AbstractApplication;
import models.classes.Action;
import models.classes.Token;
import models.classes.User;
import models.database.FinderFactory;
import models.database.IFinder;
import org.junit.Test;
import play.core.j.JavaResultExtractor;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.SimpleResult;
import play.test.FakeRequest;

import java.util.List;
import java.util.UUID;

import static org.fest.assertions.Assertions.assertThat;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.*;
import static play.test.Helpers.status;

/**
 * Created by felipebenezi on 07/06/14.
 */
public class ActionIntegrationTest extends AbstractIntegrationTest {

    @Test
    public void actionCommentSuccessfulTest() {
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

                IFinder<Action> finderAction = factory.get(Action.class);
                ExpressionList<Action> expr = finderAction.getFinder().where();
                int count = expr.findRowCount();
                if (count == 0)
                    return;

                Action action = expr.setFirstRow(count - 1).setMaxRows(1).findUnique();

                ObjectNode body = Json.newObject();
                body.put(AbstractApplication.ParameterKey.ACTION_ID, action.getId());

                FakeRequest fakeRequest = new FakeRequest(POST, "/v1/mobile/action/report")
                        .withHeader(AbstractApplication.HeaderKey.HEADER_AUTHENTICATION_TOKEN, token.getContent())
                        .withJsonBody(body);
                Result result = route(fakeRequest);

                boolean status = (status(result) == OK);

                assertThat(result).isNotNull();
                assertThat(status).isTrue();

                String responseBody = new String(JavaResultExtractor.getBody(result, 10));
                JsonNode jsonResponse = Json.parse(responseBody);

                assertStatusMessage(jsonResponse, status);
            }

        });
    }

    @Test
    public void actionWantSuccessfulTest() {
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

                IFinder<Action> finderAction = factory.get(Action.class);
                ExpressionList<Action> expr = finderAction.getFinder().where();
                int count = expr.findRowCount();
                if (count == 0)
                    return;

                Action action = expr.setFirstRow(count - 1).setMaxRows(1).findUnique();

                ObjectNode body = Json.newObject();
                body.put(AbstractApplication.ParameterKey.ACTION_ID, action.getId());

                FakeRequest fakeRequest = new FakeRequest(POST, "/v1/mobile/action/want")
                        .withHeader(AbstractApplication.HeaderKey.HEADER_AUTHENTICATION_TOKEN, token.getContent())
                        .withJsonBody(body);
                Result result = route(fakeRequest);

                boolean status = (status(result) == OK);

                assertThat(result).isNotNull();
                assertThat(status).isTrue();

                String responseBody = new String(JavaResultExtractor.getBody(result, 10));
                JsonNode jsonResponse = Json.parse(responseBody);

                assertStatusMessage(jsonResponse, status);
            }

        });
    }

    @Test
    public void actionReportSuccessfulTest() {
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

                IFinder<Action> finderAction = factory.get(Action.class);
                ExpressionList<Action> expr = finderAction.getFinder().where();
                int count = expr.findRowCount();
                if (count == 0)
                    return;

                Action action = expr.setFirstRow(count - 1).setMaxRows(1).findUnique();

                ObjectNode body = Json.newObject();
                body.put(AbstractApplication.ParameterKey.ACTION_ID, action.getId());

                FakeRequest fakeRequest = new FakeRequest(POST, "/v1/mobile/action/report")
                        .withHeader(AbstractApplication.HeaderKey.HEADER_AUTHENTICATION_TOKEN, token.getContent())
                        .withJsonBody(body);
                Result result = route(fakeRequest);

                boolean status = (status(result) == OK);

                assertThat(result).isNotNull();
                assertThat(status).isTrue();

                String responseBody = new String(JavaResultExtractor.getBody(result, 10));
                JsonNode jsonResponse = Json.parse(responseBody);

                assertStatusMessage(jsonResponse, status);
            }

        });
    }

}
