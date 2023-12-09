# Sample project to reproduce a bug when using SSE with Spring and Tomcat 9

Sometimes the SSE message leaks to the body of a regular request.
For instance, if I have a heartbeat system that sends a message from time to time to the SSE connections,
the message may appear in the response of another unrelated request.

## Project structure

The [controller](src/main/java/com/github/eduhoribe/SseController.java) only has two endpoints,
one to create an SSE connection and another to send a sample json,
plus a scheduled task that runs every .5 seconds to send a heartbeat message to all SSE connections.

The [test](src/test/java/com/github/eduhoribe/TestSseController.java) basically does the following:

1. Starts a thread which creates SSE connections that will quickly disconnect
2. Starts another thread that runs in parallel with step 1,
   which executes a regular request with something returning in the body
3. Wait for the body of a request made in step 2 contains the message sent to a SseEmitter created in step 1

## Footnotes

- Tested with spring-boot `2.7.18`, spring-framework `5.3.31` and Tomcat embed `9.0.83`

- Also tested with spring-boot `3.2.0`, spring-framework `6.1.1` and Tomcat embedded `10.1.16` and the bug does not reproduce

- I didn't manage to reproduce the bug when no message sent before the `SseEmitter` object is returned to the
  client, which may indicate that the bug is in Spring

  _**however**_

- I also didn't manage to reproduce the bug when using Jetty instead of Tomcat,
  which may indicate that the bug is in Tomcat

A better way to test that would be to run the application with the same spring version,
changing only the tomcat version, but Spring `2.7` is not compatible with Tomcat `10`,
and Spring `3` is not compatible with Tomcat `9`
