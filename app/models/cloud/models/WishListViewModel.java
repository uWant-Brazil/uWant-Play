package models.cloud.models;

import play.data.format.Formats;
import play.data.validation.Constraints;

public class WishListViewModel {

    private static final String REGEX_UUID = "[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}";

    @Constraints.Required
    @Constraints.Pattern(value = REGEX_UUID)
    @Formats.NonEmpty
    private String UUID;

    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

}
