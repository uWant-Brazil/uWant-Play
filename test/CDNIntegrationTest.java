import models.cdn.CDNFactory;
import models.cdn.CDNType;
import models.cdn.ICDN;
import org.junit.*;
import play.Play;

import java.io.*;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.running;

/**
 * Created by felipebenezi on 07/06/14.
 */
public class CDNIntegrationTest {

    @Test
    public void CDNAsyncPutTest() {
        running(fakeApplication(), new Runnable() {

            @Override
            public void run()  {
                File file = Play.application().getFile("conf/cabeca.jpg");

                assertThat(file).isNotNull();
                assertThat(file.isFile()).isTrue();

                CDNFactory factory = CDNFactory.getInstance();
                assertThat(factory).isNotNull();

                CDNType[] values = CDNType.values();
                for (CDNType type : values) {
                    ICDN icdn = factory.get(type);
                    assertThat(icdn).isNotNull();
                    icdn.save(file);
                }
            }

        });
    }

}
