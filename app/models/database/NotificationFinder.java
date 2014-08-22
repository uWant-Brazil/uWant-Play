package models.database;

import com.avaje.ebean.ExpressionList;
import models.classes.Notification;

import java.util.List;

/**
 * Entidade para acesso ao Finder da entidade Notification.class
 */
public class NotificationFinder extends AbstractFinder<Notification> implements IFinder<Notification> {

    /**
     * Construtor para acesso apenas do FinderFactory.class
     */
    NotificationFinder() {
        super(Notification.class);
    }

    @Override
    public Notification selectUnique(Long id) {
        ExpressionList<Notification> expressionList = super.generateEqualExpressions(new String[] { ID }, new Object[] { id });
        return expressionList.findUnique();
    }

    @Override
    public Notification selectUnique(String[] columns, Object[] columnsArgs) {
        ExpressionList<Notification> expressionList = super.generateEqualExpressions(columns, columnsArgs);
        return (expressionList != null ? expressionList.findUnique() : null);
    }

    @Override
    public List<Notification> selectAll() {
        return getFinder().findList();
    }

    @Override
    public List<Notification> selectAll(String[] columns, Object[] columnsArgs) {
        ExpressionList<Notification> expressionList = super.generateEqualExpressions(columns, columnsArgs);
        return (expressionList != null ? expressionList.findList() : null);
    }

    @Override
    public Notification selectLast() {
        return getFinder().where().setMaxRows(1).setFirstRow(getFinder().findRowCount() - 1).findUnique();
    }

}
