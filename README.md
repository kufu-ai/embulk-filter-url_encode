# URL Encode filter plugin for Embulk

The encode filter plugin encodes a column in the URL encoding format.

[![Circle CI](https://circleci.com/gh/mwed/embulk-filter-url_encode.svg?style=svg)](https://circleci.com/gh/mwed/embulk-filter-url_encode)

## Overview

* **Plugin type**: filter

## Configuration

- **column**: A column to encode  (string, required)
- **only_non_ascii**: When the option is true, the plugin does not Encode ASCII printable (string, default: `false`)

## Example

```yaml
filters:
  - type: url_encode
    column: url
```

## Build

```
$ ./gradlew gem  # -t to watch change of files and rebuild continuously
```

## License

```
Copyright 2016 Minnano Wedding Co., Ltd.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
