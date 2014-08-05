package models.database;

import com.avaje.ebean.ExpressionList;
import models.classes.ActionShare;
import models.classes.WishList;

import java.util.List;

/**
 * Entidade para acesso ao Finder da entidade Token.class
 */
public class ActionShareFinder extends AbstractFinder<ActionShare> implements IFinder<ActionShare> {

    /**
     * Construtor para acesso apenas do FinderFactory.class
     */
    ActionShareFinder() {
        super(ActionShare.class);
    }

    @Override
    public ActionShare selectUnique(Long id) {
        ExpressionList<ActionShare> expressionList = super.generateEqualExpressions(new String[] { ID }, new Object[] { id });
        return expressionList.findUnique();
    }

    @Override
    public ActionShare selectUnique(String[] columns, Object[] columnsArgs) {
        ExpressionList<ActionShare> expressionList = super.generateEqualExpressions(columns, columnsArgs);
        return (expressionList != null ? expressionList.findUnique() : null);
    }

    @Override
    public List<ActionShare> selectAll() {
        return getFinder().findList();
    }

    @Override
    public List<ActionShare> selectAll(String[] columns, Object[] columnsArgs) {
        ExpressionList<ActionShare> expressionList = super.generateEqualExpressions(columns, columnsArgs);
        return (expressionList != null ? expressionList.findList() : null);
    }

    @Override
    public ActionShare selectLast() {
        return getFinder().where().setMaxRows(1).setFirstRow(getFinder().findRowCount() - 1).findUnique();
    }

}
