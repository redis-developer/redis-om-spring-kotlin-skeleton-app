# 🍀 Redis OM Spring Kotlin Skeleton App

Redis OM Spring provides powerful repository and custom object-mapping abstractions built on top of the powerful Spring Data Redis (SDR) framework. This app is a simple started application using Redis OM Spring to map, manipulate and query some simple Spring Data models backed by RedisJSON documents.

Check out our [workshop video](https://youtu.be/YhQX8pHy3hk) for this project on YouTube.

### Overview

The app provides a simple model `Person`:

* `id`: An autogenerated `String` using [ULIDs](https://github.com/ulid/spec)
* `firstName`: A `String` representing their first or given name.
* `lastName`: A `String` representing theirlast or surname.
* `age`: An `Integer` representing their age in years.
* `personalStatement`: A `String` representing a text personal statement containing facts or other biographical information.
* `homeLoc`: A `org.springframework.data.geo.Point` representing the geo coordinates.
* `address`: An entity of type `Address` representing the Person's postal address.
* `skills`: A `Set<String>`, representing a collection of Strings representing skills the person has.

#### Mapping Entities to JSON

The `Person` model is annotated with `@Document`, this enables the models to be saved as RedisJSON documents.
The individuals fields that will be used to create the entity search index are annotated with `@Indexed` or
`@Searchable` (for full-text search capable text fields).

```kotlin
@Document
class Person(
    @Id
    @Indexed
    var id: String? = null,

    // Indexed for exact text matching
    @Indexed
    var firstName: String,

    @Indexed
    var lastName: String,

    //Indexed for numeric matches
    @Indexed
    var age: Int,

    //Indexed for Full Text matches
    @Searchable
    var personalStatement: String,

    //Indexed for Geo Filtering
    @Indexed var homeLoc: Point,

    // Nest indexed object
    @Indexed
    var address: Address,

    @Indexed
    var skills: Set<String>
)
```

For example, saving the above object using a repository (see below) you'll get a JSON document under the Redis key `com.redis.om.skeleton.models.Person:01FXD97WTHNYFQEA56YQ27BNQ6`:

```json
{
  "id": "01FXD97WTHNYFQEA56YQ27BNQ6",
  "firstName": "Elizabeth",
  "lastName": "Olsen",
  "age": 32,
  "personalStatement": "You Guys Know I Can Move Things With My Mind, Right?",
  "homeLoc": "40.6976701,-74.2598641",
  "address": {
    "houseNumber": "20",
    "street": "W 34th St",
    "city": "New York",
    "state": "NY",
    "postalCode": "10001",
    "country": "US"
  },
  "skills": [
    "loyalty",
    "magic"
  ]
}
```

The resulting JSON has a generated value for the `id` field using a [ULIDs](https://github.com/ulid/spec). Note that
the `homeLoc` field is mapped to the lon-lat format expected by Redis, and the set of `skills` is mapped into a JSON array.

### The SpringBoot App

Use the `@EnableRedisDocumentRepositories` annotation to scan for `@Document` annotated Spring models,
Inject repositories beans implementing `RedisDocumentRepository` which you can use for CRUD operations and custom queries (all by declaring Spring Data Query Interfaces):

```kotlin
@SpringBootApplication
@EnableRedisDocumentRepositories("com.redis.om.skeleton.*")
class OmKotlinDemoApplication(val repo: PeopleRepository) {
  
}
```

### 🧰 The Repository

Redis OM Spring data repository's goal, like other Spring Data repositories, is to significantly reduce the amount of boilerplate code required to implement data access. Simply create a Java interface
that extends `RedisDocumentRepository` that takes the domain class to manage as well as the ID type of the domain class as type arguments. `RedisDocumentRepository` extends Spring Data's `PagingAndSortingRepository`.

Declare query methods on the interface. You can both, expose CRUD methods or create declarations for complex queries that Redis OM Spring will fullfil at runtime:

```kotlin
interface PeopleRepository : RedisDocumentRepository<Person, String> {
    // Find people by age range
    fun findByAgeBetween(minAge: Int, maxAge: Int): Iterable<Person>

    // Draws a circular geo-filter around a spot and returns all people in that radius
    fun findByHomeLoc(point: Point, distance: Distance): Iterable<Person>

    // Find people by their first and last name
    fun findByFirstNameAndLastName(firstName: String, lastName: String): Iterable<Person>

    // Performs full text search on a person's personal Statement
    fun searchByPersonalStatement(text: String): Iterable<Person>

    // Performing a tag search on city
    fun findByAddress_City(city: String): Iterable<Person>

    // Performing a full-search on street
    fun findByAddress_CityAndAddress_State(city: String, state: String): Iterable<Person>

    // Search Persons that have one of multiple skills (OR condition)
    fun findBySkills(skills: Set<String>): Iterable<Person>

    // Search Persons that have all the skills (AND condition):
    fun findBySkillsContainingAll(skills: Set<String>): Iterable<Person>

    // Performing a text search on all text fields:
    fun search(text: String?): Iterable<Person>
}
```

The repository proxy has two ways to derive a store-specific query from the method name:

- By deriving the query from the method name directly.
- By using a manually defined query using the `@Query` or `@Aggregation` annotations.

### CRUD Operations

You can inject a Repository and perform the basic CRUD operations as well as any custom queries you've defined:

```kotlin
@RestController
@RequestMapping("/api/v1/people")
class PeopleControllerV1(val repo: PeopleRepository) {

    @PostMapping("new")
    fun create(@RequestBody newPerson: Person): Person {
        return repo.save(newPerson)
    }
}
```

### 🚤 Querying with Entity Streams

Redis OM Spring Entity Streams provides a Java 8 Streams interface to Query Redis JSON documents using RediSearch. Entity Streams allow you to process data in a typesafe declarative way similar to SQL statements. Streams can be used to express a query as a chain of operations.

Entity Streams in Redis OM Spring provide the same semantics as Java 8 streams. Streams can be made of Redis Mapped entities (`@Document`) or one or more properties of an Entity. Entity Streams progressively build the query until a terminal operation is invoked (such as `collect`). Whenever a Terminal operation is applied to a Stream, the Stream cannot accept additional operations to its pipeline and it also means that the Stream is started.

Let's start with a simple example, a Spring `@Service` which includes `EntityStream` to query for instances of the mapped class `Person`:

```kotlin
package com.redis.om.skeleton.services

import com.redis.om.skeleton.models.Person
import com.redis.om.skeleton.models.`Person$`
import com.redis.om.spring.search.stream.EntityStream
import org.springframework.stereotype.Service
import redis.clients.jedis.search.aggr.SortedField
import java.util.stream.Collectors

@Service
class PeopleService(val entityStream: EntityStream) {

  // Find people by age range
  fun findByAgeBetween(minAge: Int, maxAge: Int): Iterable<Person> {
    return entityStream //
        .of(Person::class.java) //
            .filter(`Person$`.AGE.between(minAge, maxAge)) //
            .sorted(`Person$`.AGE, SortedField.SortOrder.ASC) //
            .collect(Collectors.toList())
  }

}
```

The `EntityStream` is injected into the `PeopleService` using `@Autowired`. We can then get a stream for `Person` objects by using `entityStream.of(Person.class)`. At this point the stream represents the equivalent of a `SELECT * FROM Person` on a relational database. The call to `collect` will then execute the underlying query and return a collection of all `Person` objects in Redis.

#### 👭 Entity Meta-model

To produce more elaborate queries, you're provided with a generated meta-model, which is a class with the same name as your model but ending with a dollar sign. In the
example below, our entity model is `Person` therefore we get a meta-model named `Person$`. With the meta-model you have access to the operations related to the
underlying search engine field. For example, in the example we have an `age` property which is an integer. Therefore our metamodel has an `AGE` property which has
numeric operations we can use with the stream's `filter` method such as `between`.

```kotlin
// Find people by age range
fun findByAgeBetween(minAge: Int, maxAge: Int): Iterable<Person> {
    return entityStream //
        .of(Person::class.java) //
        .filter(`Person$`.AGE.between(minAge, maxAge)) //
        .sorted(`Person$`.AGE, SortedField.SortOrder.ASC) //
        .collect(Collectors.toList())
}
```

In this example we also make use of the Streams `sorted` method to declare that our stream will be sorted by the `Person$.AGE` in `ASC`ending order.

### 💾 Get the Source Code

Clone the repository from GitHub:

```bash
$ git clone https://github.com/redis-developer/redis-om-spring-kotlin-skeleton-app
$ cd redis-om-spring-skeleton-app
```

### 🚀 Launch Redis

#### 🥞Redis Stack on Docker Locally

Redis OM Spring relies on the power of the [RediSearch][redisearch-url] and [RedisJSON][redis-json-url] modules.
We have provided a docker compose YAML file for you to quickly get started. To launch the docker compose application, on the command line (or via Docker Desktop), clone this repository and run (from the root folder):

```bash
docker compose up
```

This launches Redis Stack; Redis Stack Server on port 6379, and Redis Insight 8001.

#### 🌥️Redis Cloud

If you're using Redis Enterprise Cloud, you'll need the hostname, port number, and password for your database.  Use these to set the `application.properties` configuration like this:

```bash
spring.data.redis.host=<host>
spring.data.redis.port=<port>
spring.data.redis.password=<password>
spring.data.redis.username=<username>
```

For example if your Redis Enterprise Cloud database is at port `9139` on host `enterprise.redis.com` and your password is `5uper53cret` then you'd set `REDIS_OM_URL` as follows:

```bash
spring.data.redis.host=enterprise.redis.com
spring.data.redis.port=9139
spring.data.redis.password=5uper53cret
spring.data.redis.username=default
```

## 📝 Prerequisites

* Java 17 or higher
* Spring Boot 3.0.5 (included by Redis OM Spring) 
* A [Redis](https://redis.io) database with the [RediSearch](https://redisearch.io) module version TODO or higher installed.  We've provided a `docker-compose.yml` with Redis Stack for this. You can also [sign up for a free 30Mb database with Redis Enterprise Cloud](https://redis.com/try-free/) - be sure to check the box to configure a Redis Stack instance, follow [this guide](https://developer.redis.com/create/rediscloud/).
* [curl](https://curl.se/), or [Postman](https://www.postman.com/) - to send HTTP requests to the application.  We'll provide examples using curl in this document.
* Optional: [RedisInsight](https://redis.com/redis-enterprise/redis-insight/), a free data visualization and database management tool for Redis.  When downloading RedisInsight, be sure to select version 2.x.

## 🏃Run the App

```bash
./mvnw spring-boot:run
```

### 🧭Interact with the API

You can interact with the API directly with CURL or through the [Swagger interface](http://localhost:8080/swagger-ui/index.html).