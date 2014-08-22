package models.database;

import com.avaje.ebean.ExpressionList;
import models.classes.SocialProfile;

import java.util.List;

/**
 * Entidade para acesso ao Finder da entidade SocialProfile.Login.class
 */
public class SocialProfileLoginsFinder extends AbstractFinder<SocialProfile.Login> implements IFinder<SocialProfile.Login> {

    /**
     * Construtor para acesso apenas do FinderFactory.class
     */
    SocialProfileLoginsFinder() {
        super(SocialProfile.Login.class);
    }

    @Override
    public SocialProfile.Login selectUnique(Long id) {
        ExpressionList<SocialProfile.Login> expressionList = super.generateEqualExpressions(new String[] { ID }, new Object[] { id });
        return expressionList.findUnique();
    }

    @Override
    public SocialProfile.Login selectUnique(String[] columns, Object[] columnsArgs) {
        ExpressionList<SocialProfile.Login> expressionList = super.generateEqualExpressions(columns, columnsArgs);
        return (expressionList != null ? expressionList.findUnique() : null);
    }

    @Override
    public List<SocialProfile.Login> selectAll() {
        return getFinder().findList();
    }

    @Override
    public List<SocialProfile.Login> selectAll(String[] columns, Object[] columnsArgs) {
        ExpressionList<SocialProfile.Login> expressionList = super.generateEqualExpressions(columns, columnsArgs);
        return (expressionList != null ? expressionList.findList() : null);
    }

    @Override
    public SocialProfile.Login selectLast() {
        return getFinder().where().setMaxRows(1).setFirstRow(getFinder().findRowCount() - 1).findUnique();
    }

}
