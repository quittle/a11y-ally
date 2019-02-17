# A11y Ally [![Build Status](https://travis-ci.org/quittle/a11y-ally.svg?branch=master)](https://travis-ci.org/quittle/a11y-ally)

This is a tool intended for developers to detect if the Android application they are developing is
accessible in a variety of ways. It is intended to be run alongside the main application and will
provide feedback to a developer both during manual inspection of the app during development and as
part of integration test runs by generating reports of issues found.

> It currently only supports a small subset of intended functionality. Check the feature list below
or in the app to see what features are currently supported. If there's something missing you would
like, feel free to file an issue and I'll work on adding it.

## Features

### Filtering
* Select which apps to provide checks for
* Select what visual indicators to apply

### Visual Feedback
* Issue Highlighting - Indicates view missing labeling in red
* Content Description Labels - Overlay views with their `contentDescription`

### Reports
* Logging issues to [`logcat`](https://developer.android.com/studio/command-line/logcat)
* Toggling of logging via [Intent](https://developer.android.com/guide/components/intents-filters)

## Coming One Day
* A linear navigation based overlay for sighted developers to get a visual representation of the
  linear navigation layout
* Investigate checking color-issues
  * Color-blind friendliness
  * High text to background contrast
* Reporting issues to a text file to enable testing

## Usage

To get started, open the app and press the **Check Permissions** button, following the prompts to
grant the app permissions. Once, set up, press **Enable** to turn on the service. Use the toggles at
the top of the app to enable/disable various features. Once set up, you can preview the issues the
app will report in the **Preview Accessibility Overlay** section.

To enable logging to `logcat` and eventually to a file, you can send intents to the
`com.quittle.a11yally.RecordingService`. To do so, you must have the custom permission
`com.quittle.a11yally.MANAGE_RECORDING`. This is to prevent malicious apps from making recording and
attempting to find sensitive data revealed to A11y Ally. The intents currently supported are

* `com.quittle.a11yally.START_RECORDING` - Starts a recording session
* `com.quittle.a11yally.STOP_RECORDING` - Stops a recording session

To toggle via ADB, you can use the following commands

```sh
$ adb shell run-as com.quittle.a11yally am startservice \
    -n "com.quittle.a11yally/.RecordingService" \
    -a "com.quittle.a11yally.START_RECORDING" \
    --user 0
$ adb shell run-as com.quittle.a11yally am startservice \
    -n "com.quittle.a11yally/.RecordingService" \
    -a "com.quittle.a11yally.STOP_RECORDING" \
    --user 0
```

In order to simplify the permission's configuration necessary for a user to toggle from the
commandline, A11y Ally grants itself permission to perform these actions. You can then run as the
app's user (`com.quittle.a11yally` and `--user 0`) to start the service.
