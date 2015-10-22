                 Awake FILE
                Version 3.1
                  Readme


Introduction
------------

Awake FILE is a secure Open Source framework that allows to program very 
easily file uploads/downloads, remote file access and RPC through http. 
File transfers include powerful features like file chunking and automatic 
recovery mechanism.

Security has been taken into account from the design: server side allows 
to specify strong security rules in order to protect the files and to
secure the RPC calls.

Awake FILE is licensed through the GNU Lesser General Public License 
(LGPL v2.1): you can use it for free and without any constraints in your 
open source projects and in your commercial applications.


Build instructions
------------------

Awake FILE requires JDK 1.6+.

Main source code is in /src-main.
JUnit test suite is in /src-test.
Dependencies are in /lib

Awake FILE uses Ant to build the jars. 
(We use Eclipse 3.7+, but the builds may be adapted to any IDE):

- build-awake-file.xml: 
  builds the jar awake-file-client-3.1.jar and awake-file-server-3.1.jar 
  in /dist.  (Separated dependencies are in /lib).
  
- build-awake-test.xml
  Moves the test Configurator classes to the WEB-INF\classes of
  the servlet container.


Modify the webapp.lib.dir property according to your deployment
webapp directory.


JUnit tests
-----------

1) Create this directory on the client side: user.home/kawanfw-test.
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