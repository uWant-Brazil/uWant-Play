package models.database;

import com.avaje.ebean.ExpressionList;
import models.classes.SocialProfile;

import java.util.List;

/**
 * Entidade para acesso ao Finder da entidade User.class
 */
public class SocialProfileFinder extends AbstractFinder<SocialProfile> implements IFinder<SocialProfile> {

    /**
     * Construtor para acesso apenas do FinderFactory.class
     */
    SocialProfileFinder() {
        super(SocialProfile.class);
    }

    @Override
    public SocialProfile selectUnique(Long id) {
        ExpressionList<SocialProfile> expressionList = super.generateEqualExpressions(new String[] { ID }, new Object[] { id });
        return expressionList.findUnique();
    }

    @Override
    public SocialProfile selectUnique(String[] columns, Object[] columnsArgs) {
        ExpressionList<SocialProfile> expressionList = super.generateEqualExpressions(columns, columnsArgs);
        return (expressionList != null ? expressionList.findUnique() : null);
    }

    @Override
    public List<SocialProfile> selectAll() {
        return getFinder().findList();
    }

    @Override
    public List<SocialProfile> selectAll(String[] columns, Object[] columnsArgs) {
        ExpressionList<SocialProfile> expressionList = super.generateEqualExpressions(columns, columnsArgs);
        return (expressionList != null ? expressionList.findList() : null);
    }

    @Override
    public SocialProfile selectLast() {
        return getFinder().where().setMaxRows(1).setFirstRow(getFinder().findRowCount() - 1).findUnique();
    }

}
