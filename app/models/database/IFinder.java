package models.database;

import play.db.ebean.Model;

import java.util.List;

/**
 * Interface utilizada por todos os Finder para acesso ao banco.
 */
public interface IFinder<K> {

    /**
     * Select para uma entidade única baseado em seu id.
     * @param id
     * @return
     */
    K selectUnique(Long id);

    /**
     * Select para uma entidade única baseado em colunas e argumentos.
     * @param columns
     * @param columnsArgs
     * @return
     */
    K selectUnique(String[] columns, Object[] columnsArgs);

    /**
     * Select para todas as entidades em questão.
     * @return
     */
    List<K> selectAll();

    /**
     * Select de todas as entidades baseado em colunas e argumentos.
     * @param columns
     * @param columnsArgs
     * @return
     */
    List<K> selectAll(String[] columns, Object[] columnsArgs);

    /**
     * Select do último bean adicionado ao banco de dados.
     * @return
     */
    K selectLast();

    /**
     * Retorna a instancia do finder para executar queries mais complexas.
     * @return finder
     */
    Model.Finder<Long, K> getFinder();

}
