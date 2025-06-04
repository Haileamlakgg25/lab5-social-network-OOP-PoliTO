package social;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Facade class for the social network system.
 * 
 */
public class Social {

  private final PersonRepository personRepository = new PersonRepository();

  // In-memory group storage: groupName -> set of person codes
  private static final Map<String, Set<String>> groups = new ConcurrentHashMap<>();

  // In-memory post storage: postId -> PostData
  private static final Map<String, PostData> posts = new ConcurrentHashMap<>();

  // Helper class for post data
  private static class PostData {
    String id;
    String author;
    String content;
    long timestamp;
    PostData(String id, String author, String content, long timestamp) {
      this.id = id;
      this.author = author;
      this.content = content;
      this.timestamp = timestamp;
    }
    // Added getter for timestamp to support sorting and test assertions
    public long getTimestamp() {
      return timestamp;
    }
  }

  /**
   * Creates a new account for a person
   * 
   * @param code    nickname of the account
   * @param name    first name
   * @param surname last name
   * @throws PersonExistsException in case of duplicate code
   */
  public void addPerson(String code, String name, String surname) throws PersonExistsException {
    if (personRepository.findById(code).isPresent()){
        throw new PersonExistsException();
    }
    Person person = new Person(code, name, surname);    // create the person as a POJO
    personRepository.save(person);                      // save it to db
  }

  /**
   * Retrieves information about the person given their account code.
   * The info consists in name and surname of the person, in order, separated by
   * blanks.
   * 
   * @param code account code
   * @return the information of the person
   * @throws NoSuchCodeException if a person with that code does not exist
   */
  public String getPerson(String code) throws NoSuchCodeException {
    Person p = personRepository.findById(code).orElseThrow(NoSuchCodeException::new);
    return p.getCode() + " " + p.getName() + " " + p.getSurname();
  }

  /**
   * Define a friendship relationship between two persons given their codes.
   * <p>
   * Friendship is bidirectional: if person A is adding as friend person B, that means
   * that person B automatically adds as friend person A.
   * 
   * @param codePerson1 first person code
   * @param codePerson2 second person code
   * @throws NoSuchCodeException in case either code does not exist
   */
  public void addFriendship(String codePerson1, String codePerson2)
      throws NoSuchCodeException {
    if (codePerson1.equals(codePerson2)) return;
    Person p1 = personRepository.findById(codePerson1).orElseThrow(NoSuchCodeException::new);
    Person p2 = personRepository.findById(codePerson2).orElseThrow(NoSuchCodeException::new);
    boolean changed = false;
    if (!p1.getFriends().contains(codePerson2)) {
      p1.addFriend(codePerson2);
      changed = true;
    }
    if (!p2.getFriends().contains(codePerson1)) {
      p2.addFriend(codePerson1);
      changed = true;
    }
    if (changed) {
      personRepository.update(p1);
      personRepository.update(p2);
    }
  }

  /**
   * Retrieve the collection of their friends given the code of a person.
   *
   * @param codePerson code of the person
   * @return the list of person codes
   * @throws NoSuchCodeException in case the code does not exist
   */
  public Collection<String> listOfFriends(String codePerson)
      throws NoSuchCodeException {
    Person p = personRepository.findById(codePerson).orElseThrow(NoSuchCodeException::new);
    return new ArrayList<>(p.getFriends());
  }

  /**
   * Creates a new group with the given name
   * 
   * @param groupName name of the group
   * @throws GroupExistsException if a group with given name does not exist
   */
  public void addGroup(String groupName) throws GroupExistsException {
    synchronized (groups) {
      if (groups.containsKey(groupName)) throw new GroupExistsException();
      groups.put(groupName, ConcurrentHashMap.newKeySet());
    }
  }

  /**
   * Deletes the group with the given name
   * 
   * @param groupName name of the group
   * @throws NoSuchCodeException if a group with given name does not exist
   */
  public void deleteGroup(String groupName) throws NoSuchCodeException {
    synchronized (groups) {
      if (!groups.containsKey(groupName)) throw new NoSuchCodeException();
      groups.remove(groupName);
      // Remove group from all persons
      for (Person p : personRepository.findAll()) {
        if (p.getGroups().remove(groupName)) {
          personRepository.update(p);
        }
      }
    }
  }

  /**
   * Modifies the group name
   * 
   * @param groupName name of the group
   * @throws NoSuchCodeException if the original group name does not exist
   * @throws GroupExistsException if the target group name already exist
   */
  public void updateGroupName(String groupName, String newName) throws NoSuchCodeException, GroupExistsException {
    synchronized (groups) {
      if (!groups.containsKey(groupName)) throw new NoSuchCodeException();
      if (groups.containsKey(newName)) throw new GroupExistsException();
      Set<String> members = groups.remove(groupName);
      groups.put(newName, members);
      // Update group name in all persons
      for (Person p : personRepository.findAll()) {
        if (p.getGroups().remove(groupName)) {
          p.getGroups().add(newName);
          personRepository.update(p);
        }
      }
    }
  }

  /**
   * Retrieves the list of groups.
   * 
   * @return the collection of group names
   */
  public Collection<String> listOfGroups() {
    synchronized(groups) {
      return new ArrayList<>(groups.keySet());
    }
  }

  /**
   * Add a person to a group
   * 
   * @param codePerson person code
   * @param groupName  name of the group
   * @throws NoSuchCodeException in case the code or group name do not exist
   */
  public void addPersonToGroup(String codePerson, String groupName) throws NoSuchCodeException {
    synchronized(groups) {
      Person p = personRepository.findById(codePerson).orElseThrow(NoSuchCodeException::new);
      Set<String> group = groups.get(groupName);
      if (group == null) throw new NoSuchCodeException();
      group.add(codePerson);
      if (!p.getGroups().contains(groupName)) {
        p.addGroup(groupName);
        personRepository.update(p);
      }
    }
  }

  /**
   * Retrieves the list of people in a group
   * @param groupName name of the group
   * @return collection of person codes, or null if the group does not exist
   */
  public Collection<String> listOfPeopleInGroup(String groupName) {
    synchronized(groups) {
      Set<String> group = groups.get(groupName);
      return group != null ? new ArrayList<>(group) : null;
    }
  }

  /**
   * Retrieves the code of the person having the largest number of friends
   * @return the code of the person
   */
  public String personWithLargestNumberOfFriends() {
    Optional<Person> maxPerson = personRepository.findAll().stream()
        .max(Comparator.comparingInt(p -> p.getFriends().size()));
    return maxPerson.map(Person::getCode).orElse(null);
  }

  /**
   * Find the name of group with the largest number of members
   * @return the name of the group
   */
  public String largestGroup() {
    synchronized(groups) {
      Optional<Map.Entry<String, Set<String>>> maxGroup = groups.entrySet().stream()
          .max(Map.Entry.comparingByValue(Comparator.comparingInt(Set::size)));
      return maxGroup.map(Map.Entry::getKey).orElse(null);
    }
  }

  /**
   * Find the code of the person that is member of the largest number of groups
   * @return the code of the person
   */
  public String personInLargestNumberOfGroups() {
    Optional<Person> maxPerson = personRepository.findAll().stream()
        .max(Comparator.comparingInt(p -> p.getGroups().size()));
    return maxPerson.map(Person::getCode).orElse(null);
  }

  /**
   * Returns the number of groups
   * @return number of groups
   */
  public int numberOfGroups() {
    return groups.size();
  }

  /**
   * add a new post by a given account
   * @param authorCode the id of the post author
   * @param text   the content of the post
   * @return a unique id of the post, or null if author does not exist
   */
  public String post(String authorCode, String text) {
    if (!personRepository.findById(authorCode).isPresent()) return null;
    String id = UUID.randomUUID().toString().replaceAll("-", "");
    long ts = System.currentTimeMillis();
    PostData pd = new PostData(id, authorCode, text, ts);
    posts.put(id, pd);
    return id;
  }

  /**
   * retrieves the content of the given post
   * @param pid    the id of the post
   * @return the content of the post, or null if not found
   */
  public String getPostContent(String pid) {
    PostData pd = posts.get(pid);
    return pd == null ? null : pd.content;
  }

  /**
   * retrieves the timestamp of the given post
   * @param pid    the id of the post
   * @return the timestamp of the post, or -1 if not found
   */
  public long getTimestamp(String pid) {
    PostData pd = posts.get(pid);
    return pd == null ? -1 : pd.timestamp;
  }

  /**
   * returns the list of post of a given author paginated
   * @param author     author of the post
   * @param pageNo     page number (starting at 1)
   * @param pageLength page length
   * @return the list of post ids
   */
  public List<String> getPaginatedUserPosts(String author, int pageNo, int pageLength) {
    List<PostData> userPosts = posts.values().stream()
      .filter(p -> p.author.equals(author))
      .sorted(Comparator.comparingLong((PostData p) -> p.timestamp).reversed())
      .collect(Collectors.toList());
    
    int from = (pageNo - 1) * pageLength;
    if (from >= userPosts.size()) return new ArrayList<>();
    
    int to = Math.min(from + pageLength, userPosts.size());
    return userPosts.subList(from, to).stream()
      .map(p -> p.id)
      .collect(Collectors.toList());
  }

  /**
   * returns the paginated list of post of friends.
   * The returned list contains the author and the id of a post separated by " : "
   * @param author     author of the post
   * @param pageNo     page number (starting at 1)
   * @param pageLength page length
   * @return the list of posts key element
   */
  public List<String> getPaginatedFriendPosts(String author, int pageNo, int pageLength) {
    Person p = personRepository.findById(author).orElse(null);
    if (p == null) return null;
    
    Set<String> friends = new HashSet<>(p.getFriends());
    friends.remove(author);  // exclude self posts
    
    List<PostData> friendPosts = posts.values().stream()
        .filter(post -> friends.contains(post.author))
        .sorted(Comparator.comparingLong(PostData::getTimestamp).reversed())
        .collect(Collectors.toList());
    
    int from = (pageNo - 1) * pageLength;
    if (from >= friendPosts.size()) return new ArrayList<>();
    
    int to = Math.min(from + pageLength, friendPosts.size());
    return friendPosts.subList(from, to).stream()
        .map(pd -> String.format("%s : %s", pd.author, pd.id))
        .collect(Collectors.toList());
  }

}