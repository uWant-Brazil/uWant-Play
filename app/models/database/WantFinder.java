package models.database;

import com.avaje.ebean.ExpressionList;
import models.classes.Want;
import models.classes.WishList;

import java.util.List;

/**
 * Entidade para acesso ao Finder da entidade Token.class
 */
public class WantFinder extends AbstractFinder<Want> implements IFinder<Want> {

    /**
     * Construtor para acesso apenas do FinderFactory.class
     */
    WantFinder() {
        super(Want.class);
    }

    @Override
    public Want selectUnique(Long id) {
        ExpressionList<Want> expressionList = super.generateEqualExpressions(new String[] { ID }, new Object[] { id });
        return expressionList.findUnique();
    }

    @Override
    public Want selectUnique(String[] columns, Object[] columnsArgs) {
        ExpressionList<Want> expressionList = super.generateEqualExpressions(columns, columnsArgs);
        return (expressionList != null ? expressionList.findUnique() : null);
    }

    @Override
    public List<Want> selectAll() {
        return getFinder().findList();
    }

    @Override
    public List<Want> selectAll(String[] columns, Object[] columnsArgs) {
        ExpressionList<Want> expressionList = super.generateEqualExpressions(columns, columnsArgs);
        return (expressionList != null ? expressionList.findList() : null);
    }

    @Override
    public Want selectLast() {
        return getFinder().where().setMaxRows(1).setFirstRow(getFinder().findRowCount() - 1).findUnique();
    }

}
