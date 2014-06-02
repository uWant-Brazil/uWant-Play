import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.AbstractApplication;
import models.classes.User;
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

    private void assertStatusMessage(JsonNode jsonResponse, boolean status) {
        assertThat(jsonResponse.hasNonNull(AbstractApplication.ParameterKey.STATUS)).isTrue();
        assertThat(jsonResponse.hasNonNull(AbstractApplication.ParameterKey.MESSAGE)).isTrue();

        JsonNode nodeStatus = jsonResponse.get(AbstractApplication.ParameterKey.STATUS);
        assertThat(nodeStatus.asBoolean() == status).isTrue();

        JsonNode nodeMessage = jsonResponse.get(AbstractApplication.ParameterKey.MESSAGE);
        assertThat(nodeMessage.asText()).isNotNull().isNotEmpty();
    }

}
