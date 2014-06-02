import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.AbstractApplication;
import models.classes.SocialProfile;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.UUID;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.*;

public class IntegrationTest {

    @Test
    public void successfulRegisterTest() {
        running(fakeApplication(), new Runnable() {

            @Override
            public void run() {
                Calendar now = Calendar.getInstance();
                now.set(Calendar.YEAR, (1930 + (int)(Math.random() * (now.get(Calendar.YEAR) - 1930) + 1)));
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                String birthday = formatter.format(now.getTime());

                int gender = (now.getTimeInMillis() % 8 < 5 ? User.Gender.MALE.ordinal() : User.Gender.FEMALE.ordinal());

                String uniqueIdentifier = UUID.randomUUID().toString();

                ObjectNode body = Json.newObject();
                body.put(AbstractApplication.ParameterKey.LOGIN, "test" + uniqueIdentifier);
                body.put(AbstractApplication.ParameterKey.PASSWORD, uniqueIdentifier);
                body.put(AbstractApplication.ParameterKey.FULL_NAME, "Teste " + uniqueIdentifier);
                body.put(AbstractApplication.ParameterKey.BIRTHDAY, birthday);
                body.put(AbstractApplication.ParameterKey.GENDER, gender);
                body.put(AbstractApplication.ParameterKey.MAIL, "mail-" + uniqueIdentifier + "@uwant-test.com.br");

                FakeRequest fakeRequest = new FakeRequest(POST, "/v1/mobile/user/register").withJsonBody(body);
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

    @Test
    public void successfulRegisterWithSocialTest() {
        running(fakeApplication(), new Runnable() {

            @Override
            public void run() {
                Calendar now = Calendar.getInstance();
                now.set(Calendar.YEAR, (1930 + (int)(Math.random() * (now.get(Calendar.YEAR) - 1930) + 1)));
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                String birthday = formatter.format(now.getTime());

                int gender = (now.getTimeInMillis() % 8 < 5 ? User.Gender.MALE.ordinal() : User.Gender.FEMALE.ordinal());

                String uniqueIdentifier = UUID.randomUUID().toString();

                ObjectNode body = Json.newObject();
                body.put(AbstractApplication.ParameterKey.LOGIN, "test" + uniqueIdentifier);
                body.put(AbstractApplication.ParameterKey.PASSWORD, uniqueIdentifier);
                body.put(AbstractApplication.ParameterKey.FULL_NAME, "Teste " + uniqueIdentifier);
                body.put(AbstractApplication.ParameterKey.BIRTHDAY, birthday);
                body.put(AbstractApplication.ParameterKey.GENDER, gender);
                body.put(AbstractApplication.ParameterKey.MAIL, "mail-" + uniqueIdentifier + "@uwant-test.com.br");

                ObjectNode jsonSocialProfile = Json.newObject();
                jsonSocialProfile.put(AbstractApplication.ParameterKey.SOCIAL_PROVIDER, SocialProfile.Provider.FACEBOOK.ordinal());
                jsonSocialProfile.put(AbstractApplication.ParameterKey.TOKEN, uniqueIdentifier);

                body.put(AbstractApplication.ParameterKey.SOCIAL_PROFILE, jsonSocialProfile);

                FakeRequest fakeRequest = new FakeRequest(POST, "/v1/mobile/user/register").withJsonBody(body);
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

    @Test
    public void successfulLoginTest() {
        running(fakeApplication(), new Runnable() {

            @Override
            public void run() {
                FinderFactory factory = FinderFactory.getInstance();
                IFinder<User> finder = factory.get(User.class);
                User user = finder.selectLast();

                String login = user.getLogin();
                String password = user.getPassword();

                ObjectNode body = Json.newObject();
                body.put(AbstractApplication.ParameterKey.LOGIN, login);
                body.put(AbstractApplication.ParameterKey.PASSWORD, password);

                FakeRequest fakeRequest = new FakeRequest(POST, "/v1/mobile/authorize").withJsonBody(body);
                Result result = route(fakeRequest);

                boolean status = (status(result) == Http.Status.OK);

                assertThat(result).isNotNull();
                assertThat(status).isTrue();

                assertAuthenticationHeader(result);

                String responseBody = new String(JavaResultExtractor.getBody((SimpleResult) result));
                JsonNode jsonResponse = Json.parse(responseBody);

                assertStatusMessage(jsonResponse, status);
            }

        });
    }

    private void assertAuthenticationHeader(Result result) {
        String authenticationToken = header(AbstractApplication.HeaderKey.HEADER_AUTHENTICATION_TOKEN, result);
        assertThat(authenticationToken).isNotNull().isNotEmpty();
    }

    private void assertStatusMessage(JsonNode jsonResponse, boolean status) {
        assertThat(jsonResponse.hasNonNull(AbstractApplication.ParameterKey.STATUS)).isTrue();
        assertThat(jsonResponse.hasNonNull(AbstractApplication.ParameterKey.MESSAGE)).isTrue();

        JsonNode nodeStatus = jsonResponse.get(AbstractApplication.ParameterKey.STATUS);
        assertThat(nodeStatus.asBoolean() == status).isTrue();

        JsonNode nodeMessage = jsonResponse.get(AbstractApplication.ParameterKey.MESSAGE);
        assertThat(nodeMessage.asText()).isNotNull().isNotEmpty();
    }

}
