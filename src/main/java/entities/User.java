package entities;

import annotation.Column;
import annotation.Entity;
import annotation.Id;

import java.time.LocalDate;

@Entity(name = "users")
public class User {

    @Id
    @Column(name = "id")
    private long id;
    @Column(name = "username")
    private String username;
    @Column(name = "age")
    private int age;
    @Column(name = "registration_date")
    private LocalDate registrationDate;
    @Column(name = "last_logged_in")
    private LocalDate lastLoggedIn;

    public User(String username,
                int age,
                LocalDate registrationDate) {
        this.username = username;
        this.age = age;
        this.registrationDate = registrationDate;
        this.lastLoggedIn = LocalDate.now();
    }

    public long getId() {
        return id;
    }

    public User setId(long id) {
        this.id = id;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public User setUsername(String username) {
        this.username = username;
        return this;
    }

    public int getAge() {
        return age;
    }

    public User setAge(int age) {
        this.age = age;
        return this;
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    public User setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
        return this;
    }

    public LocalDate getLastLoggedIn() {
        return lastLoggedIn;
    }

    public User setLastLoggedIn(LocalDate lastLoggedIn) {
        this.lastLoggedIn = lastLoggedIn;
        return this;
    }
}
