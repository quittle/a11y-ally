# A11y Ally [![Build Status](https://travis-ci.org/quittle/a11y-ally.svg?branch=master)](https://travis-ci.org/quittle/a11y-ally)

This is a tool intended for developers to detect if the Android application they are developing is
accessible in a variety of ways. It is intended to be run alongside the main application and will
provide feedback to a developer both during manual inspection of the app during development and as
part of integration test runs by generating reports of issues found.

It currently supports partial functionality. Check the feature list below to see what features are currently supported

## Features

### Visual Feedback
* [x] MVP with visual feedback for focusable UI elements without text to render
* [x] UI to enable/disable service
* [ ] UI to specify which apps to provide feedback for
* [ ] Improve highlighting UX
* [ ] Investigate and if feasible, provide color-blindness renderings
* [x] Provide visual text-renderings of what labels are over UI
* [ ] Provide a linear, text-based overlay, allowing only linear-navigation like consumption

### Tests
* [ ] Monkey-runner style test, checking for focusable elements, never focused during navigation.

### Reports
* [x] Basic reporting of all focusable elements without text to logcat
* [ ] Report to text file
