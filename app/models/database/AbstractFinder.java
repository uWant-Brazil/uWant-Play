package models.database;

import com.avaje.ebean.ExpressionList;
import controllers.AbstractApplication;
import play.db.ebean.Model;

/**
 * Classe abstrata utilizada para herança por todos os Finder's do sistema.
 */
abstract class AbstractFinder<K> {

    /**
     * Chave para id da tabela.
     */
    protected static final String ID = AbstractApplication.FinderKey.ID;

    /**
     * Entidade responsável pelo acesso direto no banco de uma determinada entidade.
     */
    private Model.Finder<Long, K> mFinder;

    /**
     * Construtor para criação do Finder baseado na entidade.
     * @param id
     */
    public AbstractFinder(Class<?> id) {
        this.mFinder = new Model.Finder(Long.class, id);
    }

    /**
     * Gera a expressão de equalidade entre coluna e argumentos.
     * @param columns
     * @param columnsArgs
     * @return expression
     */
    protected ExpressionList<K> generateEqualExpressions(String[] columns, Object[] columnsArgs) {
        ExpressionList<K> expressionList = null;
        for (int i = 0;i < columns.length;i++) {
            if (expressionList == null) {
                expressionList = mFinder.where();
            }

            String column = columns[i];
            Object columnArg = columnsArgs[i];
            expressionList = expressionList.eq(column, columnArg);
        }

        return expressionList;
    }

    /**
     * Retorna o finder criado para a entidade.
     * @return
     */
    public Model.Finder<Long, K> getFinder() {
        return this.mFinder;
    }

}
