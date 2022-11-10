<img width="100%" src="https://github.com/Flagsmith/flagsmith/raw/main/static-files/hero.png"/>

# Flagsmith Kotlin Android Client

The SDK client for Kotlin based Android applications for [https://www.flagsmith.com/](https://www.flagsmith.com/). Flagsmith allows you to manage feature flags and remote config across multiple projects, environments and organisations.

## Adding to your project

For full documentation visit [https://docs.flagsmith.com/clients/android](https://docs.flagsmith.com/clients/android)

## Resources

- [Website](https://www.flagsmith.com/)
- [Documentation](https://docs.flagsmith.com/)
- If you have any questions about our projects you can email [support@flagsmith.com](mailto:support@flagsmith.com)

## Development

To run the unit tests and develop using this repository you'll need to set your environment key using the environment variable `ENVIRONMENT_KEY`. E.g. to run the unit tests:

```bash
ENVIRONMENT_KEY=F5X.... ./gradlew  :FlagsmithClient:testDebugUnitTest
```