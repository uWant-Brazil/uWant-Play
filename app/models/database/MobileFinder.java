package models.database;

import com.avaje.ebean.ExpressionList;
import models.classes.Mobile;

import java.util.List;

/**
 * Entidade para acesso ao Finder da entidade User.class
 */
public class MobileFinder extends AbstractFinder<Mobile> implements IFinder<Mobile> {

    /**
     * Construtor para acesso apenas do FinderFactory.class
     */
    MobileFinder() {
        super(Mobile.class);
    }

    @Override
    public Mobile selectUnique(Long id) {
        ExpressionList<Mobile> expressionList = super.generateEqualExpressions(new String[] { ID }, new Object[] { id });
        return expressionList.findUnique();
    }

    @Override
    public Mobile selectUnique(String[] columns, Object[] columnsArgs) {
        ExpressionList<Mobile> expressionList = super.generateEqualExpressions(columns, columnsArgs);
        return (expressionList != null ? expressionList.findUnique() : null);
    }

    @Override
    public List<Mobile> selectAll() {
        return getFinder().findList();
    }

    @Override
    public List<Mobile> selectAll(String[] columns, Object[] columnsArgs) {
        ExpressionList<Mobile> expressionList = super.generateEqualExpressions(columns, columnsArgs);
        return (expressionList != null ? expressionList.findList() : null);
    }

    @Override
    public Mobile selectLast() {
        return getFinder().where().setMaxRows(1).setFirstRow(getFinder().findRowCount() - 1).findUnique();
    }

}
