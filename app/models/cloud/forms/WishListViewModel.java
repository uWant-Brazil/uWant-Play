package models.cloud.forms;

import models.classes.WishList;
import models.classes.WishListProduct;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WishListViewModel {

    private String title;
    private String UUID;
    private String description;
    private List<ProductViewModel> products;
    private Date since;

    public WishListViewModel() {
    }

    public WishListViewModel(WishList wishlist) {
        this.title = wishlist.getTitle();
        this.description = wishlist.getDescription();
        this.UUID = wishlist.getUUID();
        // TODO this.since = wishlist.getSince();

        List<ProductViewModel> products = new ArrayList<>();
        List<WishListProduct> ps = wishlist.getWishLists();
        if (ps != null && ps.size() > 0) {
            for (WishListProduct wp : ps) {
                if (wp.getStatus() == WishListProduct.Status.ACTIVE) {
                    products.add(new ProductViewModel(wp.getProduct()));
                }
            }
        }
        this.products = products;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<ProductViewModel> getProducts() {
        return products;
    }

    public void setProducts(List<ProductViewModel> products) {
        this.products = products;
    }

    public Date getSince() {
        return since;
    }

    public void setSince(Date since) {
        this.since = since;
    }
}
