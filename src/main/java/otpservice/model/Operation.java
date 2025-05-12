package otpservice.model;

import java.util.Objects;

public class Operation {

    private Long id;
    private int number;
    private String name;
    private String description;

    // Пустой конструктор
    public Operation() {
    }

    // Полный конструктор
    public Operation(Long id, int number, String name, String description) {
        this.id = id;
        this.number = number;
        this.name = name;
        this.description = description;
    }

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // hashCode
    @Override
    public int hashCode() {
        return Objects.hash(id, number, name, description);
    }

    // toString в JSON формате
    @Override
    public String toString() {

        return "Operation{" +
                "id=" + id +
                ", number='" + number + '\'' +
                ", name=" + name +
                ", description=" + description +
                '}';

//        return "{" +
//                "\"id\":" + id +
//                ", \"number\":" + number +
//                ", \"name\":\"" + (name != null ? name : "") + "\"" +
//                ", \"description\":\"" + (description != null ? description : "") + "\"" +
//                "}";
    }

    // equals
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Operation operation = (Operation) o;
        return number == operation.number &&
                Objects.equals(id, operation.id) &&
                Objects.equals(name, operation.name) &&
                Objects.equals(description, operation.description);
    }


}
