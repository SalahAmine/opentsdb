// This file is part of OpenTSDB.
// Copyright (C) 2015-2017  The OpenTSDB Authors.
//
// This program is free software: you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 2.1 of the License, or (at your
// option) any later version.  This program is distributed in the hope that it
// will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
// of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
// General Public License for more details.  You should have received a copy
// of the GNU Lesser General Public License along with this program.  If not,
// see <http://www.gnu.org/licenses/>.
package net.opentsdb.query.pojo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.util.List;

import net.opentsdb.query.pojo.Join.SetOperator;
import net.opentsdb.utils.JSON;

import org.junit.Test;

import com.google.common.collect.Lists;

public class TestJoin {

  @Test
  public void deserialize() throws Exception {
    final String json = "{\"operator\":\"union\",\"tags\":[\"host\",\"datacenter\"]}";
    final Join join = Join.newBuilder().setOperator(SetOperator.UNION)
        .setTags(Lists.newArrayList("host", "datacenter")).build();
    final Join deserialized = JSON.parseToObject(json, Join.class);
    assertEquals(join, deserialized);
  }
  
  @Test
  public void serialize() throws Exception {
    final Join join = Join.newBuilder().setOperator(SetOperator.UNION)
        .setTags(Lists.newArrayList("host", "datacenter")).build();
    final String json = JSON.serializeToString(join);
    assertTrue(json.contains("\"operator\":\"union\""));
    assertTrue(json.contains("\"tags\":["));
    assertTrue(json.contains("\"host\""));
    assertTrue(json.contains("\"datacenter\""));
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void validationErrorWhenOperatorIsNull() throws Exception {
    final String json = "{\"operator\":null}";
    final Join join = JSON.parseToObject(json, Join.class);
    join.validate();
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void validationErrorWhenOperatorIsEmpty() throws Exception {
    final String json = "{\"operator\":\"\"}";
    final Join join = JSON.parseToObject(json, Join.class);
    join.validate();
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void validationErrorWhenOperatorIsInvalid() throws Exception {
    final String json = "{\"operator\":\"nosuchop\"}";
    final Join join = JSON.parseToObject(json, Join.class);
    join.validate();
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void validationNullTag() throws Exception {
    final String json = "{\"operator\":\"intersection\",\"tags\":[null]}";
    final Join join = JSON.parseToObject(json, Join.class);
    join.validate();
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void validationEmptyTag() throws Exception {
    final String json = "{\"operator\":\"intersection\",\"tags\":[\"\"]}";
    final Join join = JSON.parseToObject(json, Join.class);
    join.validate();
  }
  
  @Test
  public void unknownShouldBeIgnored() throws Exception {
    String json = "{\"operator\":\"intersection\",\"unknown\":\"yo\"}";
    JSON.parseToObject(json, Filter.class);
    // pass if no unexpected exception
  }
  
  @Test
  public void build() throws Exception {
    final Join join = Join.newBuilder()
        .setOperator(SetOperator.CROSS)
        .setIncludeAggTags(true)
        .setIncludeDisjointTags(true)
        .addTag("b")
        .addTag("a")
        .build();
    final Join clone = Join.newBuilder(join).build();
    assertNotSame(clone, join);
    assertEquals(SetOperator.CROSS, clone.getOperator());
    assertTrue(clone.getIncludeAggTags());
    assertTrue(clone.getIncludeDisjointTags());
    assertFalse(clone.getUseQueryTags());
    assertEquals("a", clone.getTags().get(0));
    assertEquals("b", clone.getTags().get(1));
  }
  
  @Test
  public void hashCodeEqualsCompareTo() throws Exception {
    final Join j1 = new Join.Builder()
        .setOperator(SetOperator.INTERSECTION)
        .setIncludeDisjointTags(false)
        .setIncludeAggTags(false)
        .setUseQueryTags(false)
        .setTags(Lists.newArrayList("host", "datacenter"))
        .build();
    
    Join j2 = new Join.Builder()
        .setOperator(SetOperator.INTERSECTION)
        .setIncludeDisjointTags(false)
        .setIncludeAggTags(false)
        .setUseQueryTags(false)
        .setTags(Lists.newArrayList("host", "datacenter"))
        .build();
    assertEquals(j1.hashCode(), j2.hashCode());
    assertEquals(j1, j2);
    assertEquals(0, j1.compareTo(j2));
    
    j2 = new Join.Builder()
        .setOperator(SetOperator.UNION) // <-- diff
        .setIncludeDisjointTags(false)
        .setIncludeAggTags(false)
        .setUseQueryTags(false)
        .setTags(Lists.newArrayList("host", "datacenter"))
        .build();
    assertNotEquals(j1.hashCode(), j2.hashCode());
    assertNotEquals(j1, j2);
    assertEquals(-1, j1.compareTo(j2));
    
    j2 = new Join.Builder()
        .setOperator(SetOperator.INTERSECTION)
        .setIncludeDisjointTags(true) // <-- diff
        .setIncludeAggTags(false)
        .setUseQueryTags(false)
        .setTags(Lists.newArrayList("host", "datacenter"))
        .build();
    assertNotEquals(j1.hashCode(), j2.hashCode());
    assertNotEquals(j1, j2);
    assertEquals(1, j1.compareTo(j2));
    
    j2 = new Join.Builder()
        .setOperator(SetOperator.INTERSECTION) 
        .setIncludeDisjointTags(false)
        .setIncludeAggTags(true) // <-- diff
        .setUseQueryTags(false)
        .setTags(Lists.newArrayList("host", "datacenter"))
        .build();
    assertNotEquals(j1.hashCode(), j2.hashCode());
    assertNotEquals(j1, j2);
    assertEquals(1, j1.compareTo(j2));
    
    j2 = new Join.Builder()
        .setOperator(SetOperator.INTERSECTION) 
        .setIncludeDisjointTags(false)
        .setIncludeAggTags(false)
        .setUseQueryTags(true)  // <-- diff
        .setTags(Lists.newArrayList("host", "datacenter"))
        .build();
    assertNotEquals(j1.hashCode(), j2.hashCode());
    assertNotEquals(j1, j2);
    assertEquals(1, j1.compareTo(j2));
    
    j2 = new Join.Builder()
        .setOperator(SetOperator.INTERSECTION) 
        .setIncludeDisjointTags(false)
        .setIncludeAggTags(false)
        .setUseQueryTags(false)
        .setTags(Lists.newArrayList("datacenter", "host"))  // <-- diff order
        .build();
    assertEquals(j1.hashCode(), j2.hashCode());
    assertEquals(j1, j2);
    assertEquals(0, j1.compareTo(j2));
    
    j2 = new Join.Builder()
        .setOperator(SetOperator.INTERSECTION) 
        .setIncludeDisjointTags(false)
        .setIncludeAggTags(false)
        .setUseQueryTags(false)
        .setTags(Lists.newArrayList("datacenter", "diff"))  // <-- diff
        .build();
    assertNotEquals(j1.hashCode(), j2.hashCode());
    assertNotEquals(j1, j2);
    assertEquals(1, j1.compareTo(j2));
    
    List<String> empty = Lists.newArrayList();
    j2 = new Join.Builder()
        .setOperator(SetOperator.INTERSECTION) 
        .setIncludeDisjointTags(false)
        .setIncludeAggTags(false)
        .setUseQueryTags(false)
        .setTags(empty)  // <-- diff
        .build();
    assertNotEquals(j1.hashCode(), j2.hashCode());
    assertNotEquals(j1, j2);
    assertEquals(1, j1.compareTo(j2));
    
    j2 = new Join.Builder()
        .setOperator(SetOperator.INTERSECTION) 
        .setIncludeDisjointTags(false)
        .setIncludeAggTags(false)
        .setUseQueryTags(false)
        //.setTags(Lists.newArrayList("host", "datacenter"))  // <-- diff
        .build();
    assertNotEquals(j1.hashCode(), j2.hashCode());
    assertNotEquals(j1, j2);
    assertEquals(1, j1.compareTo(j2));
  }
}
