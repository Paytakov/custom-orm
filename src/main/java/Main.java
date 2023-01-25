import entities.User;
import orm.EntityManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;

import static orm.MyConnector.createConnection;
import static orm.MyConnector.getConnection;

public class Main {

    public static void main(String[] args) throws SQLException, IllegalAccessException {
        createConnection("root", "", "mini-orm");
        Connection connection = getConnection();

        EntityManager<User> entityManager = new EntityManager<>(connection);

        User user = new User("Pesho", 24, LocalDate.now());
        user.setId(2);
        user.setUsername("Pecata");

//        entityManager.doCreate(User.class);
     //   entityManager.doAlter(User.class);
        entityManager.persist(user);
    }
}
