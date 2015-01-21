package models.database;

import com.avaje.ebean.ExpressionList;
import models.classes.Manufacturer;

import java.util.List;

/**
 * Entidade para acesso ao Finder da entidade Manufacturer.class
 */
public class ManufacturerFinder extends AbstractFinder<Manufacturer> implements IFinder<Manufacturer> {

    /**
     * Construtor para acesso apenas do FinderFactory.class
     */
    ManufacturerFinder() {
        super(Manufacturer.class);
    }

    @Override
    public Manufacturer selectUnique(Long id) {
        ExpressionList<Manufacturer> expressionList = super.generateEqualExpressions(new String[] { ID }, new Object[] { id });
        return expressionList.findUnique();
    }

    @Override
    public Manufacturer selectUnique(String[] columns, Object[] columnsArgs) {
        ExpressionList<Manufacturer> expressionList = super.generateEqualExpressions(columns, columnsArgs);
        return (expressionList != null ? expressionList.findUnique() : null);
    }

    @Override
    public List<Manufacturer> selectAll() {
        return getFinder().findList();
    }

    @Override
    public List<Manufacturer> selectAll(String[] columns, Object[] columnsArgs) {
        ExpressionList<Manufacturer> expressionList = super.generateEqualExpressions(columns, columnsArgs);
        return (expressionList != null ? expressionList.findList() : null);
    }

    @Override
    public Manufacturer selectLast() {
        return getFinder().where().setMaxRows(1).setFirstRow(getFinder().findRowCount() - 1).findUnique();
    }

}
