# Quick Fix Guide for CheckStyle Violations

## Current Violations (3 total)

### 1. Fix Star Imports in ExampleInstrumentedTest.java

**File:** `app/src/androidTest/java/com/example/sprintproject/ExampleInstrumentedTest.java`

**Line 11 - Change:**
```java
import static org.junit.Assert.*;
```

**To:**
```java
import static org.junit.Assert.assertEquals;
```

---

### 2. Fix Star Imports in ExampleUnitTest.java

**File:** `app/src/test/java/com/example/sprintproject/ExampleUnitTest.java`

**Line 5 - Change:**
```java
import static org.junit.Assert.*;
```

**To:**
```java
import static org.junit.Assert.assertEquals;
```

---

### 3. Fix Method Naming in ExampleUnitTest.java

**File:** `app/src/test/java/com/example/sprintproject/ExampleUnitTest.java`

**Line 14 - Change:**
```java
@Test
public void addition_isCorrect() {
    assertEquals(4, 2 + 2);
}
```

**To:**
```java
@Test
public void additionIsCorrect() {
    assertEquals(4, 2 + 2);
}
```

---

## Verify Fixes

After making the changes, run:

```bash
cd Project/Sprint0.5-main
./gradlew checkstyle
```

You should see:
```
BUILD SUCCESSFUL
```

View the detailed report:
```
app/build/reports/checkstyle/checkstyle.html
```

---

## CheckStyle Rules Applied

- **AvoidStarImport:** Prohibits wildcard imports (e.g., `import java.util.*`)
- **MethodName:** Methods must follow camelCase pattern `^[a-z][a-zA-Z0-9]*$`

Use specific imports instead of wildcards for better code clarity and to avoid naming conflicts.
