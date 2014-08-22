package models.database;

import com.avaje.ebean.ExpressionList;
import models.classes.Action;

import java.util.List;

/**
 * Entidade para acesso ao Finder da entidade Action.class
 */
public class ActionFinder extends AbstractFinder<Action> implements IFinder<Action> {

    /**
     * Construtor para acesso apenas do FinderFactory.class
     */
    ActionFinder() {
        super(Action.class);
    }

    @Override
    public Action selectUnique(Long id) {
        ExpressionList<Action> expressionList = super.generateEqualExpressions(new String[] { ID }, new Object[] { id });
        return expressionList.findUnique();
    }

    @Override
    public Action selectUnique(String[] columns, Object[] columnsArgs) {
        ExpressionList<Action> expressionList = super.generateEqualExpressions(columns, columnsArgs);
        return (expressionList != null ? expressionList.findUnique() : null);
    }

    @Override
    public List<Action> selectAll() {
        return getFinder().findList();
    }

    @Override
    public List<Action> selectAll(String[] columns, Object[] columnsArgs) {
        ExpressionList<Action> expressionList = super.generateEqualExpressions(columns, columnsArgs);
        return (expressionList != null ? expressionList.findList() : null);
    }

    @Override
    public Action selectLast() {
        return getFinder().where().setMaxRows(1).setFirstRow(getFinder().findRowCount() - 1).findUnique();
    }

}
