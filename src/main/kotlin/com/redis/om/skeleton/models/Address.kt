package com.redis.om.skeleton.models

import com.redis.om.spring.annotations.Indexed
import com.redis.om.spring.annotations.Searchable

class Address(
    @Indexed
    var houseNumber: String,
    @Searchable(nostem = true)
    var street: String,
    @Indexed
    var city: String,
    @Indexed
    var state: String,
    @Indexed
    var postalCode: String,
    @Indexed
    var country: String
)


