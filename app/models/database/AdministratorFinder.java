package models.database;

import com.avaje.ebean.ExpressionList;
import models.classes.User;
import models.classes.admin.Administrator;

import java.util.List;

/**
 * Entidade para acesso ao Finder da entidade Administrator.class
 */
public class AdministratorFinder extends AbstractFinder<Administrator> implements IFinder<Administrator> {

    /**
     * Construtor para acesso apenas do FinderFactory.class
     */
    AdministratorFinder() {
        super(Administrator.class);
    }

    @Override
    public Administrator selectUnique(Long id) {
        ExpressionList<Administrator> expressionList = super.generateEqualExpressions(new String[] { ID }, new Object[] { id });
        return expressionList.findUnique();
    }

    @Override
    public Administrator selectUnique(String[] columns, Object[] columnsArgs) {
        ExpressionList<Administrator> expressionList = super.generateEqualExpressions(columns, columnsArgs);
        return (expressionList != null ? expressionList.findUnique() : null);
    }

    @Override
    public List<Administrator> selectAll() {
        return getFinder().findList();
    }

    @Override
    public List<Administrator> selectAll(String[] columns, Object[] columnsArgs) {
        ExpressionList<Administrator> expressionList = super.generateEqualExpressions(columns, columnsArgs);
        return (expressionList != null ? expressionList.findList() : null);
    }

    @Override
    public Administrator selectLast() {
        return getFinder().where().setMaxRows(1).setFirstRow(getFinder().findRowCount() - 1).findUnique();
    }

}
