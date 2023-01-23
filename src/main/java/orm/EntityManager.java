package orm;

import annotation.Column;
import annotation.Entity;
import annotation.Id;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class EntityManager<E> implements DBContext<E> {

    private Connection connection;

    public EntityManager(Connection connection) {
        this.connection = connection;
    }

    public void doCreate(Class<E> entityClass) throws SQLException {
        String tableName = getTableName(entityClass);
        String fieldsWithTypes = getSQLFieldsWithTypes(entityClass);

        String createQuery = String.format("CREATE TABLE %s (" +
                        "id INT PRIMARY KEY AUTO_INCREMENT, %s)",
                tableName, fieldsWithTypes);

        PreparedStatement preparedStatement =
                connection.prepareStatement(createQuery);
        preparedStatement.execute();
    }

    @Override
    public boolean persist(E entity) throws IllegalAccessException, SQLException {
        Field primaryKey = getIdColumn(entity.getClass());
        primaryKey.setAccessible(true);
        Object idValue = primaryKey.get(entity);

        if (idValue == null || (long) idValue <= 0) {
            return doInsert(entity, primaryKey);
        }

        return doUpdate(entity, primaryKey);
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

    private Field getIdColumn(Class<?> entity) {
        return Arrays.stream(entity.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(Id.class))
                .findFirst()
                .orElseThrow(() -> new UnsupportedOperationException(
                        "Entity does not have primary key!"));
    }

    private String getTableName(Class<?> aClass) {
        Entity[] entityAnnotations = aClass.getAnnotationsByType(Entity.class);

        if (entityAnnotations.length == 0) {
            throw new UnsupportedOperationException("Class must be Entity!");
        }

        return entityAnnotations[0].name();
    }

    private String getSQLFieldsWithTypes(Class<E> entityClass) {
        return Arrays.stream(entityClass.getDeclaredFields())
                .filter(f -> !f.isAnnotationPresent(Id.class))
                .filter(f -> f.isAnnotationPresent(Column.class))
                .map(field -> {
                    String fieldName = field.getAnnotationsByType(Column.class)[0].name();
                    Class<?> type = field.getType();
                    String sqlType = getSQLType(type);

                    return fieldName + " " + sqlType;
                })
                .collect(Collectors.joining(","));

    }

    private String getSQLType(Class<?> fieldType) {
        String type = "";
        if (fieldType == Integer.class || fieldType == int.class) {
            type = "INT";
        } else if (fieldType == String.class) {
            type = "VARCHAR(200)";
        } else if (fieldType == LocalDate.class) {
            type = "DATE";
        }

        return type;
    }

    private boolean doUpdate(E entity, Field primaryKey) {
        return false;
    }

    private boolean doInsert(E entity, Field primaryKey) throws IllegalAccessException, SQLException {
        String tableName = getTableName(entity.getClass());
        String tableFields = getColumnsWithoutId(entity.getClass());
        String columnValues = getColumnValuesWithoutId(entity);

        String insertQuery = String.format(
                "INSERT INTO %s (%s) VALUES (%s)",
                tableName,
                tableFields,
                columnValues
        );

        return connection.prepareStatement(insertQuery).execute();
    }

    private String getColumnValuesWithoutId(E entity) throws IllegalAccessException {
        Class<?> aClass = entity.getClass();
        List<Field> fields = Arrays.stream(aClass.getDeclaredFields())
                .filter(f -> !f.isAnnotationPresent(Id.class))
                .filter(f -> f.isAnnotationPresent(Column.class))
                .toList();

        List<String> values = new ArrayList<>();
        for (Field field : fields) {
            field.setAccessible(true);
            Object value = field.get(entity);

            if (value instanceof String ||
                    value instanceof LocalDate) {
                values.add("'" + value + "'");
            } else {
                values.add(value.toString());
            }
        }

        return String.join(",", values);
    }

    private String getColumnsWithoutId(Class<?> aClass) {
        return Arrays.stream(aClass.getDeclaredFields())
                .filter(f -> !f.isAnnotationPresent(Id.class))
                .filter(f -> f.isAnnotationPresent(Column.class))
                .map(f -> f.getAnnotationsByType(Column.class))
                .map(a -> a[0].name())
                .collect(Collectors.joining(","));
    }
}
