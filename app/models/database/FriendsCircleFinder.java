package models.database;

import com.avaje.ebean.ExpressionList;
import models.classes.FriendsCircle;

import java.util.List;

/**
 * Entidade para acesso ao Finder da entidade FriendsCircle.class
 */
public class FriendsCircleFinder extends AbstractFinder<FriendsCircle> implements IFinder<FriendsCircle> {

    /**
     * Construtor para acesso apenas do FinderFactory.class
     */
    FriendsCircleFinder() {
        super(FriendsCircle.class);
    }

    @Override
    public FriendsCircle selectUnique(Long id) {
        ExpressionList<FriendsCircle> expressionList = super.generateEqualExpressions(new String[] { ID }, new Object[] { id });
        return expressionList.findUnique();
    }

    @Override
    public FriendsCircle selectUnique(String[] columns, Object[] columnsArgs) {
        ExpressionList<FriendsCircle> expressionList = super.generateEqualExpressions(columns, columnsArgs);
        return (expressionList != null ? expressionList.findUnique() : null);
    }

    @Override
    public List<FriendsCircle> selectAll() {
        return getFinder().findList();
    }

    @Override
    public List<FriendsCircle> selectAll(String[] columns, Object[] columnsArgs) {
        ExpressionList<FriendsCircle> expressionList = super.generateEqualExpressions(columns, columnsArgs);
        return (expressionList != null ? expressionList.findList() : null);
    }

    @Override
    public FriendsCircle selectLast() {
        return getFinder().where().setMaxRows(1).setFirstRow(getFinder().findRowCount() - 1).findUnique();
    }

}
