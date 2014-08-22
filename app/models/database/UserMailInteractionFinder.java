package models.database;

import com.avaje.ebean.ExpressionList;
import models.classes.UserMailInteraction;

import java.util.List;

/**
 * Entidade para acesso ao Finder da entidade UserMailInteraction.class
 */
public class UserMailInteractionFinder extends AbstractFinder<UserMailInteraction> implements IFinder<UserMailInteraction> {

    /**
     * Construtor para acesso apenas do FinderFactory.class
     */
    UserMailInteractionFinder() {
        super(UserMailInteraction.class);
    }

    @Override
    public UserMailInteraction selectUnique(Long id) {
        ExpressionList<UserMailInteraction> expressionList = super.generateEqualExpressions(new String[] { ID }, new Object[] { id });
        return expressionList.findUnique();
    }

    @Override
    public UserMailInteraction selectUnique(String[] columns, Object[] columnsArgs) {
        ExpressionList<UserMailInteraction> expressionList = super.generateEqualExpressions(columns, columnsArgs);
        return (expressionList != null ? expressionList.findUnique() : null);
    }

    @Override
    public List<UserMailInteraction> selectAll() {
        return getFinder().findList();
    }

    @Override
    public List<UserMailInteraction> selectAll(String[] columns, Object[] columnsArgs) {
        ExpressionList<UserMailInteraction> expressionList = super.generateEqualExpressions(columns, columnsArgs);
        return (expressionList != null ? expressionList.findList() : null);
    }

    @Override
    public UserMailInteraction selectLast() {
        return getFinder().where().setMaxRows(1).setFirstRow(getFinder().findRowCount() - 1).findUnique();
    }

}
