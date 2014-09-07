package utils;

import controllers.AbstractApplication;
import models.classes.admin.Administrator;
import models.database.FinderFactory;
import models.database.IFinder;

public abstract class AdminUtil {

    public static Administrator get(String token) {
        FinderFactory factory = FinderFactory.getInstance();
        IFinder<Administrator> finder = factory.get(Administrator.class);
        return finder.selectUnique(new String[] { AbstractApplication.FinderKey.TOKEN }, new Object[] { token });
    }

    /**
     * Classe estática para definição das regras para os usuários administradores.
     * Cada regra está separada por contexto de permissão.
     */
    public static final class Roles {

        /**
         * Contexto padrão para definição de um "deus", ops, digo administrador! :)
         */
        public static final String GOD = "role.god";

        /**
         * Contexto para notificações entre o sistema e os usuários.
         */
        public static final class Notification {
            public static final String SEND = "role.notification.send";
            public static final String SEND_SCHEDULE = "role.notification.send_schedule";
        }

    }

}
