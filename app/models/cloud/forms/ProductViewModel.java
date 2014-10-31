package models.cloud.forms;

import models.classes.Product;

public class ProductViewModel {

    private String name;
    private String description;
    private MultimediaViewModel multimedia;

    public ProductViewModel() {
    }

    public ProductViewModel(Product product) {
        this.name = product.getName();
        // TODO this.description = product.getDescription();
        this.multimedia = new MultimediaViewModel(product.getMultimedia());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public MultimediaViewModel getMultimedia() {
        return multimedia;
    }

    public void setMultimedia(MultimediaViewModel multimedia) {
        this.multimedia = multimedia;
    }
}
