package it.polito.po.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import social.JPAUtil;
import social.Social;

import java.util.List;

public class TestR5_Posts {

  @BeforeClass
  public static void setUp() throws Exception {
    JPAUtil.setTestMode();
    Social f = new Social();

    f.addPerson("Mario99", "Mario", "Rossi");
    f.addPerson("Mario88", "Mario", "Verdi");
    f.addPerson("Elena66", "Elena", "Aresti");
    f.addPerson("BigLupo", "Lupo", "Bianchi");
    f.addPerson("FFA", "Franca", "Rosetti");
    f.addPerson("Sally", "Sandra", "Sandroni");

    f.addFriendship("Mario99", "BigLupo");
    f.addFriendship("Mario99", "Elena66");
    f.addFriendship("Elena66", "FFA");
    f.addFriendship("Elena66", "Sally");
    f.addFriendship("BigLupo", "FFA");
  }

  @AfterClass
  public static void tearDownAfterAllTests() {
    JPAUtil.close();
  }

  @Test
  public void testPost() {
    Social f = new Social();
    Social r = new Social();
    String pid = f.post("BigLupo", "Hi everybody!");

    assertNotNull("Missing post id", pid);
    assertTrue("Empty post id", !pid.isEmpty());

    String postContent = r.getPostContent(pid);
    assertNotNull("Missing post content", postContent);
    assertEquals("Incorrect post content", "Hi everybody!", postContent);
  }

  @Test
  public void testPostIdUnique() {
    Social f = new Social();
    String pid = f.post("BigLupo", "Hi everybody!");
    String pid2 = f.post("BigLupo", "Hi everybody!");

    assertNotNull("Missing post id", pid);
    assertNotEquals("Post Ids must be unique", pid, pid2);
  }

  @Test
  public void testPostData() {
    Social f = new Social();
    Social r = new Social();
    String author = "BigLupo";
    long t0 = System.currentTimeMillis();
    String text = "Hi everybody!";
    String pid = f.post(author, text);

    assertNotNull("Missing post id", pid);

    String c = r.getPostContent(pid);
    long t = r.getTimestamp(pid);

    long t1 = System.currentTimeMillis();

    assertNotNull("Missing post content", c);
    assertEquals("Wrong content", text, c);

    assertTrue("Missing post timestamp", t >= 0);
    assertTrue("Wrong author", t >= t0 && t <= t1);

  }

  @Test
  public void testPaginatedUserPosts() throws InterruptedException {
    Social f = new Social();
    Social r = new Social();
    String author = "FFA";
    String text = "Hi everybody!";
    f.post(author, text);
    Thread.sleep(5);
    String pid2 = f.post(author, 2 + text);
    Thread.sleep(5);
    f.post(author, 3 + text);
    Thread.sleep(5);
    f.post(author, 4 + text);
    Thread.sleep(5);
    String pid1 = f.post(author, 5 + text);

    List<String> posts = r.getPaginatedUserPosts(author, 1, 3);

    assertNotNull("Missing paginated post for user " + author, posts);
    assertEquals("Wrong number of posts in page", 3, posts.size());
    assertEquals("First post should be most recent", pid1, posts.get(0));

    posts = f.getPaginatedUserPosts(author, 2, 3);
    assertNotNull("Missing paginated post for user " + author, posts);
    assertEquals("Wrong number of posts in second page", 2, posts.size());
    assertEquals("First post on page should be most recent", pid2, posts.get(0));

  }

  @Test
  public void testPaginatedFriendPosts() throws InterruptedException {
    Social f = new Social();
    Social r = new Social();
    String author = "Mario88";
    String text = "Hi everybody!";

    f.post(author, text); // not a friend
    Thread.sleep(5);
    String pid2 = f.post("Sally", text);
    Thread.sleep(5);
    f.post("Mario99", text);
    Thread.sleep(5);
    String pid1 = f.post("Sally", 2 + text);
    Thread.sleep(5);
    f.post("Elena66", text); // self post, not included

    List<String> posts = r.getPaginatedFriendPosts("Elena66", 1, 2);

    assertNotNull("Missing paginated post for user " + author, posts);
    assertEquals("Wrong number of posts in page", 2, posts.size());
    String[] post = posts.get(0).split("\\s*:\\s*");
    assertEquals("Wrong post format", 2, post.length);
    assertEquals("First post should be most recent", pid1, post[1]);
    assertEquals("First post should be most recent", "Sally", post[0]);

    posts = f.getPaginatedFriendPosts("Elena66", 2, 2);
    assertNotNull("Missing paginated post for user " + author, posts);
    assertEquals("Wrong number of posts in second page", 1, posts.size());
    post = posts.get(0).split("\\s*:\\s*");
    assertEquals("First post on page should be most recent", pid2, post[1]);

  }

}
