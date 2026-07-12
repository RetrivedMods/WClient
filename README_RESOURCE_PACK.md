Ink Client - Resource Pack Module

This branch adds an enterprise-styled Resource Pack dashboard and a GlobalResourcePackChanger module that sends JSON commands to a local loopback agent for hot-swapping resource packs without requiring a client reload.

Quickstart:
1. Run the loopback agent on your development machine (JVM):
   - From the repository root run: `kotlin -classpath tools/loopback-agent/build/libs/* com.inkclient.agent.LoopbackAgentKt` (or build/run via Gradle/IDE)
   - The agent listens on 127.0.0.1:19133 and responds with a simple ACK.

2. Launch the Android app from the feature branch and open ResourcePackActivity (manually or wire into your main UI). The activity registers the GlobalResourcePackChanger module and exposes a Compose ResourcePackPanel with Apply and Hot-Swap actions.

Notes and next steps:
- For production hot-swap behavior, you must implement a platform-specific agent that can modify the running client's resource stack.
- This commit intentionally avoided a repo-wide automated rename of existing packages. If you want a full branding refactor, request it and I will apply it in this branch.
