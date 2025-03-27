![Cache-Only](.github/assets/CacheOnly_small.png)

[![GitHub license](https://img.shields.io/github/license/nagyesta/cache-only?color=informational)](https://raw.githubusercontent.com/nagyesta/cache-only/main/LICENSE)
[![Java version](https://img.shields.io/badge/Java%20version-17-yellow?logo=java)](https://img.shields.io/badge/Java%20version-17-yellow?logo=java)
[![latest-release](https://img.shields.io/github/v/tag/nagyesta/cache-only?color=blue&logo=git&label=releases&sort=semver)](https://github.com/nagyesta/cache-only/releases)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.nagyesta/cache-only?logo=apache-maven)](https://search.maven.org/artifact/com.github.nagyesta/cache-only)
[![JavaCI](https://img.shields.io/github/actions/workflow/status/nagyesta/cache-only/gradle.yml?logo=github&branch=main)](https://img.shields.io/github/actions/workflow/status/nagyesta/cache-only/gradle.yml?logo=github&branch=main)

[![Code Coverage](https://qlty.sh/badges/befb4798-6447-4608-838c-39d46e5cc55e/test_coverage.svg?nocache)](https://qlty.sh/gh/nagyesta/projects/cache-only)
[![Maintainability](https://qlty.sh/badges/befb4798-6447-4608-838c-39d46e5cc55e/maintainability.svg?nocache)](https://qlty.sh/gh/nagyesta/projects/cache-only)
[![last_commit](https://img.shields.io/github/last-commit/nagyesta/cache-only?logo=git)](https://img.shields.io/github/last-commit/nagyesta/cache-only?logo=git)
[![wiki](https://img.shields.io/badge/See-Wiki-informational)](https://github.com/nagyesta/cache-only/wiki)

## About Cache-Only

Cache-Only is a minimal library augmenting the Spring cache abstraction to better support bulk requests.

## What does this mean?

Bulk requests are challenging to cache on almost every level due to the fact that they contain multiple things in a
collection instead of caching each individual part. Although this is not always an issue, if the users/consumers of our
API want to access a slightly different set of items in each request, we cannot meaningfully reduce the number of calls
made to the full service by caching the response since the keys we would generate for the items are not likely to fully
match next time.

### A few examples for these APIs

- Searching for a set of your parcels over an API allowing you to enter up to 10 tracking Ids at a time
- Loading responses to a few comment threads under your blog post, article, video
- Comparing 2-5 similar items in a webshop
- Loading stock updates for a watchlist of stocks in a single request

### How will we solve this?

Cache-Only provides a few interfaces:

- [BatchRequestTransformer](src/main/java/com/github/nagyesta/cacheonly/transform/BatchRequestTransformer.java) to allow
  you to transform your bulk requests into partial requests containing only an easily cacheable piece each and back to
  bulk from these partial requests.
- [BatchResponseTransformer](src/main/java/com/github/nagyesta/cacheonly/transform/BatchResponseTransformer.java) to do
  the same for the responses.
- [PartialCacheSupport](src/main/java/com/github/nagyesta/cacheonly/transform/PartialCacheSupport.java) to define how
  each small part can be cached.

All of these are glued together by the implementations
of [CachingServiceTemplate](src/main/java/com/github/nagyesta/cacheonly/core/CachingServiceTemplate.java)
which are managing how the bulk request is split, then each part is looked up from the cache
using the cache support. For those which were missed, we can decide to merge into a bulk request and call the real API.
When the response is received, we just need to make sure each partial response is saved to the cache one-by-one. (Please
check out the [wiki](https://github.com/nagyesta/cache-only/wiki) for more details)

## Additional features

Thanks to the ability of splitting and merging requests and responses, Cache-Only can manage batch size limits and
automatic partitioning of the request to let you only focus on what is important and forget about the tedious task of
counting items and splitting them into multiple parts when needed.

## Examples

The [wiki](https://github.com/nagyesta/cache-only/wiki) contains a few examples you can use to find out how you can get
the most out of this library.
