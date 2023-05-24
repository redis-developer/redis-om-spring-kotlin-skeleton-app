package com.redis.om.skeleton.models

import com.redis.om.spring.annotations.Document
import com.redis.om.spring.annotations.Indexed
import com.redis.om.spring.annotations.Searchable
import org.springframework.data.annotation.Id
import org.springframework.data.geo.Point

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
