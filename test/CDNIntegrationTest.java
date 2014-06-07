import models.cdn.CDNFactory;
import models.cdn.CDNType;
import models.cdn.ICDN;
import org.junit.*;

import java.io.File;
import java.io.IOException;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Created by felipebenezi on 07/06/14.
 */
public class CDNIntegrationTest {

    @Test
    public void CDNAsyncPutTest() {
        File file = new File(""); // TODO Definir a pasta para criar o arquivo.
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        assertThat(file).isNotNull();
        assertThat(file.isFile()).isTrue();

        CDNFactory factory = CDNFactory.getInstance();
        assertThat(factory).isNotNull();

        CDNType[] values = CDNType.values();
        for (CDNType type : values) {
            ICDN icdn = factory.get(type);
            assertThat(icdn).isNotNull();
            icdn.asyncPut(file);
        }
    }

}
