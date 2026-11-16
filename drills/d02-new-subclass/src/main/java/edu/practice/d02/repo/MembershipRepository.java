package edu.practice.d02.repo;

import edu.practice.d02.model.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory storage, the same shape as the LBP starters' repositories.
 */
public class MembershipRepository {

  private final Map<Long, Membership> memberships = new ConcurrentHashMap<>();

  public MembershipRepository() {
    memberships.put(1L, new PayAsYouGo(1, "Asha", 450));
    // TODO d02-4 (after you create Annual): add new Annual(2, "Ben", 39_000) here.
    memberships.put(2L, new Annual(2, "Ben", 39_000));
  }

  public Collection<Membership> all() {
    return List.copyOf(memberships.values());
  }

  public Membership get(long id) {
    return memberships.get(id);
  }
}
