package models.database;

import com.avaje.ebean.ExpressionList;
import models.classes.Token;
import models.classes.User;

import java.util.List;

/**
 * Entidade para acesso ao Finder da entidade User.class
 */
public class TokenFinder extends AbstractFinder<Token> implements IFinder<Token> {

    /**
     * Construtor para acesso apenas do FinderFactory.class
     */
    TokenFinder() {
        super(Token.class);
    }

    @Override
    public Token selectUnique(Long id) {
        ExpressionList<Token> expressionList = super.generateEqualExpressions(new String[] { ID }, new Object[] { id });
        return expressionList.findUnique();
    }

    @Override
    public Token selectUnique(String[] columns, Object[] columnsArgs) {
        ExpressionList<Token> expressionList = super.generateEqualExpressions(columns, columnsArgs);
        return (expressionList != null ? expressionList.findUnique() : null);
    }

    @Override
    public List<Token> selectAll() {
        return getFinder().findList();
    }

    @Override
    public List<Token> selectAll(String[] columns, Object[] columnsArgs) {
        ExpressionList<Token> expressionList = super.generateEqualExpressions(columns, columnsArgs);
        return (expressionList != null ? expressionList.findList() : null);
    }

}
