package models.database;

import com.avaje.ebean.ExpressionList;
import models.classes.WantComment;

import java.util.List;

/**
 * Entidade para acesso ao Finder da entidade WantComment.class
 */
public class WantCommentFinder extends AbstractFinder<WantComment> implements IFinder<WantComment> {

    /**
     * Construtor para acesso apenas do FinderFactory.class
     */
    WantCommentFinder() {
        super(WantComment.class);
    }

    @Override
    public WantComment selectUnique(Long id) {
        ExpressionList<WantComment> expressionList = super.generateEqualExpressions(new String[] { ID }, new Object[] { id });
        return expressionList.findUnique();
    }

    @Override
    public WantComment selectUnique(String[] columns, Object[] columnsArgs) {
        ExpressionList<WantComment> expressionList = super.generateEqualExpressions(columns, columnsArgs);
        return (expressionList != null ? expressionList.findUnique() : null);
    }

    @Override
    public List<WantComment> selectAll() {
        return getFinder().findList();
    }

    @Override
    public List<WantComment> selectAll(String[] columns, Object[] columnsArgs) {
        ExpressionList<WantComment> expressionList = super.generateEqualExpressions(columns, columnsArgs);
        return (expressionList != null ? expressionList.findList() : null);
    }

    @Override
    public WantComment selectLast() {
        return getFinder().where().setMaxRows(1).setFirstRow(getFinder().findRowCount() - 1).findUnique();
    }

}
