package orm;

import java.sql.Connection;
import java.util.Iterator;

public class EntityManager<E> implements DBContext<E> {

    private Connection connection;

    public EntityManager(Connection connection) {
        this.connection = connection;
    }

    @Override
    public boolean persist(E entity) {
        return false;
    }

    @Override
    public Iterator<E> find(Class<E> table) {
        return null;
    }

    @Override
    public Iterator<E> find(Class<E> table, String where) {
        return null;
    }

    @Override
    public E findFirst(Class<E> table) {
        return null;
    }

    @Override
    public E findFirst(Class<E> table, String where) {
        return null;
    }
}
