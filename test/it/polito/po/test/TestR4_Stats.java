package it.polito.po.test;

import org.junit.BeforeClass;
import org.junit.Test;

import social.*;

import static org.junit.Assert.*;

import org.junit.AfterClass;

public class TestR4_Stats {

  @BeforeClass
  public static void populateDb() throws PersonExistsException, NoSuchCodeException, GroupExistsException {
    JPAUtil.setTestMode();
    Social m = new Social();

    m.addPerson("ABCD", "Ricardo", "Kaka");
    m.addPerson("XYZ", "Alex", "Pato");
    m.addPerson("GGG", "Gennaro", "Gattuso");
    m.addPerson("PPP", "Paolo", "Maldini");
    m.addPerson("AAA", "Andrea", "Pirlo");
    m.addFriendship("ABCD", "XYZ");
    m.addFriendship("ABCD", "GGG");
    m.addFriendship("PPP", "GGG");
    m.addFriendship("AAA", "GGG");

    m.addGroup("milan");
    m.addGroup("brasile");
    m.addGroup("poli");

    m.addPersonToGroup("XYZ", "brasile");
    m.addPersonToGroup("ABCD", "brasile");
    m.addPersonToGroup("GGG", "milan");
    m.addPersonToGroup("XYZ", "milan");
    m.addPersonToGroup("PPP", "milan");

    m.addPerson("SSS", "Andrey", "Sheva");
    m.addFriendship("SSS", "XYZ");
  }

  @AfterClass
  public static void tearDownAfterAllTests() {
    JPAUtil.close();
  }

  @Test
  public void testR41PersonWithLargestNumberOfFriends() {
    Social m = new Social();
    String s = m.personWithLargestNumberOfFriends();
    assertEquals("GGG", s);
  }

  @Test
  public void testR42PopularGroup() {
    Social m = new Social();
    String s = m.largestGroup();
    assertEquals("milan", s);
  }

  @Test
  public void testR43PersonInLargestNumberOfGroups() {
    Social m = new Social();
    String s = m.personInLargestNumberOfGroups();
    assertEquals("XYZ", s); // pato 2
  }

}