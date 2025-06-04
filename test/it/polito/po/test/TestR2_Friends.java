package it.polito.po.test;

import java.util.Collection;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import social.*;

import static org.junit.Assert.*;

public class TestR2_Friends {

  @BeforeClass
  public static void populateDb() throws PersonExistsException, NoSuchCodeException {
    JPAUtil.setTestMode();
    Social m = new Social();

    m.addPerson("ABCD", "Ricardo", "Kaka");
    m.addPerson("XYZ", "Alex", "Pato");
    m.addPerson("GGG", "Gennaro", "Gattuso");
    m.addFriendship("ABCD", "XYZ");
    m.addFriendship("ABCD", "GGG");

    m.addPerson("KABI", "Khaby", "Lame");

    m.addPerson("PPP", "Paolo", "Maldini");
    m.addPerson("AAA", "Andrea", "Pirlo");
    m.addFriendship("PPP", "GGG");
    m.addFriendship("AAA", "GGG");

    m.addFriendship("ABCD", "PPP");
  }

  @AfterClass
  public static void tearDownAfterAllTests() {
    JPAUtil.close();
  }

  @Test
  public void testR21Friendship() throws NoSuchCodeException {
    Social m = new Social();
    Collection<String> friends = m.listOfFriends("ABCD");

    assertNotNull("Missing list of friends", friends);
    assertTrue(friends.contains("XYZ"));
    assertTrue(friends.contains("GGG"));
  }

  @Test
  public void testR22TwoWayFriendship() throws NoSuchCodeException {
    Social m = new Social();
    Collection<String> friends = m.listOfFriends("ABCD");

    assertNotNull("Missing list of friends", friends);
    assertTrue(friends.contains("XYZ"));
    assertTrue(friends.contains("GGG"));
    Collection<String> friends2 = m.listOfFriends("GGG");
    assertTrue(friends2.contains("ABCD"));
    Collection<String> friends3 = m.listOfFriends("XYZ");
    assertTrue(friends3.contains("ABCD"));
  }

  @Test
  public void testR23FriendshipNotExistingCode() {
    Social m = new Social();
    assertThrows("Expecting an exception for friendship with non existing code",
        NoSuchCodeException.class,
        () -> m.addFriendship("ABCD", "UUUU"));
  }

  @Test
  public void testR24FriendshipNotExistingCode2() {
    Social m = new Social();
    assertThrows("Expecting an exception for friendship with non existing code",
        NoSuchCodeException.class,
        () -> m.listOfFriends("UUUU"));
  }

  @Test
  public void testR25FriendshipNull() throws NoSuchCodeException {
    Social m = new Social();
    Collection<String> friends = m.listOfFriends("KABI");

    assertNotNull("Missing list of friends", friends);
    assertEquals("Expecting no friends for newly created person",
        0, friends.size());
  }

}