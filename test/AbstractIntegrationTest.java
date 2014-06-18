import com.fasterxml.jackson.databind.JsonNode;
import controllers.AbstractApplication;
import play.mvc.Result;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.header;

/**
 * Created by felipebenezi on 18/06/14.
 */
abstract class AbstractIntegrationTest {

    protected void assertAuthenticationHeader(Result result) {
        String authenticationToken = header(AbstractApplication.HeaderKey.HEADER_AUTHENTICATION_TOKEN, result);
        assertThat(authenticationToken).isNotNull().isNotEmpty();
    }

    protected void assertStatusMessage(JsonNode jsonResponse, boolean status) {
        assertThat(jsonResponse.hasNonNull(AbstractApplication.ParameterKey.STATUS)).isTrue();
        assertThat(jsonResponse.hasNonNull(AbstractApplication.ParameterKey.MESSAGE)).isTrue();

        JsonNode nodeStatus = jsonResponse.get(AbstractApplication.ParameterKey.STATUS);
        assertThat(nodeStatus.asBoolean() == status).isTrue();

        JsonNode nodeMessage = jsonResponse.get(AbstractApplication.ParameterKey.MESSAGE);
        assertThat(nodeMessage.asText()).isNotNull().isNotEmpty();
    }

}
