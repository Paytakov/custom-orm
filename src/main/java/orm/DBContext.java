package orm;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

public interface DBContext<E> {

    boolean persist(E entity) throws IllegalAccessException, SQLException;
    Iterable<E> find(Class<E> table) throws SQLException, InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException;
    Iterable<E> find(Class<E> table, String where) throws SQLException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException;
    E findFirst(Class<E> table) throws SQLException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException;
    E findFirst(Class<E> table, String where) throws SQLException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException;
    boolean delete(E toDelete) throws IllegalAccessException, SQLException;
}
