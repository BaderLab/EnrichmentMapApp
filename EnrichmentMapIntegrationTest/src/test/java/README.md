Some Notes about Pax Exam

- Can't figure out how to reuse utility classes from enrichmentmap-app in the integration tests so they had to be copied.
- For some reason using the same package names was causing NoClassDefFoundErrors, so I added 'integration' to the package names.
  - Note: These are not unit tests, so the trick of using the same package names is not necessary.