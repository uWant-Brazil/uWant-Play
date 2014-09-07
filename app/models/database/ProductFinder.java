package models.database;

import com.avaje.ebean.ExpressionList;
import models.classes.Product;

import java.util.List;

/**
 * Entidade para acesso ao Finder da entidade Product.class
 */
public class ProductFinder extends AbstractFinder<Product> implements IFinder<Product> {

    /**
     * Construtor para acesso apenas do FinderFactory.class
     */
    ProductFinder() {
        super(Product.class);
    }

    @Override
    public Product selectUnique(Long id) {
        ExpressionList<Product> expressionList = super.generateEqualExpressions(new String[] { ID }, new Object[] { id });
        return expressionList.findUnique();
    }

    @Override
    public Product selectUnique(String[] columns, Object[] columnsArgs) {
        ExpressionList<Product> expressionList = super.generateEqualExpressions(columns, columnsArgs);
        return (expressionList != null ? expressionList.findUnique() : null);
    }

    @Override
    public List<Product> selectAll() {
        return getFinder().findList();
    }

    @Override
    public List<Product> selectAll(String[] columns, Object[] columnsArgs) {
        ExpressionList<Product> expressionList = super.generateEqualExpressions(columns, columnsArgs);
        return (expressionList != null ? expressionList.findList() : null);
    }

    @Override
    public Product selectLast() {
        return getFinder().where().setMaxRows(1).setFirstRow(getFinder().findRowCount() - 1).findUnique();
    }

}
