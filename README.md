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
* Select what visual indicators to appy

### Visual Feedback
* Issue Highlighting - Indicates view missing labeling in red
* Content Description Labels - Overlay views with their `contentDescription`

### Reports
* Logging issues to `logcat`

## Coming One Day
* A linear navigation based overlay for sighted developers to get a visual representation of the
  linear navigation layout
* Investigate checking color-issues
  * Color-blind friendliness
  * High text to background contrast
* Reporting issues to a text file to enable testing
