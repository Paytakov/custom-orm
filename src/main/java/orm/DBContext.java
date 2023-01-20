package orm;

import java.util.Iterator;

public interface DBContext<E> {

    boolean persist(E entity);
    Iterator<E> find(Class<E> table);
    Iterator<E> find(Class<E> table, String where);
    E findFirst(Class<E> table);
    E findFirst(Class<E> table, String where);
}
