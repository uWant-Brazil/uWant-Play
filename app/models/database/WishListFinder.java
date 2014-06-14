package models.database;

import com.avaje.ebean.ExpressionList;
import models.classes.Token;
import models.classes.WishList;

import java.util.List;

/**
 * Entidade para acesso ao Finder da entidade Token.class
 */
public class WishListFinder extends AbstractFinder<WishList> implements IFinder<WishList> {

    /**
     * Construtor para acesso apenas do FinderFactory.class
     */
    WishListFinder() {
        super(WishList.class);
    }

    @Override
    public WishList selectUnique(Long id) {
        ExpressionList<WishList> expressionList = super.generateEqualExpressions(new String[] { ID }, new Object[] { id });
        return expressionList.findUnique();
    }

    @Override
    public WishList selectUnique(String[] columns, Object[] columnsArgs) {
        ExpressionList<WishList> expressionList = super.generateEqualExpressions(columns, columnsArgs);
        return (expressionList != null ? expressionList.findUnique() : null);
    }

    @Override
    public List<WishList> selectAll() {
        return getFinder().findList();
    }

    @Override
    public List<WishList> selectAll(String[] columns, Object[] columnsArgs) {
        ExpressionList<WishList> expressionList = super.generateEqualExpressions(columns, columnsArgs);
        return (expressionList != null ? expressionList.findList() : null);
    }

    @Override
    public WishList selectLast() {
        return getFinder().where().setMaxRows(1).setFirstRow(getFinder().findRowCount() - 1).findUnique();
    }

}
