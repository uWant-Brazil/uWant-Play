package models.database;

import com.avaje.ebean.ExpressionList;
import models.classes.Comment;

import java.util.List;

/**
 * Entidade para acesso ao Finder da entidade Comment.class
 */
public class CommentFinder extends AbstractFinder<Comment> implements IFinder<Comment> {

    /**
     * Construtor para acesso apenas do FinderFactory.class
     */
    CommentFinder() {
        super(Comment.class);
    }

    @Override
    public Comment selectUnique(Long id) {
        ExpressionList<Comment> expressionList = super.generateEqualExpressions(new String[] { ID }, new Object[] { id });
        return expressionList.findUnique();
    }

    @Override
    public Comment selectUnique(String[] columns, Object[] columnsArgs) {
        ExpressionList<Comment> expressionList = super.generateEqualExpressions(columns, columnsArgs);
        return (expressionList != null ? expressionList.findUnique() : null);
    }

    @Override
    public List<Comment> selectAll() {
        return getFinder().findList();
    }

    @Override
    public List<Comment> selectAll(String[] columns, Object[] columnsArgs) {
        ExpressionList<Comment> expressionList = super.generateEqualExpressions(columns, columnsArgs);
        return (expressionList != null ? expressionList.findList() : null);
    }

    @Override
    public Comment selectLast() {
        return getFinder().where().setMaxRows(1).setFirstRow(getFinder().findRowCount() - 1).findUnique();
    }

}
