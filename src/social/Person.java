package social;

import jakarta.persistence.*;
import java.util.*;

@Entity
public class Person {
  @Id
  private String code;
  private String name;
  private String surname;

  @ElementCollection(fetch = FetchType.EAGER)
  private Set<String> friends = new HashSet<>();

  @ElementCollection(fetch = FetchType.EAGER)
  private Set<String> groups = new HashSet<>();

  public Person() {
    // default constructor is needed by JPA
  }

  Person(String code, String name, String surname) {
    this.code = code;
    this.name = name;
    this.surname = surname;
  }

  String getCode() {
    return code;
  }

  String getName() {
    return name;
  }

  String getSurname() {
    return surname;
  }

  Set<String> getFriends() {
    return friends;
  }

  Set<String> getGroups() {
    return groups;
  }

  void addFriend(String code) {
    friends.add(code);
  }

  void removeFriend(String code) {
    friends.remove(code);
  }

  void addGroup(String groupName) {
    groups.add(groupName);
  }

  void removeGroup(String groupName) {
    groups.remove(groupName);
  }
}
