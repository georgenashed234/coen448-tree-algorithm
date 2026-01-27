
## Phase 1: Vibe Coding the Java Program

### The Development Prompt
Instead of worrying about synchronized blocks or Volatile variables right away, we describe the energy of the system.

### Prompt:

"I want to create a Java TaskProcessor in maven project to support multitasking It should have a worker pool that takes tasks (represented as name as Strings and task as Runnables) and processes them concurrently. It needs to be thread-safe but it shouldn't block the main thread when adding tasks. Use modern Java libraries (like ExecutorService) and keep the API super clean. Add logging by some 'check' print statements so I can see the threads working in harmony."

## Phase 2: The COSTAR Prompt for Testing

Now we apply the COSTAR technique to ensure the task processor is actually robust enough for production.

### Mapping COSTAR for Threading

- Context: Java 17+, Maven environment, high-concurrency threading.
- Outcome: Verify thread safety, atomic counting, and shutdown behavior.
- Steps: 1. Stress test with 1000 tasks. 2. Assert atomic count accuracy. 3. Test shutdown timeout.
- Tools: JUnit 5, CountDownLatch for thread synchronization in tests.
- Audience: Backend Engineers ensuring no race conditions.
- Relevance: Critical. If threads leak or counters drift, the system loses integrity.


### The COSTAR Test Generation Prompt

Context: I have a Java-based TaskProcessor (code provided) that uses an ExecutorService and AtomicInteger.

Outcome: Generate JUnit 5 tests. I need to prove that even with 100 concurrent submissions, the tasksProcessed count is exactly correct (no race conditions).

Steps: > 1. Use a CountDownLatch to wait for all asynchronous tasks to finish before asserting. 2. Test the edge case of submitting tasks after the processor has been shut down. 3. Verify that multiple threads can submit tasks simultaneously without crashing.

Tools: JUnit 5 @Test and @BeforeEach annotations.

Audience: Senior Developers looking for thread-safe guarantees.

Relevance: This prevents data loss and memory leaks in our worker service.


## Run tests with maven and see report

### Running Tests with Maven

Basic test execution

    cd task_processor_java
    mvn clean test

Detailed test output

    mvn clean test -X

Run specific test class

    mvn test -Dtest=TaskProcessorTest

Run specific test method

    mvn test -Dtest=TaskProcessorTest#testConcurrentTaskSubmissions


## Run tests with coverage (Maven + JaCoCO)

    mvn clean test jacoco:report

View coverage report (HTML)

    open target/site/jacoco/index.html  # macOS
    xdg-open target/site/jacoco/index.html  # Linux
    start target/site/jacoco/index.html  # Windows

### View Test Reports with maven surefire
Default Maven Surefire Report (HTML), After running tests, Maven generates a report at:

    target/surefire-reports/

Quick text summary in console

    mvn surefire-report:report

Then open:

    target/site/surefire-report.html
