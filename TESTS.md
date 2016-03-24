How To Run Tests
================

All of the tests are ran during regular Maven build.

    mvn clean install

Refer to the following instructions how to run and debug particular type of test.


Arquillian based tests
======================

Overview
--------

Tests that need to be run in real application server are ran with Arquillian, they can be clearly distinguished by their class-level annotation:

    @RunWith(Arquillian.class)
    public class TestCase { ... }

The configuration of the tests is driven by `arquillian.xml` file.

Arquillian tests are runnable from build system (Maven) and IDE (Eclipse).



Micro-deployments for tests
---------------------------

The deployment that will be deployed to application server is defined alongside the test source:

    @Deployment
    public static WebArchive archive() { ... }

We use so called micro-deployment strategy that allows you to define only subset of all the required resources to test certain part of UnifiedPush Server. That speeds up test development and execution and makes it clear what is being tested.

Test development is simplified with custom ShrinkWrap archive called `UnifiedPushArchive` and its implementation `UnifiedPushArchiveImpl`. This archive allows you to quickly build WAR micro-deployment that is customized for given test, while staying DRY:

    @Deployment
    public static WebArchive archive() {
        return UnifiedPushArchive.forTestClass(TestMetricsCollector.class)
                .withMessaging()
                    .addClasses(MetricsCollector.class)
                    .addClasses(PushMessageMetricsService.class)
                    .addClasses(MetricCollectionTrigger.class)
                .withMockito()
                    .addClasses(MockProviders.class)
                .as(WebArchive.class);
    }

NOTE: the deployment created with `UnifiedPushArchive#forTestClass` will be by default called by the name of test class, e.g. `TestMetricsCollector.war` in this case.

Test run modes
--------------

There are two ways to run tests:

* *managed*
  * suitable one-off test execution (without prior development environment setup)
  * suitable for continuous integration
  * complete container lifecycle management
    * downloads and starts container
* *remote*
  * best for development, debugging and fast turnaround generally
  * tests reuse container that is already running on given port

Selecting application server and run mode
-----------------------------------------

Tests leverage [Arquillian Chameleon](https://github.com/arquillian/arquillian-container-chameleon) extension that allows to transparently switch between different application containers or their run modes just by changing property in `arquillian.xml`:

    <property name="chameleonTarget">wildfly:10.0.0.Final:managed</property>

Any other application server and its version can be used (such as EAP7), refer to Arquillian Chameleon documentation.

Remote run mode
---------------

If you want to run/debug test against remote container (that speeds up execution), you have to start required containers first - right now two containers are required with certain post-offset:

    ./bin/standalone.sh -c standalone-full-ha.xml -Djboss.socket.binding.port-offset=4321 -Djboss.node.name=node1 -Djboss.messaging.cluster.password=somepassword -Djava.net.preferIPv4Stack=true
    ./bin/standalone.sh -c standalone-full-ha.xml -Djboss.socket.binding.port-offset=4322 -Djboss.node.name=node2 -Djboss.messaging.cluster.password=somepassword -Djava.net.preferIPv4Stack=true

Since Arquillian is configured to detect running container (`allowConnectingToRunningServer=true`), it will detect running container and you don't have to anything else to run tests in dedicated test server:

    mvn verify -Dtest=TestMetricCollector

Debugging tests in Eclipse
--------------------------

Since Arquillian tests run in application server, it may be harder to debug them, but with IDE, it is quite easy:

1. start Eclipse
2. start two WildFly servers in Debug mode, using `standalone-full-ha.xml` and configured `port-offset` (see the configuration properties above)
3. insert breakpoint into a test or an implementation code
4. run the test class (or just selected method) by selecting its name in the IDE and choosing `Run as... JUnit Test`
5. the breakpoint will be hit once server deploys micro-deployment and executes the test method

If you need to debug failing test, it may be helpful to reveal what is going on in Arquillian - turn on its debug mode by passing following VM argument to the container (in Eclipse you need to modify launch configuration):

    -Darquillian.debug=true

Additionally, turn on AeroGear debug log by executing following CLI commands:

    /subsystem=logging/console-handler=AEROGEAR:add(formatter="%d{HH:mm:ss,SSS} %-5p [%c] (%t) %s%E%n",autoflush=true)
    /subsystem=logging/logger=org.jboss.aerogear.unifiedpush:add(level=FINEST,use-parent-handlers=false,handlers=["AEROGEAR"])

Writing new tests
-----------------

Since tests are deployed to running container, they can reuse anything that is deployed with them:

* inject CDI (`@Inject`) or EJB (`@EJB`) beans
* inject resources such as data sources or JMS destinations (`@Resource`)
* mock dependencies using Mockito (see `UnifiedPushArchive#withMockito`)

Tests can leverage basic JUnit features such as `@Before` and `@After`, `@Test(timeout)`, `@Test(expected)` or even `@Rule`, etc.

Tests for cluster
-----------------

_NOTE:_ **You need to enable multicast, as discussed [here](https://github.com/aerogear/aerogear-unifiedpush-server#getting-started-with-clustered-servers)**

Since we are running two containers in HA profile, we can test also behavior specific to clustering.

In that case, we need to define which deployment will be targetting which container (if no `@TargetsContainer` is specified, a container marked as `default=true` in `arquillian.xml` will be used):

    @Deployment(name = "war-1") @TargetsContainer("container-1") static WebArchive archive1() { ... }
    @Deployment(name = "war-2") @TargetsContainer("container-2") static WebArchive archive1() { ... }

Then test methods need to specify against which deployment will be executed and to ensure certain order of test method execution, they also need to specify ordering:

    @Test @OperateOnDeployment("war-1") @InSequence(1) performOnFirstServer() { ... }
    @Test @OperateOnDeployment("war-2") @InSequence(2) verifyOnSecondServer() { ... }

This allows you to, for example:

* queue JMS message on one server and receive it on another
* write a value into DB or cache and read on another server
