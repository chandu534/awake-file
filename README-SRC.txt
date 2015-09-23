                 Awake FILE
                Version 3.0
                  Readme


Introduction
------------

Awake FILE requires JDK 1.6+.

Main source code is in /src-main.
JUnit test suite is in /src-test.
Dependencies are in /lib


Build instructions
------------------

Awake FILE uses Ant to build the jars. 
(We use Eclipse 3.7+, but the builds may be adapted to any IDE):

- build-awake-file.xml: 
  builds the jar awake-file-3.0.jar and awake-file-android-3.0.jar 
  in /dist.  (Separated dependencies are in /lib).
  
- build-awake-test.xml
  Moves the test Configurator classes to the WEB-INF\classes of
  the servlet container.


Modify the webapp.lib.dir property according to your deployment
webapp directory.

JUnit tests
-----------

1) Create this directory on the client side: user.home/awake-test.
2) Create two image files: Koala.jpg and Tulips.jpg and copy them in 
  the user.home/awake-test directory.
3) Create a webapp in your servlet container and install all Awake FILEs 
   libraries and web.xml requirements.
4) Update org.awakefw.file.test.parms.TestParms.AWAKE_URL according to your webapp.
5) Run org.awakefw.file.test.run.TestRunnerJunit test suite.


Feedback
--------

Please direct all bugs to contact@awakeframework.org.

The Awake FILE Team