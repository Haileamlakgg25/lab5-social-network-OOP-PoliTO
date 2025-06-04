package it.polito.po.test;

import org.junit.BeforeClass;
import org.junit.Test;

import social.*;

import static org.junit.Assert.*;

import org.junit.AfterClass;

public class TestR1_Subscription {

  @BeforeClass
  public static void populateDb() throws PersonExistsException {
    JPAUtil.setTestMode();
    Social m = new Social();

    m.addPerson("ABCD", "Ricardo", "Kaka");
  }

  @AfterClass
  public static void tearDownAfterAllTests() {
    JPAUtil.close();
  }

  @Test
  public void testR11AddPerson() throws NoSuchCodeException {
    Social m = new Social();
    String s = m.getPerson("ABCD");
    assertEquals("Wrong person information", "ABCD Ricardo Kaka", s);
  }

  @Test
  public void testR13PersonDoesNotExist() {
    Social m = new Social();
    assertThrows("Non existing person should throw exception", NoSuchCodeException.class,
        () -> m.getPerson("ZZ"));
  }

  @Test
  public void testR14PersonExists() {
    Social m = new Social();
    assertThrows("Duplicate code should throw exception", PersonExistsException.class,
        () -> m.addPerson("ABCD", "Alex", "Pato"));
  }
}
