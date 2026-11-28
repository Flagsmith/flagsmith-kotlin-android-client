<img width="100%" src="https://github.com/Flagsmith/flagsmith/raw/main/static-files/hero.png"/>

[![Download](https://img.shields.io/maven-central/v/com.flagsmith/flagsmith-kotlin-android-client)](https://mvnrepository.com/artifact/com.flagsmith/flagsmith-kotlin-android-client)
![build](https://github.com/Flagsmith/flagsmith-kotlin-android-client/actions/workflows/verify.yml/badge.svg)

# Flagsmith Kotlin Android Client

> Flagsmith allows you to manage feature flags and remote config across multiple projects, environments and organisations.

The SDK client for Kotlin based Android applications for [https://www.flagsmith.com/](https://www.flagsmith.com/).

## Adding to your project

For full documentation visit [https://docs.flagsmith.com/clients/android](https://docs.flagsmith.com/clients/android)

## Background event delivery

By default, the client sends buffered tracking/exposure events and flag-usage
analytics when `ProcessLifecycleOwner` emits `ON_STOP` (the application enters
the background). Sends are asynchronous and use the existing event retry rules;
the normal flush timers continue unchanged. Moving between Activities or
recreating an Activity does not trigger a process background flush.

Pass `flushOnBackground = false` to the `Flagsmith` constructor to opt out.
`close()` removes the lifecycle observer as well as stopping background work.

This is best-effort delivery. Android delays the process stop notification;
killing the process before that notification or before the request completes
can still lose experiment events. This does not add event persistence or
offline recovery.

## Contributing

Please read [CONTRIBUTING.md](https://gist.github.com/kyle-ssg/c36a03aebe492e45cbd3eefb21cb0486) for details on our code of conduct, and the process for submitting pull requests

## Getting Help

If you encounter a bug or feature request we would like to hear about it. Before you submit an issue please search existing issues in order to prevent duplicates.

## Get in touch

If you have any questions about our projects you can email <a href="mailto:support@flagsmith.com">support@flagsmith.com</a>.

## Useful links

[Website](https://www.flagsmith.com/)

[Documentation](https://docs.flagsmith.com/)