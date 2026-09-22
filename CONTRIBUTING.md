# Contributing

Thanks for your interest. This is a learning project: I am building it step by step to become a
backend engineer, so the most useful help is the kind that teaches me something.

## Tell me what is missing or could be better

This is the contribution I value most. Open an issue with the **Feedback** template and explain
what you would change and **why**: a link to the official docs, a trade-off I did not consider,
or a real problem it causes.

Found something that does not work? Use the **Bug report** template, with the request you sent and
the response you got.

## Build the same project in another language

You are welcome to build this API in parallel with another stack (Node, Go, .NET, Python, Kotlin…)
and compare the solutions.

1. The contract to implement is in [`docs/openapi/v1.0.0.json`](docs/openapi/v1.0.0.json). You
   can open it in [Swagger Editor](https://editor.swagger.io/). Same paths, same status codes and
   the same error format ([RFC 9457](https://www.rfc-editor.org/rfc/rfc9457)).
2. Build it in **your own repository**. It is your project, not a folder inside this one.
3. Open an issue with the **Port to another language** template so I can link it from the README.

The [CHANGELOG](CHANGELOG.md) lists what each version adds, so you can follow along version by
version.

## Pull requests

Pull requests are welcome for typos and documentation. For anything that changes the code,
please **open an issue first**: I want to understand and write the change myself, that is the point
of the project.

If you do send a pull request:

- Target the `develop` branch, not `main`.
- Use [Conventional Commits](https://www.conventionalcommits.org/) in English, for example
  `docs: fix the port in the README`.
- Make sure `./gradlew test` passes.
