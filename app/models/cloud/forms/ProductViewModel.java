package models.cloud.forms;

import models.classes.Product;

public class ProductViewModel {

    private long id;
    private String name;
    private String description;
    private MultimediaViewModel picture;

    public ProductViewModel() {
    }

    public ProductViewModel(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        // TODO this.description = product.getDescription();
        this.picture = new MultimediaViewModel(product.getMultimedia());
    }

    public long getId() {
        return id;
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

    public MultimediaViewModel getPicture() {
        return picture;
    }
}
