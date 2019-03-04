Accessibility services have potential access to sensitive data within other apps as they may read
arbitrary data presented to the user. A11y Ally has access to this data and care must be taken to
prevent malicious apps on the device from using A11y Ally to extract data from other apps it should
not have access to. There are multiple entry points and attack surfaces presented in the app. This
document should cover these surfaces and how they are protected.

# Logcat Reports

Android used to allow applications to read the full logcat logs programmatically by executing the
`logcat` command and reading the `stdout` of it. At one point Android restricted the output of it
such that an application could only see the system's and its own logs when executing `logcat`. In
order to protect users on older versions of Android or if particular OEMs failed to include this
protection in their flavor of Android, the app will only write reports to Logcat during a recording
session. Starting a recording session requires sending a `startservice` intent to the app from
an app that has been granted the custom recording permission
`com.quittle.a11yally.MANAGE_RECORDING`.

# Report Files
