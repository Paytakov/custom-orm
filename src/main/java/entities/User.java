package entities;

import java.time.LocalDate;

public class User {

    private long id;
    private String username;
    private int age;
    private LocalDate registrationDate;

    public User(String username,
                int age,
                LocalDate registrationDate) {
        this.username = username;
        this.age = age;
        this.registrationDate = registrationDate;
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
}
