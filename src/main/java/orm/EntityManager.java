package orm;

import annotation.Column;
import annotation.Entity;
import annotation.Id;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

    public void doAlter(Class<E> entityClass) throws SQLException {
        String tableName = getTableName(entityClass);
        String addColumnStatements = getAddColumnStatementsForNewFields(entityClass);

        String alterQuery = String.format("ALTER TABLE %s %s",
                tableName,
                addColumnStatements);

        PreparedStatement preparedStatement =
                connection.prepareStatement(alterQuery);
        preparedStatement.execute();
    }

    @Override
    public boolean persist(E entity) throws IllegalAccessException, SQLException {
        Field primaryKey = getIdColumn(entity.getClass());
        primaryKey.setAccessible(true);
        Object idValue = primaryKey.get(entity);

        if (idValue == null || (long) idValue <= 0) {
            return doInsert(entity);
        }

        return doUpdate(entity, (long) idValue);
    }

    @Override
    public Iterable<E> find(Class<E> table) throws SQLException, InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {
        return find(table, null);
    }

    @Override
    public Iterable<E> find(Class<E> table, String where) throws SQLException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {
        String tableName = getTableName(table);

        String selectQuery = String.format("SELECT * FROM %s %s",
                tableName, where != null ? "WHERE " + where : "");

        PreparedStatement statement = connection.prepareStatement(selectQuery);
        ResultSet resultSet = statement.executeQuery();

        List<E> result = new ArrayList<>();
        while (resultSet.next()) {
            E entity = table.getDeclaredConstructor().newInstance();
            fillEntity(table, resultSet, entity);

            result.add(entity);
        }

        return result;
    }

    @Override
    public E findFirst(Class<E> table) throws SQLException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        return findFirst(table, null);
    }

    @Override
    public E findFirst(Class<E> table, String where) throws SQLException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        String tableName = getTableName(table);

        String selectQuery = String.format("SELECT * FROM %s %s LIMIT1",
                tableName, where != null ? "WHERE " + where : "");

        PreparedStatement statement = connection.prepareStatement(selectQuery);
        ResultSet resultSet = statement.executeQuery();

        resultSet.next();

        E result = table.getDeclaredConstructor().newInstance();
        fillEntity(table, resultSet, result);

        return result;
    }

    private void fillEntity(Class<E> table, ResultSet resultSet, E entity) throws SQLException, IllegalAccessException {
        Field[] declaredFields = table.getDeclaredFields();
        for (Field field : declaredFields) {
            field.setAccessible(true);
            fillField(field, resultSet, entity);
        }
    }

    private void fillField(Field field, ResultSet resultSet, E entity) throws SQLException, IllegalAccessException {
        Class<?> fieldType = field.getType();
        String fieldName = field.getAnnotationsByType(Column.class)[0].name();

        if (fieldType == int.class || fieldType == Integer.class) {
            int value = resultSet.getInt(fieldName);

            field.set(entity, value);
        } else if (fieldType == long.class || fieldType == Long.class) {
            long value = resultSet.getLong(fieldName);

            field.set(entity, value);
        } else if (fieldType == LocalDate.class) {
            LocalDate value = LocalDate.parse(resultSet.getString(fieldName));

            field.set(entity, value);
        } else {
            String value = resultSet.getString(fieldName);

            field.set(entity, value);
        }
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

    private boolean doUpdate(E entity, long idValue) throws IllegalAccessException, SQLException {
        String tableName = getTableName(entity.getClass());
        List<String> tableFields = getColumnsWithoutId(entity.getClass());// username, age, reg_date,
        List<String> columnValues = getColumnValuesWithoutId(entity);


        List<String> setStatements = new ArrayList<>();
        for (int i = 0; i < tableFields.size(); i++) {
            String statement = tableFields.get(i) + " = " + columnValues.get(i);

            setStatements.add(statement);
        }
        String updateQuery = String.format("UPDATE %s SET %s WHERE id = %d",
                tableName,
                String.join(",", setStatements),
                idValue);

        return connection.prepareStatement(updateQuery).execute();
    }

    private boolean doInsert(E entity) throws IllegalAccessException, SQLException {
        String tableName = getTableName(entity.getClass());
        List<String> tableFields = getColumnsWithoutId(entity.getClass());// username, age, reg_date,
        List<String> columnValues = getColumnValuesWithoutId(entity);

        String insertQuery = String.format(
                "INSERT INTO %s (%s) VALUES (%s)",
                tableName,
                String.join(",", tableFields),
                String.join(",", columnValues)
        );

        return connection.prepareStatement(insertQuery).execute();
    }

    private String getAddColumnStatementsForNewFields(Class<E> entityClass) throws SQLException {
        Set<String> sqlColumns = getSQLColumnNames(entityClass);
        List<Field> fields = Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> !field.isAnnotationPresent(Id.class))
                .filter(field -> field.isAnnotationPresent(Column.class))
                .toList();

        List<String> allAddStatements = new ArrayList<>();
        for (Field field : fields) {
            String fieldName = field.getAnnotationsByType(Column.class)[0].name();
            String sqlType = getSQLType(field.getType());

            if (!sqlColumns.contains(fieldName)) {
                String statement = String.format("ADD COLUMN %s %s", fieldName, sqlType);
                allAddStatements.add(statement);
            }
        }

        return String.join(",", allAddStatements);
    }

    private Set<String> getSQLColumnNames(Class<E> entityClass) throws SQLException {
        String schemaQuery = "SELECT COLUMN_NAME FROM information_schema.`COLUMNS` c" +
                " WHERE c.TABLE_SCHEMA = 'custom-orm'" +
                " AND COLUMN_NAME != '%s'" +
                " AND TABLE_NAME = '%s';";

        PreparedStatement statement = connection.prepareStatement(schemaQuery);
        ResultSet resultSet = statement.executeQuery();

        Set<String> result = new HashSet<>();
        while (resultSet.next()) {
            String columnName = resultSet.getString("COLUMN_NAME");
            result.add(columnName);
        }

        return result;
    }

    private List<String> getColumnValuesWithoutId(E entity) throws IllegalAccessException {
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

        return values;
    }

    private List<String> getColumnsWithoutId(Class<?> aClass) {
        return Arrays.stream(aClass.getDeclaredFields())
                .filter(f -> !f.isAnnotationPresent(Id.class))
                .filter(f -> f.isAnnotationPresent(Column.class))
                .map(f -> f.getAnnotationsByType(Column.class))
                .map(a -> a[0].name())
                .toList();
    }
}
