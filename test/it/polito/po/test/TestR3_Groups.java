package it.polito.po.test;

import java.util.Collection;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import social.*;

import static org.junit.Assert.*;

public class TestR3_Groups {

  @BeforeClass
  public static void populateDb() throws PersonExistsException, NoSuchCodeException, GroupExistsException {
    JPAUtil.setTestMode();
    Social m = new Social();

    m.addPerson("ABCD", "Ricardo", "Kaka");
    m.addPerson("XYZ", "Alex", "Pato");
    m.addPerson("GGG", "Gennaro", "Gattuso");
    m.addFriendship("ABCD", "XYZ");
    m.addFriendship("ABCD", "GGG");

    m.addGroup("milan");
    m.addGroup("juve");
    m.addGroup("brasile");
    m.addGroup("poli");

    m.addPersonToGroup("XYZ", "brasile");
    m.addPersonToGroup("ABCD", "brasile");
    m.addPersonToGroup("ABCD", "milan");
    m.addPersonToGroup("GGG", "milan");
  }

  @AfterClass
  public static void tearDownAfterAllTests() {
    JPAUtil.close();
  }

  @Test
  public void testR31Group() {
    Social m = new Social();
    Collection<String> s = m.listOfGroups();

    assertNotNull("Missing list of groups", s);
    assertTrue(s.contains("milan"));
    assertTrue(s.contains("juve"));
  }

  @Test
  public void testR3_UpdateGroupSuccess() throws Exception {
    Social m = new Social();
    Social s = new Social();
    m.updateGroupName("milan", "renamedgroup");

    try{
      Collection<String> groups = s.listOfGroups();
      assertNotNull("Missing list of groups", groups);

      assertFalse("Old group name should not exist", groups.contains("milan"));
      assertTrue("New group name should be present", groups.contains("renamedgroup"));
    }finally{
      m.updateGroupName("renamedgroup", "milan");
    }
  }

  @Test
  public void testR3_UpdateGroup_GroupExistsException() {
    Social m = new Social();

    assertThrows("Updating to an already existing group should fail",
        GroupExistsException.class,
        () -> m.updateGroupName("juve", "milan"));
  }

  @Test
  public void testR3_UpdateGroup_NoSuchCodeException() {
    Social m = new Social();

    assertThrows("Updating a non-existent group should fail",
        NoSuchCodeException.class,
        () -> m.updateGroupName("doesnotexist", "newname"));
  }

  @Test
  public void testR3_DeleteGroupSuccess() throws Exception {
    Social m = new Social();
    Social s = new Social();
    Collection<String> groups;

    m.addGroup("tobedeleted");
    groups = s.listOfGroups();
    assertNotNull("Missing list of groups", groups);

    assertTrue("Group tobedeleted should be present", groups.contains("tobedeleted"));
    s.deleteGroup("tobedeleted");
    groups = m.listOfGroups();
    assertFalse("Group tobedeleted should not be present", groups.contains("tobedeleted"));
  }

  @Test
  public void testR3_DeleteGroup_NoSuchCodeException() {
    Social m = new Social();
    assertThrows("Deleting a non-existent group should fail",
        NoSuchCodeException.class,
        () -> m.deleteGroup("ghostgroup"));
  }

  @Test
  public void testR33GroupListing() {
    Social m = new Social();
    Collection<String> s = m.listOfPeopleInGroup("brasile");

    assertNotNull("Missing list of groups", s);
    assertTrue(s.contains("XYZ"));
    assertTrue(s.contains("ABCD"));

    s = m.listOfPeopleInGroup("milan");
    assertTrue(s.contains("ABCD"));
    assertTrue(s.contains("GGG"));
  }

  @Test
  public void testR3_MissingPerson() {
    Social m = new Social();
    assertThrows("When adding an unknown person to a group an exception is expected",
        NoSuchCodeException.class,
        () -> m.addPersonToGroup("NONEXISTENT", "brasil"));
  }

  @Test
  public void testR3_MissingGroup() {
    Social m = new Social();
    assertThrows("When adding to an unknown group an exception is expected",
        NoSuchCodeException.class,
        () -> m.addPersonToGroup("ABCD", "NO_GROUP"));
  }

  @Test
  public void testR3_EmptyGroup() {
    Social m = new Social();
    Collection<String> s = m.listOfPeopleInGroup("juve");

    assertNotNull("Missing collection of members for empty group", s);
    assertEquals("Empty group should have no members", 0, s.size());
  }

}