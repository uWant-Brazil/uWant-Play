package models.database;

import com.avaje.ebean.ExpressionList;
import models.classes.WishListProduct;

import java.util.List;

/**
 * Entidade para acesso ao Finder da entidade WishListProduct.class
 */
public class WishListProductFinder extends AbstractFinder<WishListProduct> implements IFinder<WishListProduct> {

    /**
     * Construtor para acesso apenas do FinderFactory.class
     */
    WishListProductFinder() {
        super(WishListProduct.class);
    }

    @Override
    public WishListProduct selectUnique(Long id) {
        ExpressionList<WishListProduct> expressionList = super.generateEqualExpressions(new String[] { ID }, new Object[] { id });
        return expressionList.findUnique();
    }

    @Override
    public WishListProduct selectUnique(String[] columns, Object[] columnsArgs) {
        ExpressionList<WishListProduct> expressionList = super.generateEqualExpressions(columns, columnsArgs);
        return (expressionList != null ? expressionList.findUnique() : null);
    }

    @Override
    public List<WishListProduct> selectAll() {
        return getFinder().findList();
    }

    @Override
    public List<WishListProduct> selectAll(String[] columns, Object[] columnsArgs) {
        ExpressionList<WishListProduct> expressionList = super.generateEqualExpressions(columns, columnsArgs);
        return (expressionList != null ? expressionList.findList() : null);
    }

    @Override
    public WishListProduct selectLast() {
        return getFinder().where().setMaxRows(1).setFirstRow(getFinder().findRowCount() - 1).findUnique();
    }

}
