package models.database;

import com.avaje.ebean.ExpressionList;
import models.classes.ActionReport;

import java.util.List;

/**
 * Entidade para acesso ao Finder da entidade ActionReport.class
 */
public class ActionReportFinder extends AbstractFinder<ActionReport> implements IFinder<ActionReport> {

    /**
     * Construtor para acesso apenas do FinderFactory.class
     */
    ActionReportFinder() {
        super(ActionReport.class);
    }

    @Override
    public ActionReport selectUnique(Long id) {
        ExpressionList<ActionReport> expressionList = super.generateEqualExpressions(new String[] { ID }, new Object[] { id });
        return expressionList.findUnique();
    }

    @Override
    public ActionReport selectUnique(String[] columns, Object[] columnsArgs) {
        ExpressionList<ActionReport> expressionList = super.generateEqualExpressions(columns, columnsArgs);
        return (expressionList != null ? expressionList.findUnique() : null);
    }

    @Override
    public List<ActionReport> selectAll() {
        return getFinder().findList();
    }

    @Override
    public List<ActionReport> selectAll(String[] columns, Object[] columnsArgs) {
        ExpressionList<ActionReport> expressionList = super.generateEqualExpressions(columns, columnsArgs);
        return (expressionList != null ? expressionList.findList() : null);
    }

    @Override
    public ActionReport selectLast() {
        return getFinder().where().setMaxRows(1).setFirstRow(getFinder().findRowCount() - 1).findUnique();
    }

}
