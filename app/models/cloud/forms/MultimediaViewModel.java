package models.cloud.forms;

import models.classes.Multimedia;

public class MultimediaViewModel {

    private String url;

    public MultimediaViewModel() {
    }

    public MultimediaViewModel(Multimedia multimedia) {
        this.url = multimedia.getUrl();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
