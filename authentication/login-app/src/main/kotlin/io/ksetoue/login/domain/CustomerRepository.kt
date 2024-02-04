package io.ksetoue.login.domain

import org.springframework.data.repository.CrudRepository

interface CustomerRepository : CrudRepository<Customer, Long>